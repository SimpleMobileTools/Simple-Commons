package com.simplemobiletools.commons.compose.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.simplemobiletools.commons.compose.theme.model.Theme.Companion.systemDefaultMaterialYou


@Composable
fun getCurrentTheme() = getTheme(LocalContext.current, systemDefaultMaterialYou())

@Composable
@ReadOnlyComposable
fun isInDarkThemeOrSurfaceIsNotLitWell() = isSystemInDarkTheme() || isSurfaceNotLitWell()

@Composable
@ReadOnlyComposable
fun isInDarkThemeAndSurfaceIsNotLitWell() = isSystemInDarkTheme() && isSurfaceNotLitWell()

internal const val LUMINANCE_THRESHOLD = 0.5f

@Composable
@ReadOnlyComposable
fun isSurfaceNotLitWell(threshold: Float = LUMINANCE_THRESHOLD) = SimpleTheme.colorScheme.surface.luminance() < threshold

@Composable
@ReadOnlyComposable
fun isSurfaceLitWell(threshold: Float = LUMINANCE_THRESHOLD) = SimpleTheme.colorScheme.surface.luminance() > threshold

internal fun Context.isDarkMode(): Boolean {
    val darkModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}
