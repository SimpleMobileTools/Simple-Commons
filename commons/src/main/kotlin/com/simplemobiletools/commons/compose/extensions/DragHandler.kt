package com.simplemobiletools.commons.compose.extensions

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput

internal fun Modifier.listDragHandlerLongKey(
    lazyListState: LazyListState,
    haptics: HapticFeedback,
    selectedIds: MutableState<Set<Long>>,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float,
    dragUpdate: (Boolean) -> Unit
) = pointerInput(Unit) {


    var initialKey: Long? = null
    var currentKey: Long? = null
    val onDragCancelAndEnd = {
        initialKey = null
        autoScrollSpeed.value = 0f
        dragUpdate(false)
    }
    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            dragUpdate(true)
            lazyListState.itemKeyAtPosition(offset)?.let { key ->
                if (!selectedIds.value.contains(key)) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    initialKey = key
                    currentKey = key
                    selectedIds.value += key
                }
            }
        },
        onDragCancel = onDragCancelAndEnd,
        onDragEnd = onDragCancelAndEnd,
        onDrag = { change, _ ->
            if (initialKey != null) {
                val distFromBottom = lazyListState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y
                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                    distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                    else -> 0f
                }

                lazyListState.itemKeyAtPosition(change.position)?.let { key ->
                    if (currentKey != key) {
                        selectedIds.value = selectedIds.value
                            .minus(initialKey!!..currentKey!!)
                            .minus(currentKey!!..initialKey!!)
                            .plus(initialKey!!..key)
                            .plus(key..initialKey!!)
                        currentKey = key
                    }
                }
            }
        }
    )
}

internal fun LazyListState.itemKeyAtPosition(hitPoint: Offset): Long? =
    layoutInfo.visibleItemsInfo
        .firstOrNull { lazyListItemInfo ->
            hitPoint.y.toInt() in lazyListItemInfo.offset..lazyListItemInfo.offset + lazyListItemInfo.size
        }
        ?.key as? Long
