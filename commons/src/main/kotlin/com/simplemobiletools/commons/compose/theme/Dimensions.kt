package com.simplemobiletools.commons.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.theme.model.Dimensions

val Dimens: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current

internal val CommonDimensions = Dimensions(
    margin = Dimensions.Margins(
        extraSmall = 2.dp,
        small = 4.dp,
        medium = 8.dp,
        large = 12.dp,
        extraLarge = 16.dp,
    ),
    icon = Dimensions.IconSizes(
        small = 32.dp,
        medium = 48.dp,
        large = 64.dp,
    )
)

internal val Sw600DpDimensions = CommonDimensions.copy()

val LocalDimensions: ProvidableCompositionLocal<Dimensions> =
    staticCompositionLocalOf { CommonDimensions }
