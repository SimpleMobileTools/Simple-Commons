package com.simplemobiletools.commons.compose.bottom_sheet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch

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
    fun rememberWindowInsets(
        defaultInsets: WindowInsets = BottomSheetDefaults.windowInsets
    ) = remember { if (edgeToEdgeEnabled) WindowInsets(0) else defaultInsets }

    @Composable
    fun rememberSheetState() = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = confirmValueChange
    )

    var isOpen by mutableStateOf(openBottomSheet)
        private set

    private var closeDialog by mutableStateOf(false)

    /**
     * Closes the dialog completely, calling [open] will create the dialog
     * from scratch.
     */
    fun close() {
        if (closeDialog) {
            closeDialog = false
        }
        closeDialog = true
    }

    fun open() {
        if (isOpen) {
            isOpen = false
        }
        isOpen = true
    }

    /**
     * Minimises the dialog, calling [open] again will
     * maximize it, keep in mind you need to call it with the
     * appropriate params to avoid jumping if for example: it was fully expanded then
     * you've minimised it and now you've called [open] again.
     */
    fun minimise() {
        isOpen = false
    }

    @Composable
    fun DialogMember(
        content: @Composable () -> Unit
    ) {
        val bottomSheetState = rememberSheetState()

        LaunchedEffect(closeDialog) {
            if (closeDialog) {
                launch { bottomSheetState.hide() }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        minimise()
                    }
                }
            }
        }

        if (isOpen) {
            content()
        }
    }
}
