package com.simplemobiletools.commons.compose.alert_dialog

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState.Companion.SAVER

@Composable
fun rememberAlertDialogState(
    isShownInitially: Boolean = false
) = remember { AlertDialogState(isShownInitially) }

/**
 * Use this function to control the state whenever you want its visibility to be retained
 * even after configuration and process death
 * @param isShownInitially Boolean
 * @return AlertDialogState
 */
@Composable
fun rememberAlertDialogStateSaveable(
    isShownInitially: Boolean = false
) = rememberSaveable(saver = SAVER) { AlertDialogState(isShownInitially) }

@Stable
class AlertDialogState(isShownInitially: Boolean = false) {

    companion object {
        val SAVER = object : Saver<AlertDialogState, Boolean> {
            override fun restore(value: Boolean): AlertDialogState = AlertDialogState(value)
            override fun SaverScope.save(value: AlertDialogState): Boolean = value.isShown
        }
    }

    var isShown by mutableStateOf(isShownInitially)
        private set

    fun show() {
        if (isShown) {
            isShown = false
        }
        isShown = true
    }

    fun hide() {
        isShown = false
    }

    fun toggleVisibility() {
        isShown = !isShown
    }

    fun changeVisibility(predicate: Boolean) {
        isShown = predicate
    }

    @Composable
    fun DialogMember(
        content: @Composable () -> Unit
    ) {
        if (isShown) {
            content()
        }
    }
}
