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
        tiny = 2.dp,
        small = 4.dp,
        smaller = 6.dp,
        medium = 8.dp,
        normal = 12.dp,
        activity = 16.dp,
        bigger = 20.dp,
        big = 24.dp,
        section = 32.dp,
        labelStart = 72.dp
    ),
    icon = Dimensions.IconSizes(
        normal = 48.dp,
        medium = 32.dp,
        shortcut = 64.dp
    )
)

internal val Sw600DpDimensions = CommonDimensions.copy()

val LocalDimensions: ProvidableCompositionLocal<Dimensions> =
    staticCompositionLocalOf { CommonDimensions }
