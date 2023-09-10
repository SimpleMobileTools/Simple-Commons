package com.simplemobiletools.commons.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.components.SimpleDropDownMenuItem
import com.simplemobiletools.commons.compose.extensions.*
import com.simplemobiletools.commons.compose.menus.ActionItem
import com.simplemobiletools.commons.compose.menus.ActionMenu
import com.simplemobiletools.commons.compose.menus.OverflowMode
import com.simplemobiletools.commons.compose.settings.SettingsCheckBoxComponent
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.settings.scaffold.*
import com.simplemobiletools.commons.compose.system_ui_controller.rememberSystemUiController
import com.simplemobiletools.commons.compose.theme.*
import com.simplemobiletools.commons.compose.theme.model.Theme
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
    onCopy: (BlockedNumber) -> Unit,
) {
    val startingPadding = remember { Modifier.padding(horizontal = 4.dp) }
    val selectedIds: MutableState<Set<Long>> = rememberSaveable { mutableStateOf(emptySet()) }
    val isInActionMode by remember { derivedStateOf { selectedIds.value.isNotEmpty() } }
    val clearSelection = remember {
        { selectedIds.value = emptySet() }
    }
    BackHandler(isInActionMode) {
        clearSelection()
    }
    TransparentSystemBars(darkIcons = MaterialTheme.colorScheme.primaryContainer.isLitWell())
    SettingsLazyScaffold(
        customTopBar = { scrolledColor: Color,
                         navigationInteractionSource: MutableInteractionSource,
                         scrollBehavior: TopAppBarScrollBehavior,
                         statusBarColor: Int,
                         colorTransitionFraction: Float,
                         contrastColor: Color ->
            Column {
                if (isInActionMode) {
                    ActionModeToolbar(
                        selectedIdsCount = selectedIds.value.count(),
                        blockedNumbersCount = blockedNumbers.count(),
                        onBackClick = clearSelection,
                        onCopy = {
                            onCopy(blockedNumbers.first { it.id == selectedIds.value.first() })
                            clearSelection()
                        },
                        onDelete = {
                            onDelete(selectedIds.value)
                            clearSelection()
                        },
                        onSelectAll = {
                            selectedIds.value = blockedNumbers.map { it.id }.toSet()
                        }
                    )
                } else {
                    NonActionModeToolbar(
                        scrolledColor = scrolledColor,
                        navigationInteractionSource = navigationInteractionSource,
                        goBack = goBack,
                        scrollBehavior = scrollBehavior,
                        statusBarColor = statusBarColor,
                        colorTransitionFraction = colorTransitionFraction,
                        contrastColor = contrastColor,
                        onAdd = onAdd,
                        onImportBlockedNumbers = onImportBlockedNumbers,
                        onExportBlockedNumbers = onExportBlockedNumbers
                    )
                }
                SettingsCheckBoxComponent(
                    title = if (isDialer) stringResource(id = R.string.block_not_stored_calls) else stringResource(id = R.string.block_not_stored_messages),
                    initialValue = isBlockUnknownSelected,
                    onChange = onBlockUnknownSelectedChange,
                    modifier = startingPadding.then(Modifier.topAppBarPaddings()),
                )
                SettingsCheckBoxComponent(
                    title = if (isDialer) stringResource(id = R.string.block_hidden_calls) else stringResource(id = R.string.block_hidden_messages),
                    initialValue = isHiddenSelected,
                    onChange = onHiddenSelectedChange,
                    modifier = startingPadding.then(Modifier.topAppBarPaddings()),
                )
                SettingsHorizontalDivider(modifier = Modifier.topAppBarPaddings())
            }
        },
    ) { paddingValues ->
        val state = rememberCanPerformVerticalScrollLazyListState()
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
                autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() },
            ),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
        ) {
            when {
                !hasGivenPermissionToBlock -> {
                    noPermissionToBlock(setAsDefault = setAsDefault)
                }

                blockedNumbers.isEmpty() -> {
                    emptyBlockedNumbers(addABlockedNumber = onAdd)
                }

                hasGivenPermissionToBlock && blockedNumbers.isNotEmpty() -> {
                    items(blockedNumbers, key = { blockedNumber -> blockedNumber.id }) { blockedNumber ->
                        val isSelected = selectedIds.value.contains(blockedNumber.id)
                        BlockedNumber(
                            modifier = Modifier
                                .animateItemPlacement()
                                .semantics {
                                    if (!isInActionMode) {
                                        onLongClick("select") {
                                            selectedIds.value += blockedNumber.id
                                            true
                                        }
                                    }
                                }
                                .ifTrue(!isInActionMode) {
                                    Modifier.clickable {
                                        onEdit(blockedNumber)
                                    }
                                }
                                .ifTrue(isInActionMode) {
                                    Modifier.toggleable(
                                        value = isSelected,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onValueChange = {
                                            if (isSelected) {
                                                selectedIds.value += blockedNumber.id
                                            } else {
                                                selectedIds.value -= blockedNumber.id
                                            }
                                        }
                                    )
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
        colors = blockedNumberListItemColors(
            isSelected = isSelected
        )
    )
}

@Composable
private fun blockedNumberListItemColors(
    isSelected: Boolean
) = ListItemDefaults.colors(
    containerColor = if (isSelected) {
        if (LocalTheme.current is Theme.SystemDefaultMaterialYou) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        }
    } else {
        MaterialTheme.colorScheme.surface
    },
    trailingIconColor = iconsColor
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
        modifier = modifier
    ) {
        SimpleDropDownMenuItem(onClick = {
            onCopy()
            dismissMenu()
        }, text = {
            Text(
                text = stringResource(id = R.string.copy_number_to_clipboard),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        })
        SimpleDropDownMenuItem(onClick = {
            onDelete()
            dismissMenu()
        }, text = {
            Text(
                text = stringResource(id = R.string.delete),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        })
    }
    IconButton(onClick = {
        isMenuVisible = true
    }) {
        Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more_info), tint = iconsColor)
    }
}

@Composable
private fun ActionModeToolbar(
    modifier: Modifier = Modifier,
    selectedIdsCount: Int,
    blockedNumbersCount: Int,
    onBackClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val wasDarkIcons = rememberSaveable { systemUiController.statusBarDarkContentEnabled }
    val navigationIconInteractionSource = remember { MutableInteractionSource() }
    val bgColor = actionModeBgColor()
    val textColor by remember {
        derivedStateOf { Color(bgColor.toArgb().getContrastColor()) }
    }
    DisposableEffect(systemUiController, bgColor) {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = bgColor.isLitWell())
        onDispose {
            systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = wasDarkIcons)
        }
    }
    TopAppBar(
        title = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        if (selectedIdsCount == blockedNumbersCount) {
                            onBackClick()
                        } else {
                            onSelectAll()
                        }
                    }
                    .padding(horizontal = 18.dp), contentAlignment = Alignment.Center
            ) {
                Text(text = "$selectedIdsCount / $blockedNumbersCount", color = textColor)
            }

        },
        navigationIcon = {
            SettingsNavigationIcon(navigationIconInteractionSource = navigationIconInteractionSource, goBack = onBackClick, iconColor = textColor)
        },
        actions = {
            BlockedNumberActionMenu(selectedIdsCount = selectedIdsCount, onDelete = onDelete, onCopy = onCopy, iconColor = textColor)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bgColor
        ),
        modifier = modifier.topAppBarPaddings(),
        windowInsets = topAppBarInsets()
    )
}

@Composable
@ReadOnlyComposable
private fun actionModeBgColor(): Color =
    if (LocalTheme.current is Theme.SystemDefaultMaterialYou) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        actionModeColor
    }


@Composable
private fun BlockedNumberActionMenu(
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
    ActionMenu(items = actionMenus, numIcons = if (selectedIdsCount == 1) 2 else 1, isMenuVisible = true, onMenuToggle = { }, iconsColor = iconColor)
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
            ActionMenu(items = actionMenus, numIcons = 2, isMenuVisible = isMenuVisible, onMenuToggle = { isMenuVisible = it }, iconsColor = scrolledColor)
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
        Box(
            modifier = Modifier
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { addABlockedNumber() }) {
                Text(
                    text = stringResource(id = R.string.add_a_blocked_number),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
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
        Box(
            modifier = Modifier
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { setAsDefault() }) {
                Text(
                    text = stringResource(id = R.string.set_as_default),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
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
