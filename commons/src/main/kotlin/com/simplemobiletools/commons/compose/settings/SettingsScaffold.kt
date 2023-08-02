package com.simplemobiletools.commons.compose.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.onEventValue
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.isNotLitWell
import com.simplemobiletools.commons.compose.theme.isSurfaceLitWell
import com.simplemobiletools.commons.extensions.getColoredMaterialStatusBarColor
import com.simplemobiletools.commons.extensions.getContrastColor

@Composable
fun SettingsScaffold(
    title: String,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current

    val statusBarColor = onEventValue { context.getColoredMaterialStatusBarColor() }
    val contrastColor by remember(statusBarColor) {
        derivedStateOf { Color(statusBarColor.getContrastColor()) }
    }
    val systemUiController = rememberSystemUiController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val scrolledColor = lerp(
        start = if (isSurfaceLitWell()) Color.Black else Color.White,
        stop = contrastColor,
        fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    )
    SideEffect {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = scrolledColor.isNotLitWell())
    }
    val navigationIconInteractionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                Modifier
                    .matchParentSize()
                    .verticalScroll(rememberScrollState())
            ) {
                content()
            }
        }
    }
}

@MyDevices
@Composable
private fun SettingsScaffoldPreview() {
    AppThemeSurface {
        SettingsScaffold(title = "About", goBack = {}, content = {
            ListItem(headlineContent = { Text(text = "Some text") },
                leadingContent = {
                    Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null)
                })
        })
    }
}
