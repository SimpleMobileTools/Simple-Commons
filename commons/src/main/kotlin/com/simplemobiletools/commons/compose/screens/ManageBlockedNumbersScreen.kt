package com.simplemobiletools.commons.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.BooleanPreviewParameterProvider
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.dragHandler
import com.simplemobiletools.commons.compose.extensions.ifTrue
import com.simplemobiletools.commons.compose.menus.ActionItem
import com.simplemobiletools.commons.compose.menus.ActionMenu
import com.simplemobiletools.commons.compose.menus.OverflowMode
import com.simplemobiletools.commons.compose.settings.SettingsCheckBoxComponent
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsLazyScaffold
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsNavigationIcon
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsScaffoldTopBar
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.ripple_dark
import com.simplemobiletools.commons.compose.theme.ripple_light
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.models.BlockedNumber
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
internal fun ManageBlockedNumbersScreen(
    goBack: () -> Unit,
    onAdd: () -> Unit,
    onImportBlockedNumbers: () -> Unit,
    onExportBlockedNumbers: () -> Unit,
    setAsDefault: () -> Unit,
    isDialer: Boolean,
    hasGivenPermissionToBlock: Boolean,
    isBlockUnknownSelected: Boolean,
    onBlockUnknownSelectedChange: (Boolean) -> Unit,
    isHiddenSelected: Boolean,
    onHiddenSelectedChange: (Boolean) -> Unit,
    blockedNumbers: ImmutableList<BlockedNumber>,
    onDelete: (Set<Long>) -> Unit,
    onEdit: (BlockedNumber) -> Unit,
    onCopy: (BlockedNumber) -> Unit
) {
    val startingPadding = Modifier.padding(horizontal = 4.dp)
    val selectedIds: MutableState<Set<Long>> = rememberSaveable { mutableStateOf(emptySet()) }
    val isInActionMode by remember { derivedStateOf { selectedIds.value.isNotEmpty() } }
    val clearSelection = remember {
        { selectedIds.value = emptySet() }
    }
    SettingsLazyScaffold(
        customTopBar = { scrolledColor: Color,
                         navigationInteractionSource: MutableInteractionSource,
                         scrollBehavior: TopAppBarScrollBehavior,
                         statusBarColor: Int,
                         colorTransitionFraction: Float,
                         contrastColor: Color ->
            if (isInActionMode) {
                ActionModeToolbar(
                    selectedIdsCount = selectedIds.value.count(),
                    blockedNumbersCount = blockedNumbers.count(),
                    onBackClick = clearSelection,
                    onCopy = {
                        onCopy(blockedNumbers.first { it.id == selectedIds.value.first() })
                        clearSelection()
                    }
                ) {
                    onDelete(selectedIds.value)
                    clearSelection()
                }
            } else {
                NonActionModeToolbar(
                    scrolledColor,
                    navigationInteractionSource,
                    goBack,
                    scrollBehavior,
                    statusBarColor,
                    colorTransitionFraction,
                    contrastColor,
                    onAdd,
                    onImportBlockedNumbers,
                    onExportBlockedNumbers
                )
            }
        },
    ) {

        val state = rememberLazyListState()
        val autoScrollSpeed = remember { mutableFloatStateOf(0f) }
        LaunchedEffect(autoScrollSpeed.floatValue) {
            if (autoScrollSpeed.floatValue != 0f) {
                while (isActive) {
                    state.scrollBy(autoScrollSpeed.floatValue)
                    delay(10)
                }
            }
        }

        LazyColumn(
            state = state,
            modifier = Modifier.dragHandler(
                lazyListState = state,
                haptics = LocalHapticFeedback.current,
                selectedIds = selectedIds,
                autoScrollSpeed = autoScrollSpeed,
                autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() }
            )) {
            item(key = "unknown") {
                SettingsCheckBoxComponent(
                    title = if (isDialer) stringResource(id = R.string.block_not_stored_calls) else stringResource(id = R.string.block_not_stored_messages),
                    initialValue = isBlockUnknownSelected,
                    onChange = onBlockUnknownSelectedChange,
                    modifier = startingPadding,
                )
            }

            item(key = "hidden") {
                SettingsCheckBoxComponent(
                    title = if (isDialer) stringResource(id = R.string.block_hidden_calls) else stringResource(id = R.string.block_hidden_messages),
                    initialValue = isHiddenSelected,
                    onChange = onHiddenSelectedChange,
                    modifier = startingPadding,
                )
            }
            item(key = "divider") {
                SettingsHorizontalDivider()
            }
            when {
                !hasGivenPermissionToBlock -> {
                    noPermissionToBlock(setAsDefault = setAsDefault)
                }

                blockedNumbers.isEmpty() -> {
                    emptyBlockedNumbers(addABlockedNumber = onAdd)
                }

                hasGivenPermissionToBlock && blockedNumbers.isNotEmpty() -> {
                    items(blockedNumbers, key = { it.id }) { blockedNumber ->
                        val isSelected = selectedIds.value.contains(blockedNumber.id)
                        BlockedNumber(
                            modifier = Modifier
                                .semantics {
                                    if (!isInActionMode) {
                                        onLongClick("Select") {
                                            selectedIds.value += blockedNumber.id
                                            true
                                        }
                                    }
                                }
                                .ifTrue(isInActionMode) {
                                    Modifier.toggleable(
                                        value = isSelected,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onValueChange = {
                                            if (it) {
                                                selectedIds.value += blockedNumber.id
                                            } else {
                                                selectedIds.value -= blockedNumber.id
                                            }
                                        }
                                    )
                                }
                                .ifTrue(!isInActionMode) {
                                    Modifier.clickable {
                                        onEdit(blockedNumber)
                                    }
                                },
                            blockedNumber = blockedNumber,
                            onDelete = onDelete,
                            onCopy = onCopy,
                            isSelected = isSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedNumber(
    modifier: Modifier = Modifier,
    blockedNumber: BlockedNumber,
    onDelete: (Set<Long>) -> Unit,
    onCopy: (BlockedNumber) -> Unit,
    isSelected: Boolean
) {
    val rippleColor = if (isSystemInDarkTheme()) ripple_dark else ripple_light
    ListItem(
        modifier = modifier,
        headlineContent = {
            BlockedNumberHeadlineContent(blockedNumber = blockedNumber)
        },
        trailingContent = {
            BlockedNumberTrailingContent(onDelete = {
                onDelete(setOf(blockedNumber.id))
            }, onCopy = {
                onCopy(blockedNumber)
            })
        },
        colors = blockedNumberListItemColors(isSelected, rippleColor)
    )

}

@Composable
private fun blockedNumberListItemColors(
    isSelected: Boolean,
    rippleColor: Color
) = ListItemDefaults.colors(
    containerColor = if (isSelected) {
        rippleColor
    } else {
        MaterialTheme.colorScheme.surface
    }
)

@Composable
private fun BlockedNumberHeadlineContent(modifier: Modifier = Modifier, blockedNumber: BlockedNumber) {
    Text(
        text = blockedNumber.number,
        modifier = modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun BlockedNumberTrailingContent(modifier: Modifier = Modifier, onDelete: () -> Unit, onCopy: () -> Unit) {
    var isMenuVisible by remember { mutableStateOf(false) }
    val dismissMenu = remember {
        {
            isMenuVisible = false
        }
    }
    DropdownMenu(
        //https://github.com/JetBrains/compose-multiplatform/issues/1878 same in M3
        expanded = isMenuVisible,
        onDismissRequest = dismissMenu,
    ) {
        DropdownMenuItem(text = {
            Text(
                text = stringResource(id = R.string.copy_number_to_clipboard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )
        }, onClick = {
            onCopy()
            dismissMenu()
        })
        DropdownMenuItem(text = {
            Text(
                text = stringResource(id = R.string.delete),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )
        }, onClick = {
            onDelete()
            dismissMenu()
        })
    }
    Icon(Icons.Default.MoreVert, contentDescription = null,
        modifier = modifier.clickable {
            isMenuVisible = true
        })
}

@Composable
private fun ActionModeToolbar(
    selectedIdsCount: Int,
    blockedNumbersCount: Int,
    onBackClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val paddingValues = WindowInsets.navigationBars.asPaddingValues()
    val context = LocalContext.current
    val navigationIconInteractionSource = remember { MutableInteractionSource() }
    val baseConfig = remember {
        context.baseConfig
    }
    val bgColor = if (baseConfig.isUsingSystemTheme) {
        colorResource(R.color.you_contextual_status_bar_color)
    } else {
        Color.Black
    }
    val textColor by remember {
        derivedStateOf { Color(bgColor.toArgb().getContrastColor()) }
    }
    TopAppBar(
        title = {
            Text(text = "$selectedIdsCount/$blockedNumbersCount", letterSpacing = 8.sp, color = textColor)
        },
        navigationIcon = {
            SettingsNavigationIcon(navigationIconInteractionSource = navigationIconInteractionSource, goBack = onBackClick, iconColor = textColor)
        },
        actions = {
            ActionMenu(selectedIdsCount, onDelete, onCopy, textColor)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
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
private fun ActionMenu(
    selectedIdsCount: Int,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    iconColor: Color? = null
) {
    val actionMenus = remember(selectedIdsCount) {
        val delete =
            ActionItem(
                nameRes = R.string.delete,
                icon = Icons.Default.Delete,
                doAction = onDelete,
                overflowMode = OverflowMode.NEVER_OVERFLOW,
                iconColor = iconColor
            )

        val list = if (selectedIdsCount == 1) {
            listOf(
                ActionItem(
                    nameRes = R.string.copy,
                    icon = Icons.Default.ContentCopy,
                    doAction = onCopy,
                    overflowMode = OverflowMode.NEVER_OVERFLOW,
                    iconColor = iconColor
                ),
                delete
            )
        } else {
            listOf(delete)
        }
        list.toImmutableList()
    }
    ActionMenu(items = actionMenus, numIcons = if (selectedIdsCount == 1) 2 else 1, isMenuVisible = true, onMenuToggle = { })
}

@Composable
private fun NonActionModeToolbar(
    scrolledColor: Color,
    navigationInteractionSource: MutableInteractionSource,
    goBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    onAdd: () -> Unit,
    onImportBlockedNumbers: () -> Unit,
    onExportBlockedNumbers: () -> Unit
) {
    SettingsScaffoldTopBar(
        title = { scrolledTextColor ->
            Text(
                text = stringResource(id = R.string.manage_blocked_numbers),
                modifier = Modifier.padding(start = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = scrolledTextColor
            )
        },
        scrolledColor = scrolledColor,
        navigationIconInteractionSource = navigationInteractionSource,
        goBack = goBack,
        scrollBehavior = scrollBehavior,
        statusBarColor = statusBarColor,
        colorTransitionFraction = colorTransitionFraction,
        contrastColor = contrastColor,
        actions = {
            val actionMenus = remember {
                listOf(
                    ActionItem(R.string.add_a_blocked_number, icon = Icons.Filled.Add, doAction = onAdd),
                    ActionItem(R.string.import_blocked_numbers, doAction = onImportBlockedNumbers, overflowMode = OverflowMode.ALWAYS_OVERFLOW),
                    ActionItem(R.string.export_blocked_numbers, doAction = onExportBlockedNumbers, overflowMode = OverflowMode.ALWAYS_OVERFLOW),
                ).toImmutableList()
            }
            var isMenuVisible by remember { mutableStateOf(false) }
            ActionMenu(items = actionMenus, numIcons = 2, isMenuVisible = isMenuVisible, onMenuToggle = { isMenuVisible = it })
        }
    )
}


private fun LazyListScope.emptyBlockedNumbers(
    addABlockedNumber: () -> Unit
) {
    item {
        Text(
            text = stringResource(id = R.string.not_blocking_anyone),
            style = TextStyle(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        )
    }
    item {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable {
                addABlockedNumber()
            }) {
            Text(
                text = stringResource(id = R.string.add_a_blocked_number),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

    }
}

private fun LazyListScope.noPermissionToBlock(
    setAsDefault: () -> Unit
) {
    item {
        Text(
            text = stringResource(id = R.string.must_make_default_dialer),
            style = TextStyle(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        )
    }
    item {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { setAsDefault() }) {
            Text(
                text = stringResource(id = R.string.set_as_default),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}


@MyDevices
@Composable
private fun ManageBlockedNumbersScreenPreview(@PreviewParameter(BooleanPreviewParameterProvider::class) isDialer: Boolean) {
    AppThemeSurface {
        ManageBlockedNumbersScreen(
            goBack = {},
            onAdd = {},
            onImportBlockedNumbers = {},
            onExportBlockedNumbers = {},
            setAsDefault = {},
            isDialer = isDialer,
            hasGivenPermissionToBlock = !isDialer,
            isBlockUnknownSelected = false,
            onBlockUnknownSelectedChange = {},
            isHiddenSelected = false,
            onHiddenSelectedChange = {},
            blockedNumbers = listOf(
                BlockedNumber(id = 1, number = "000000000", normalizedNumber = "000000000", numberToCompare = "000000000"),
                BlockedNumber(id = 2, number = "111111111", normalizedNumber = "111111111", numberToCompare = "111111111"),
                BlockedNumber(id = 3, number = "5555555555", normalizedNumber = "5555555555", numberToCompare = "5555555555"),
                BlockedNumber(id = 4, number = "1234567890", normalizedNumber = "1234567890", numberToCompare = "1234567890"),
                BlockedNumber(id = 5, number = "9876543210", normalizedNumber = "9876543210", numberToCompare = "9876543210"),
                BlockedNumber(id = 6, number = "9998887777", normalizedNumber = "9998887777", numberToCompare = "9998887777"),
                BlockedNumber(id = 7, number = "2223334444", normalizedNumber = "2223334444", numberToCompare = "2223334444"),
                BlockedNumber(id = 8, number = "5552221111", normalizedNumber = "5552221111", numberToCompare = "5552221111")
            ).toImmutableList(),
            onDelete = {},
            onEdit = {}
        ) {}
    }
}
