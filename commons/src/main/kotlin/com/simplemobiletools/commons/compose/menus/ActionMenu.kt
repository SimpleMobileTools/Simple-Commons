package com.simplemobiletools.commons.compose.menus

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.compose.components.SimpleDropDownMenuItem
import kotlinx.collections.immutable.ImmutableList

/**
 * Essentially a wrapper around a lambda function to give it a name and icon
 * akin to Android menu XML entries.
 * As an item on the action bar, the action will be displayed with an IconButton
 * with the given icon, if not null. Otherwise, the string from the name resource is used.
 * In overflow menu, item will always be displayed as text.
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
                IconButton(onClick = item.doAction) {
                    if (item.iconColor != null) {
                        Icon(item.icon, name, tint = item.iconColor)
                    } else {
                        Icon(item.icon, name)
                    }
                }
            } else {
                SimpleDropDownMenuItem(onClick = item.doAction, text = name)
            }
        }
    }

    if (overflowActions.isNotEmpty()) {
        IconButton(onClick = { onMenuToggle(true) }) {
            Icon(Icons.Default.MoreVert, stringResource(id = com.simplemobiletools.commons.R.string.more_info))
        }
        DropdownMenu(
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
