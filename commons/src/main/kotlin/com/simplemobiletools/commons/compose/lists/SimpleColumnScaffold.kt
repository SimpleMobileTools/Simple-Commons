package com.simplemobiletools.commons.compose.lists

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.simplemobiletools.commons.compose.extensions.AdjustNavigationBarColors
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

@Composable
fun SimpleColumnScaffold(
    title: String,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable (ColumnScope.(PaddingValues) -> Unit)
) {
    val context = LocalContext.current
    val (statusBarColor, contrastColor) = statusBarAndContrastColor(context)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(scrollBehavior, contrastColor)
    SystemUISettingsScaffoldStatusBarColor(scrolledColor)
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SimpleScaffoldTopBar(
                title = title,
                scrolledColor = scrolledColor,
                navigationIconInteractionSource = navigationIconInteractionSource,
                goBack = goBack,
                scrollBehavior = scrollBehavior,
                statusBarColor = statusBarColor,
                colorTransitionFraction = colorTransitionFraction,
                contrastColor = contrastColor
            )
        },
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            Column(
                Modifier
                    .matchParentSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement
            ) {
                content(paddingValues)
                Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
            }
        }
    }
}


@Composable
fun SimpleColumnScaffold(
    modifier: Modifier = Modifier,
    title: @Composable (scrolledColor: Color) -> Unit,
    goBack: () -> Unit,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable (ColumnScope.(PaddingValues) -> Unit)
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(context)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(scrollBehavior, contrastColor)
    SystemUISettingsScaffoldStatusBarColor(scrolledColor)
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SimpleScaffoldTopBar(
                title = title,
                scrolledColor = scrolledColor,
                navigationIconInteractionSource = navigationIconInteractionSource,
                goBack = goBack,
                scrollBehavior = scrollBehavior,
                statusBarColor = statusBarColor,
                colorTransitionFraction = colorTransitionFraction,
                contrastColor = contrastColor
            )
        }
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .verticalScroll(scrollState),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                content(paddingValues)
                Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
            }
        }
    }
}

@Composable
fun SimpleColumnScaffold(
    modifier: Modifier = Modifier,
    title: @Composable (scrolledColor: Color) -> Unit,
    actions: @Composable() (RowScope.() -> Unit),
    goBack: () -> Unit,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable (ColumnScope.(PaddingValues) -> Unit)
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(context)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(scrollBehavior, contrastColor)
    SystemUISettingsScaffoldStatusBarColor(scrolledColor)
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SimpleScaffoldTopBar(
                title = title,
                scrolledColor = scrolledColor,
                navigationIconInteractionSource = navigationIconInteractionSource,
                goBack = goBack,
                scrollBehavior = scrollBehavior,
                statusBarColor = statusBarColor,
                colorTransitionFraction = colorTransitionFraction,
                contrastColor = contrastColor,
                actions = actions
            )
        }
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .verticalScroll(scrollState),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                content(paddingValues)
                Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
            }
        }
    }
}

@Composable
fun SimpleColumnScaffold(
    modifier: Modifier = Modifier,
    customTopBar: @Composable (scrolledColor: Color, navigationInteractionSource: MutableInteractionSource, scrollBehavior: TopAppBarScrollBehavior, statusBarColor: Int, colorTransitionFraction: Float, contrastColor: Color) -> Unit,
    goBack: () -> Unit,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable() (ColumnScope.(PaddingValues) -> Unit)
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(context)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(scrollBehavior, contrastColor)
    SystemUISettingsScaffoldStatusBarColor(scrolledColor)
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            customTopBar(scrolledColor, navigationIconInteractionSource, scrollBehavior, statusBarColor, colorTransitionFraction, contrastColor)
        }
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .verticalScroll(scrollState),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                content(paddingValues)
                Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
            }
        }
    }
}


@MyDevices
@Composable
private fun SimpleColumnScaffoldPreview() {
    AppThemeSurface {
        SimpleColumnScaffold(title = "About", goBack = {}) {
            ListItem(headlineContent = { Text(text = "Some text") },
                leadingContent = {
                    Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null)
                })
        }
    }
}
