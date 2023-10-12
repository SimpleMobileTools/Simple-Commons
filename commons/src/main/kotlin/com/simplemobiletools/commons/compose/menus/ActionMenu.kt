package com.simplemobiletools.commons.compose.menus

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.dialogBorder
import com.simplemobiletools.commons.compose.alert_dialog.dialogContainerColor
import com.simplemobiletools.commons.compose.components.SimpleDropDownMenuItem
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.Shapes
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Essentially a wrapper around a lambda function to give it a name and icon
 * akin to Android menu XML entries.
 * As an item on the action bar, the action will be displayed with an IconButton
 * with the given icon, if not null. Otherwise, the string from the name resource is used.
 * In overflow menu, item will always be displayed as text.
 * Original idea: https://gist.github.com/MachFour/369ebb56a66e2f583ebfb988dda2decf
 */
@Immutable
data class ActionItem(
    @StringRes
    val nameRes: Int,
    val icon: ImageVector? = null,
    val overflowMode: OverflowMode = OverflowMode.IF_NECESSARY,
    val doAction: () -> Unit,
    val iconColor: Color? = null
) {
    // allow 'calling' the action like a function
    operator fun invoke() = doAction()
}

/**
 * Whether action items are allowed to overflow into a dropdown menu - or NOT SHOWN to hide
 */
@Immutable
enum class OverflowMode {
    NEVER_OVERFLOW, IF_NECESSARY, ALWAYS_OVERFLOW, NOT_SHOWN
}

/**
 * Best if combined with [RowScope] or used within a [Row] or [LazyRow]
 */
@Composable
fun ActionMenu(
    items: ImmutableList<ActionItem>,
    numIcons: Int = 2, // includes overflow menu icon; may be overridden by NEVER_OVERFLOW
    isMenuVisible: Boolean,
    iconsColor: Color? = null,
    onMenuToggle: (isVisible: Boolean) -> Unit
) {
    if (items.isEmpty()) {
        return
    }
    // decide how many action items to show as icons
    val (appbarActions, overflowActions) = remember(items, numIcons) {
        separateIntoIconAndOverflow(items, numIcons)
    }
    for (item in appbarActions) {
        key(item.hashCode()) {
            val name = stringResource(item.nameRes)
            if (item.icon != null) {
                val iconButtonColor = when {
                    iconsColor != null -> iconsColor
                    item.iconColor != null -> item.iconColor
                    else -> LocalContentColor.current
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(spacingBetweenTooltipAndAnchor = 18.dp),
                    tooltip = {
                        PlainTooltip(shape = Shapes.extraLarge) {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(SimpleTheme.dimens.padding.medium),
                            )
                        }
                    },
                    state = rememberTooltipState(),
                ) {
                    ActionIconButton(
                        onClick = item.doAction,
                        contentColor = iconButtonColor,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = name
                        )
                    }
                }
            } else {
                SimpleDropDownMenuItem(onClick = item.doAction, text = name)
            }
        }
    }

    if (overflowActions.isNotEmpty()) {
        TooltipBox(
            tooltip = {
                PlainTooltip(shape = Shapes.extraLarge) {
                    Text(
                        text = stringResource(id = R.string.more_options),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(SimpleTheme.dimens.padding.medium),
                    )
                }
            },
            state = rememberTooltipState(),
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(spacingBetweenTooltipAndAnchor = 18.dp),
        ) {
            ActionIconButton(
                onClick = { onMenuToggle(true) },
                contentColor = iconsColor ?: LocalContentColor.current,
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more_options))
            }
        }
        DropdownMenu(
            modifier = Modifier
                .background(dialogContainerColor)
                .dialogBorder,
            expanded = isMenuVisible,
            onDismissRequest = { onMenuToggle(false) },
        ) {
            for (item in overflowActions) {
                key(item.hashCode()) {
                    SimpleDropDownMenuItem(text = item.nameRes, onClick = {
                        onMenuToggle(false)
                        item.doAction()
                    })
                }
            }
        }
    }
}

@Composable
internal fun ActionIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    contentColor: Color,
    content: @Composable () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .clip(RoundedCornerShape(50))
            .combinedClickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = false,
                    radius = 40.dp / 2
                ),
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

private fun separateIntoIconAndOverflow(
    items: List<ActionItem>,
    numIcons: Int
): Pair<List<ActionItem>, List<ActionItem>> {
    var (iconCount, overflowCount, preferIconCount) = Triple(0, 0, 0)
    for (item in items) {
        when (item.overflowMode) {
            OverflowMode.NEVER_OVERFLOW -> iconCount++
            OverflowMode.IF_NECESSARY -> preferIconCount++
            OverflowMode.ALWAYS_OVERFLOW -> overflowCount++
            OverflowMode.NOT_SHOWN -> {}
        }
    }

    val needsOverflow = ((iconCount + preferIconCount) > numIcons) || (overflowCount > 0)
    val actionIconSpace = numIcons - (if (needsOverflow) 1 else 0)

    val iconActions = mutableListOf<ActionItem>()
    val overflowActions = mutableListOf<ActionItem>()

    var iconsAvailableBeforeOverflow = actionIconSpace - iconCount
    for (item in items) {
        when (item.overflowMode) {
            OverflowMode.NEVER_OVERFLOW -> {
                iconActions.add(item)
            }

            OverflowMode.ALWAYS_OVERFLOW -> {
                overflowActions.add(item)
            }

            OverflowMode.IF_NECESSARY -> {
                if (iconsAvailableBeforeOverflow > 0) {
                    iconActions.add(item)
                    iconsAvailableBeforeOverflow--
                } else {
                    overflowActions.add(item)
                }
            }

            OverflowMode.NOT_SHOWN -> {
                // skip
            }
        }
    }
    return Pair(iconActions, overflowActions)
}

@MyDevices
@Composable
private fun ActionMenuPreview() {
    AppThemeSurface {
        val actionMenus = remember {
            listOf(
                ActionItem(R.string.add_a_blocked_number, icon = Icons.Filled.Add, doAction = { }),
                ActionItem(R.string.import_blocked_numbers, doAction = {}, overflowMode = OverflowMode.ALWAYS_OVERFLOW),
                ActionItem(R.string.export_blocked_numbers, doAction = { }, overflowMode = OverflowMode.ALWAYS_OVERFLOW),
            ).toImmutableList()
        }
        ActionMenu(items = actionMenus, numIcons = 2, isMenuVisible = true, onMenuToggle = { }, iconsColor = Color.Black)
    }
}
