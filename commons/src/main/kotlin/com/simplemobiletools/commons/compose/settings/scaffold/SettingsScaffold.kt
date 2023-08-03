package com.simplemobiletools.commons.compose.settings.scaffold

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

@Composable
fun SettingsScaffold(
    title: String,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(context)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(scrollBehavior, contrastColor)
    SystemUISettingsScaffoldStatusBarColor(scrolledColor)
    val navigationIconInteractionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingsScaffoldTopBar(
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
