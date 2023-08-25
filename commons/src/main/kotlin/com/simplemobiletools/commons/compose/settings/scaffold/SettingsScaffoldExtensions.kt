package com.simplemobiletools.commons.compose.settings.scaffold

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.simplemobiletools.commons.compose.extensions.onEventValue
import com.simplemobiletools.commons.compose.extensions.rememberWindowInsetsController
import com.simplemobiletools.commons.compose.theme.isNotLitWell
import com.simplemobiletools.commons.compose.theme.isSurfaceLitWell
import com.simplemobiletools.commons.extensions.getColoredMaterialStatusBarColor
import com.simplemobiletools.commons.extensions.getContrastColor

@Composable
internal fun SystemUISettingsScaffoldStatusBarColor(scrolledColor: Color) {
    val insetController = rememberWindowInsetsController()
    insetController.isAppearanceLightStatusBars = scrolledColor.isNotLitWell()
}

@Composable
internal fun ScreenBoxSettingsScaffold(paddingValues: PaddingValues, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection)
            )
    ) {
        content()
    }
}

@Composable
internal fun statusBarAndContrastColor(context: Context): Pair<Int, Color> {
    val statusBarColor = onEventValue { context.getColoredMaterialStatusBarColor() }
    val contrastColor by remember(statusBarColor) {
        derivedStateOf { Color(statusBarColor.getContrastColor()) }
    }
    return Pair(statusBarColor, contrastColor)
}

@Composable
internal fun transitionFractionAndScrolledColor(
    scrollBehavior: TopAppBarScrollBehavior,
    contrastColor: Color
): Pair<Float, Color> {
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val scrolledColor = lerp(
        start = if (isSurfaceLitWell()) Color.Black else Color.White,
        stop = contrastColor,
        fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    )
    return Pair(colorTransitionFraction, scrolledColor)
}
