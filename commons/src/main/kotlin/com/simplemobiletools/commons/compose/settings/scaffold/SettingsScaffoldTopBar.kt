package com.simplemobiletools.commons.compose.settings.scaffold

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

@Composable
internal fun SettingsScaffoldTopBar(
    title: String,
    scrolledColor: Color,
    navigationIconInteractionSource: MutableInteractionSource,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    goBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val paddingValues = WindowInsets.navigationBars.asPaddingValues()
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(),
                color = scrolledColor
            )
        },
        navigationIcon = {
            SettingsNavigationIcon(goBack = goBack, navigationIconInteractionSource = navigationIconInteractionSource, iconColor = scrolledColor)
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color(statusBarColor),
            containerColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface,
            navigationIconContentColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.padding(
            top = paddingValues.calculateTopPadding(),
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection)
        ),
        windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.navigationBars)
    )
}

@Composable
internal fun SettingsScaffoldTopBar(
    title: @Composable (scrolledColor: Color) -> Unit,
    scrolledColor: Color,
    navigationIconInteractionSource: MutableInteractionSource,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    goBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val paddingValues = WindowInsets.navigationBars.asPaddingValues()
    TopAppBar(
        title = {
            title(scrolledColor)
        },
        navigationIcon = {
            SettingsNavigationIcon(goBack = goBack, navigationIconInteractionSource = navigationIconInteractionSource, iconColor = scrolledColor)
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color(statusBarColor),
            containerColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface,
            navigationIconContentColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.padding(
            top = paddingValues.calculateTopPadding(),
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection)
        ),
        windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.navigationBars)
    )
}

@Composable
internal fun SettingsScaffoldTopBar(
    title: @Composable (scrolledColor: Color) -> Unit,
    actions: @Composable RowScope.() -> Unit,
    scrolledColor: Color,
    navigationIconInteractionSource: MutableInteractionSource,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    goBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val paddingValues = WindowInsets.navigationBars.asPaddingValues()
    TopAppBar(
        title = {
            title(scrolledColor)
        },
        navigationIcon = {
            SettingsNavigationIcon(goBack = goBack, navigationIconInteractionSource = navigationIconInteractionSource, iconColor = scrolledColor)
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color(statusBarColor),
            containerColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface,
            navigationIconContentColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.padding(
            top = paddingValues.calculateTopPadding(),
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection)
        ),
        windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.navigationBars)
    )
}

@Composable
internal fun SettingsNavigationIcon(
    modifier: Modifier = Modifier,
    navigationIconInteractionSource: MutableInteractionSource,
    goBack: () -> Unit,
    iconColor: Color? = null
) {
    Box(
        modifier
            .padding(start = 8.dp)
            .clip(RoundedCornerShape(50))
            .clickable(
                navigationIconInteractionSource, rememberRipple(
                    color = MaterialTheme.colorScheme.onSurface,
                    bounded = true
                )
            ) { goBack() }
    ) {
        if (iconColor == null) {
            Icon(
                imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back),
                modifier = Modifier.padding(4.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back),
                tint = iconColor,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}


@Composable
@MyDevices
private fun SettingsScaffoldTopBarPreview() {
    AppThemeSurface {
        val interactionSource = remember { MutableInteractionSource() }
        SettingsScaffoldTopBar(
            title = "SettingsScaffoldTopBar",
            scrolledColor = Color.Black,
            navigationIconInteractionSource = interactionSource,
            goBack = {},
            statusBarColor = Color.Magenta.toArgb(),
            colorTransitionFraction = 1.0f,
            contrastColor = Color.Gray
        )
    }
}
