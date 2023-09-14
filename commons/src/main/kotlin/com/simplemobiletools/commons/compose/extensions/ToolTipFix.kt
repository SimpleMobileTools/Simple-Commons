package com.simplemobiletools.commons.compose.extensions

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.BasicTooltipDefaults
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

//https://issuetracker.google.com/issues/299500338
@Composable
@ExperimentalMaterial3Api
fun rememberTooltipStateFix(
    initialIsVisible: Boolean = false,
    isPersistent: Boolean = false
): TooltipState {
    return rememberSaveable(
        isPersistent,
        saver = TooltipStateImpl.Saver
    ) {
        TooltipStateImpl(
            initialIsVisible = initialIsVisible,
            isPersistent = isPersistent,
            mutatorMutex = BasicTooltipDefaults.GlobalMutatorMutex
        )
    }
}

@Stable
private class TooltipStateImpl(
    initialIsVisible: Boolean,
    override val isPersistent: Boolean,
    private val mutatorMutex: MutatorMutex
) : TooltipState {
    override val transition: MutableTransitionState<Boolean> =
        MutableTransitionState(initialIsVisible)

    override val isVisible: Boolean
        get() = transition.currentState || transition.targetState

    /**
     * continuation used to clean up
     */
    private var job: (CancellableContinuation<Unit>)? = null

    /**
     * Show the tooltip associated with the current [BasicTooltipState].
     * When this method is called, all of the other tooltips associated
     * with [mutatorMutex] will be dismissed.
     *
     * @param mutatePriority [MutatePriority] to be used with [mutatorMutex].
     */
    override suspend fun show(
        mutatePriority: MutatePriority
    ) {
        val cancellableShow: suspend () -> Unit = {
            suspendCancellableCoroutine { continuation ->
                transition.targetState = true
                job = continuation
            }
        }

        // Show associated tooltip for [TooltipDuration] amount of time
        // or until tooltip is explicitly dismissed depending on [isPersistent].
        mutatorMutex.mutate(mutatePriority) {
            try {
                if (isPersistent) {
                    cancellableShow()
                } else {
                    withTimeout(BasicTooltipDefaults.TooltipDuration) {
                        cancellableShow()
                    }
                }
            } finally {
                // timeout or cancellation has occurred
                // and we close out the current tooltip.
                dismiss()
            }
        }
    }

    /**
     * Dismiss the tooltip associated with
     * this [TooltipState] if it's currently being shown.
     */
    override fun dismiss() {
        transition.targetState = false
    }

    /**
     * Cleans up [mutatorMutex] when the tooltip associated
     * with this state leaves Composition.
     */
    override fun onDispose() {
        job?.cancel()
    }

    companion object {
        /**
         * The default [Saver] implementation for [TooltipStateImpl].
         */
        val Saver = Saver<TooltipStateImpl, Any>(
            save = {
                listOf(
                    it.isVisible,
                    it.isPersistent,
                )
            },
            restore = {
                val (isVisible, isPersistent) = it as List<*>
                TooltipStateImpl(
                    initialIsVisible = isVisible as Boolean,
                    isPersistent = isPersistent as Boolean,
                    mutatorMutex = BasicTooltipDefaults.GlobalMutatorMutex,
                )
            }
        )
    }
}
