package com.simplemobiletools.commons.compose.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal object DynamicThemeRipple : RippleTheme {
    @Composable
    override fun defaultColor(): Color = if (isSurfaceLitWell()) ripple_light else LocalContentColor.current

    @Composable
    override fun rippleAlpha(): RippleAlpha = DefaultRippleAlpha

    private val DefaultRippleAlpha = RippleAlpha(
        pressedAlpha = StateTokens.PressedStateLayerOpacity,
        focusedAlpha = StateTokens.FocusStateLayerOpacity,
        draggedAlpha = StateTokens.DraggedStateLayerOpacity,
        hoveredAlpha = StateTokens.HoverStateLayerOpacity
    )

    @Immutable
    internal object StateTokens {
        const val DraggedStateLayerOpacity = 0.16f
        const val FocusStateLayerOpacity = 0.12f
        const val HoverStateLayerOpacity = 0.08f
        const val PressedStateLayerOpacity = 0.12f
    }
}



