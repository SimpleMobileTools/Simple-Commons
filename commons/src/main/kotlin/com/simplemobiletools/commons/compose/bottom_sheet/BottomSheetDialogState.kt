package com.simplemobiletools.commons.compose.bottom_sheet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.simplemobiletools.commons.compose.alert_dialog.dialogContainerColor
import com.simplemobiletools.commons.compose.alert_dialog.dialogElevation

@Composable
fun rememberBottomSheetDialogState(
    openBottomSheet: Boolean = false,
    skipPartiallyExpanded: Boolean = false,
    edgeToEdgeEnabled: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
) = remember {
    BottomSheetDialogState(
        openBottomSheet = openBottomSheet,
        skipPartiallyExpanded = skipPartiallyExpanded,
        edgeToEdgeEnabled = edgeToEdgeEnabled,
        confirmValueChange = confirmValueChange
    )
}

@Composable
fun rememberBottomSheetDialogStateSaveable(
    openBottomSheet: Boolean = false,
    skipPartiallyExpanded: Boolean = false,
    edgeToEdgeEnabled: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
) = rememberSaveable(stateSaver = mapSaver(save = {
    mapOf(
        "skipPartiallyExpanded" to skipPartiallyExpanded,
        "edgeToEdgeEnabled" to edgeToEdgeEnabled,
        "openBottomSheet" to openBottomSheet,
    )
}, restore = {
    BottomSheetDialogState(
        openBottomSheet = it["openBottomSheet"] as Boolean,
        skipPartiallyExpanded = it["openBottomSheet"] as Boolean,
        edgeToEdgeEnabled = it["openBottomSheet"] as Boolean,
    )
})) {
    mutableStateOf(
        BottomSheetDialogState(
            skipPartiallyExpanded = skipPartiallyExpanded,
            edgeToEdgeEnabled = edgeToEdgeEnabled,
            confirmValueChange = confirmValueChange
        )
    )
}

@Stable
class BottomSheetDialogState(
    openBottomSheet: Boolean = false,
    private val skipPartiallyExpanded: Boolean = false,
    private val edgeToEdgeEnabled: Boolean = false,
    private val confirmValueChange: (SheetValue) -> Boolean = { true },
) {
    @Composable
    private fun rememberWindowInsets(
        defaultInsets: WindowInsets = BottomSheetDefaults.windowInsets
    ) = remember { if (edgeToEdgeEnabled) WindowInsets(0) else defaultInsets }

    @Composable
    private fun rememberSheetState() = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = confirmValueChange
    )

    var isOpen by mutableStateOf(openBottomSheet)
        private set

    fun close() {
        isOpen = false
    }

    fun open() {
        isOpen = true
    }

    @Composable
    fun BottomSheetContent(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val bottomSheetState = rememberSheetState()
        val windowInsets = rememberWindowInsets()

        LaunchedEffect(isOpen) {
            if (isOpen && !bottomSheetState.isVisible) {
                bottomSheetState.show()
            } else {
                bottomSheetState.hide()
            }
        }

        if (isOpen) {
            ModalBottomSheet(
                modifier = modifier,
                onDismissRequest = ::close,
                sheetState = bottomSheetState,
                windowInsets = windowInsets,
                dragHandle = {}, //leave empty as we provide our own dialog surfaces
                shape = bottomSheetDialogShape,
                containerColor = dialogContainerColor,
                tonalElevation = dialogElevation,
            ) {
                content()
            }
        }
    }
}
