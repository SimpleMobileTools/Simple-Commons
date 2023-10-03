package com.simplemobiletools.commons.compose.theme.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Stable
data class Dimensions(
    val margin: Margins,
    val text: TextSizes,
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
    data class TextSizes(
        val tiny: TextUnit,
        val small: TextUnit,
        val smaller: TextUnit,
        val normal: TextUnit,
        val medium: TextUnit,
        val bigger: TextUnit,
        val middle: TextUnit,
        val big: TextUnit,
        val extraBig: TextUnit,
        val actionBar: TextUnit
    )

    @Stable
    data class IconSizes(
        val normal: Dp,
        val medium: Dp,
        val shortcut: Dp,
    )
}
