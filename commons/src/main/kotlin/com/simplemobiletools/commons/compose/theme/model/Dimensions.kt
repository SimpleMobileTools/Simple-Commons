package com.simplemobiletools.commons.compose.theme.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp

@Stable
data class Dimensions(
    val margin: Margins,
    val icon: IconSizes
) {
    @Stable
    data class Margins(
        val tiny: Dp,
        val small: Dp,
        val smaller: Dp,
        val medium: Dp,
        val normal: Dp,
        val activity: Dp,
        val bigger: Dp,
        val big: Dp,
        val section: Dp,
        val labelStart: Dp
    )

    @Stable
    data class IconSizes(
        val normal: Dp,
        val medium: Dp,
        val shortcut: Dp,
    )
}
