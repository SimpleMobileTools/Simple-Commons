package com.simplemobiletools.commons.dialogs

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.theme.LocalTheme
import com.simplemobiletools.commons.compose.theme.Shapes
import com.simplemobiletools.commons.compose.theme.light_grey_stroke
import com.simplemobiletools.commons.compose.theme.model.Theme
import kotlinx.coroutines.android.awaitFrame

val dialogContainerColor
    @ReadOnlyComposable
    @Composable get() = when (LocalTheme.current) {
        is Theme.BlackAndWhite -> Color.Black
        else -> MaterialTheme.colorScheme.surface
    }

val dialogShape = Shapes.large

val dialogElevation = 0.dp

val dialogTextColor @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val Modifier.dialogBorder: Modifier
    @ReadOnlyComposable
    @Composable get() =
        when (LocalTheme.current) {
            is Theme.BlackAndWhite -> then(Modifier.border(1.dp, light_grey_stroke, dialogShape))
            else -> Modifier
        }

@Composable
fun ShowKeyboardWhenDialogIsOpenedAndRequestFocus(
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    focusRequester: FocusRequester?
) {
    LaunchedEffect(Unit) {
        //await two frames to render the scrim and the dialog
        awaitFrame()
        awaitFrame()
        keyboardController?.show()
        focusRequester?.requestFocus()
    }
}
