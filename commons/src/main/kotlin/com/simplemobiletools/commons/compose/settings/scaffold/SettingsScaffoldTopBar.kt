package com.simplemobiletools.commons.compose.settings.scaffold

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
            Box(
                Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        navigationIconInteractionSource, rememberRipple(
                            color = MaterialTheme.colorScheme.onSurface,
                            bounded = true
                        )
                    ) { goBack() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back),
                    tint = scrolledColor,
                    modifier = Modifier.padding(4.dp)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color(statusBarColor),
            containerColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface,
            navigationIconContentColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface
        ),
    )
}

@Composable
internal fun SettingsScaffoldTopBar(
    title: @Composable (scrolledColor : Color) -> Unit,
    scrolledColor: Color,
    navigationIconInteractionSource: MutableInteractionSource,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    goBack: () -> Unit,
) {
    TopAppBar(
        title = {
            title(scrolledColor)
        },
        navigationIcon = {
            Box(
                Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        navigationIconInteractionSource, rememberRipple(
                            color = MaterialTheme.colorScheme.onSurface,
                            bounded = true
                        )
                    ) { goBack() }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back),
                    tint = scrolledColor,
                    modifier = Modifier.padding(4.dp)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color(statusBarColor),
            containerColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface,
            navigationIconContentColor = if (colorTransitionFraction == 1f) contrastColor else MaterialTheme.colorScheme.surface
        ),
    )
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
