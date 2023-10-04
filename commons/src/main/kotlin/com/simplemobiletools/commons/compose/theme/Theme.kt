package com.simplemobiletools.commons.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.simplemobiletools.commons.compose.extensions.config
import com.simplemobiletools.commons.compose.theme.model.Theme
import com.simplemobiletools.commons.compose.theme.model.Theme.Companion.systemDefaultMaterialYou
import com.simplemobiletools.commons.helpers.isSPlus

@Composable
internal fun Theme(
    theme: Theme = systemDefaultMaterialYou(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val baseConfig = remember { context.config }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val colorScheme = if (!view.isInEditMode) {
        when {
            theme is Theme.SystemDefaultMaterialYou && isSPlus() -> {
                if (isSystemInDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            theme is Theme.Custom || theme is Theme.Dark -> darkColorScheme(
                primary = theme.primaryColor,
                surface = theme.backgroundColor,
                onSurface = theme.textColor
            )

            theme is Theme.White -> darkColorScheme(
                primary = Color(theme.accentColor),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor,
            )

            theme is Theme.BlackAndWhite -> darkColorScheme(
                primary = Color(theme.accentColor),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor
            )

            else -> darkColorScheme
        }
    } else {
        previewColorScheme()
    }

    SideEffect {
        updateRecentsAppIcon(baseConfig, context)
    }

    val dimensions = CommonDimensions

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        content = {
            CompositionLocalProvider(
                LocalRippleTheme provides DynamicThemeRipple,
                LocalTheme provides theme,
                LocalDimensions provides dimensions
            ) {
                content()
            }
        },
    )
}

val LocalTheme: ProvidableCompositionLocal<Theme> =
    staticCompositionLocalOf { Theme.Custom(1, 1, 1, 1) }

@Composable
private fun previewColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme
} else {
    lightColorScheme
}

