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
        val extraSmall: Dp,
        val small: Dp,
        val medium: Dp,
        val large: Dp,
        val extraLarge: Dp,
    )

    @Stable
    data class IconSizes(
        val small: Dp,
        val medium: Dp,
        val large: Dp,
    )
}
