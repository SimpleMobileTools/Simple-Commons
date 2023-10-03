package com.simplemobiletools.commons.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.simplemobiletools.commons.compose.theme.LocalTheme
import com.simplemobiletools.commons.compose.theme.model.Theme
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getProperPrimaryColor

@Composable
fun linkColor(
    theme: Theme = LocalTheme.current,
    accentColor: Int = LocalContext.current.baseConfig.accentColor,
    getProperPrimaryColor: Int = LocalContext.current.getProperPrimaryColor()
): Color = onEventValue {
    Color(
        when (theme) {
            is Theme.BlackAndWhite, is Theme.White -> accentColor
            else -> getProperPrimaryColor
        }
    )
}
