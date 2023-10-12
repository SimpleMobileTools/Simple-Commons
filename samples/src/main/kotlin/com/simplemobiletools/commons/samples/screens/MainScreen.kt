package com.simplemobiletools.commons.samples.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.lists.SimpleScaffold
import com.simplemobiletools.commons.compose.lists.simpleTopAppBarColors
import com.simplemobiletools.commons.compose.lists.topAppBarInsets
import com.simplemobiletools.commons.compose.lists.topAppBarPaddings
import com.simplemobiletools.commons.compose.menus.ActionItem
import com.simplemobiletools.commons.compose.menus.ActionMenu
import com.simplemobiletools.commons.compose.menus.OverflowMode
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MainScreen(
    openColorCustomization: () -> Unit,
    manageBlockedNumbers: () -> Unit,
    showComposeDialogs: () -> Unit,
    openTestButton: () -> Unit,
    showMoreApps: Boolean,
    openAbout: () -> Unit,
    moreAppsFromUs: () -> Unit,
) {
    SimpleScaffold(
        customTopBar = { scrolledColor: Color, _: MutableInteractionSource, scrollBehavior: TopAppBarScrollBehavior, statusBarColor: Int, colorTransitionFraction: Float, contrastColor: Color ->
            TopAppBar(
                title = {},
                actions = {
                    val actionMenus = remember {
                        buildActionMenuItems(
                            showMoreApps = showMoreApps,
                            openAbout = openAbout,
                            moreAppsFromUs = moreAppsFromUs
                        )
                    }
                    var isMenuVisible by remember { mutableStateOf(false) }
                    ActionMenu(
                        items = actionMenus,
                        numIcons = 2,
                        isMenuVisible = isMenuVisible,
                        onMenuToggle = { isMenuVisible = it },
                        iconsColor = scrolledColor
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = simpleTopAppBarColors(statusBarColor, colorTransitionFraction, contrastColor),
                modifier = Modifier.topAppBarPaddings(),
                windowInsets = topAppBarInsets()
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = openColorCustomization
            ) {
                Text(stringResource(id = R.string.color_customization))
            }
            Button(
                onClick = openAbout
            ) {
                Text("About")
            }
            Button(
                onClick = manageBlockedNumbers
            ) {
                Text("Manage blocked numbers")
            }
            Button(
                onClick = showComposeDialogs
            ) {
                Text("Compose dialogs")
            }
            Button(
                onClick = openTestButton
            ) {
                Text("Test button")
            }
        }
    }
}

private fun buildActionMenuItems(
    showMoreApps: Boolean,
    openAbout: () -> Unit,
    moreAppsFromUs: () -> Unit
): ImmutableList<ActionItem> {
    val list = mutableListOf<ActionItem>()
    list += ActionItem(
        R.string.about,
        icon = Icons.Outlined.Info,
        doAction = openAbout,
        overflowMode = OverflowMode.NEVER_OVERFLOW
    )
    if (showMoreApps) {
        list += ActionItem(
                R.string.more_apps_from_us,
                doAction = moreAppsFromUs,
                overflowMode = OverflowMode.ALWAYS_OVERFLOW
            )
    }
    return list.toImmutableList()
}

@Composable
@MyDevices
private fun MainScreenPreview() {
    AppThemeSurface {
        MainScreen(
            openColorCustomization = {},
            manageBlockedNumbers = {},
            showComposeDialogs = {},
            openTestButton = {},
            showMoreApps = true,
            openAbout = {},
            moreAppsFromUs = {}
        )
    }
}
