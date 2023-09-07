package com.simplemobiletools.commons.compose.extensions

import android.util.Log
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
    autoScrollThreshold: Float,
    //enableActionMode: () -> Unit
) = pointerInput(Unit) {
    fun LazyListState.gridItemKeyAtPosition(hitPoint: Offset): Long? =
        layoutInfo.visibleItemsInfo
            .firstOrNull { lazyListItemInfo ->
                hitPoint.y.toInt() >= lazyListItemInfo.offset && hitPoint.y.toInt() < lazyListItemInfo.offset + lazyListItemInfo.size
                //hitPoint.y.toInt() in lazyListItemInfo.offset..lazyListItemInfo.offset + lazyListItemInfo.size
            }
            ?.key as? Long

    var initialKey: Long? = null
    var currentKey: Long? = null
    val onDragCancelAndEnd = {
        initialKey = null
        autoScrollSpeed.value = 0f
    }
    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            Log.d("detectDragGesturesAfterLongPress", "onDragStart $offset")
            lazyListState.gridItemKeyAtPosition(offset)?.let { key ->
                Log.d("detectDragGesturesAfterLongPress", "gridItemKeyAtPosition $key")
                if (!selectedIds.value.contains(key)) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    initialKey = key
                    currentKey = key
                    selectedIds.value += key
                    //enableActionMode()
                    Log.d("detectDragGesturesAfterLongPress", "check key=$key initial=$initialKey and current=$currentKey and ${selectedIds.value.toList()}")
                }
            }
        },
        onDragCancel = {
            onDragCancelAndEnd()
            Log.d("detectDragGesturesAfterLongPress", "onDragCancel")
        },
        onDragEnd = {
            onDragCancelAndEnd()
            Log.d("detectDragGesturesAfterLongPress", "onDragEnd")
        },
        onDrag = { change, _ ->
            if (initialKey != null) {
                val distFromBottom = lazyListState.layoutInfo.viewportSize.height - change.position.y
                val distFromTop = change.position.y
                autoScrollSpeed.value = when {
                    distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                    distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                    else -> 0f
                }

                lazyListState.gridItemKeyAtPosition(change.position)?.let { key ->
                    Log.d("detectDragGesturesAfterLongPress", "onDrag $key and $currentKey")

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
