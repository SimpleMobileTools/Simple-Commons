package com.simplemobiletools.commons.compose.extensions

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.dragHandler(
    lazyListState: LazyListState,
    haptics: HapticFeedback,
    selectedIds: MutableState<Set<Long>>,
    autoScrollSpeed: MutableState<Float>,
    autoScrollThreshold: Float
) = pointerInput(Unit) {
    fun LazyListState.gridItemKeyAtPosition(hitPoint: Offset): Long? =
        layoutInfo.visibleItemsInfo
            .firstOrNull { lazyListItemInfo ->
                hitPoint.y.toInt() in lazyListItemInfo.offset..lazyListItemInfo.offset + lazyListItemInfo.size }
            ?.key as? Long

    var initialKey: Long? = null
    var currentKey: Long? = null
    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            lazyListState.gridItemKeyAtPosition(offset)?.let { key ->
                if (!selectedIds.value.contains(key)) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    initialKey = key
                    currentKey = key
                    selectedIds.value += key
                }
            }
        },
        onDragCancel = { initialKey = null; autoScrollSpeed.value = 0f },
        onDragEnd = { initialKey = null; autoScrollSpeed.value = 0f },
        onDrag = { change, _ ->
            val initialKeyNotNull = initialKey
            if (initialKeyNotNull != null) {
                val distFromBottom =
                    lazyListState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y
                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                    distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                    else -> 0f
                }

                lazyListState.gridItemKeyAtPosition(change.position)?.let { key ->
                    if (currentKey != key) {
                        selectedIds.value = selectedIds.value
                            .minus(initialKeyNotNull..currentKey!!)
                            .minus(currentKey!!..initialKeyNotNull)
                            .plus(initialKeyNotNull..key)
                            .plus(key..initialKeyNotNull)
                        currentKey = key
                    }
                }
            }
        }
    )
}
