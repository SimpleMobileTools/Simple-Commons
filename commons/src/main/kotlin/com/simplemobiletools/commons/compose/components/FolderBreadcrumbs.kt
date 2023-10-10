package com.simplemobiletools.commons.compose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun FolderBreadcrumbs(
    modifier: Modifier = Modifier,
    items: ImmutableList<FileDirItem>,
    fontSize: TextUnit = 14.sp,
    onBreadcrumbClicked: (Int, FileDirItem) -> Unit,
) {
    Row(
        modifier = modifier
    ) {
        if (items.size > 0) {
            val rootItem = items.first()
            RootBreadcrumb(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = rootItem.name,
                activated = items.size == 1,
                fontSize = fontSize,
                onClick = { onBreadcrumbClicked(0, rootItem) }
            )

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.horizontalScroll(scrollState).align(Alignment.CenterVertically),
            ) {
                for ((index, item) in items.withIndex().drop(1)) {
                    key(item.path) {
                        Breadcrumb(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = "> ${item.name}",
                            activated = items.size == index + 1,
                            fontSize = fontSize,
                            onClick = { onBreadcrumbClicked(index, item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RootBreadcrumb(
    modifier: Modifier = Modifier,
    text: String,
    activated: Boolean,
    fontSize: TextUnit,
    onClick: () -> Unit
) {
    Text(
        modifier = modifier
            .alpha(if (activated) 1f else 0.6f)
            .padding(start = SimpleTheme.dimens.padding.extraSmall)
            .border(1.dp, SimpleTheme.colorScheme.onBackground)
            .requiredSizeIn(minHeight = 48.dp)
            .padding(
                horizontal = SimpleTheme.dimens.padding.small,
                vertical = SimpleTheme.dimens.padding.medium,
            )
            .clickable(onClick = onClick)
            .wrapContentSize(unbounded = true),
        text = text,
        textAlign = TextAlign.Justify,
        fontSize = fontSize,
    )
}

@Composable
private fun Breadcrumb(
    modifier: Modifier = Modifier,
    text: String,
    activated: Boolean,
    fontSize: TextUnit,
    onClick: () -> Unit
) {
    Text(
        modifier = modifier
            .alpha(if (activated) 1f else 0.6f)
            .requiredSizeIn(minHeight = 48.dp)
            .padding(
                horizontal = SimpleTheme.dimens.padding.small,
                vertical = SimpleTheme.dimens.padding.medium,
            )
            .clickable(onClick = onClick)
            .wrapContentSize(unbounded = true),
        text = text,
        textAlign = TextAlign.Center,
        fontSize = fontSize
    )
}

@Composable
@MyDevices
private fun FolderBreadcrumbsPreview() {
    AppThemeSurface {
        FolderBreadcrumbs(
            items = listOf(
                FileDirItem("Internal", "Internal"),
                FileDirItem("Internal/folder", "folder"),
                FileDirItem("Internal/folder2", "folder2"),
            ).toImmutableList(),
            onBreadcrumbClicked = { _, _ -> }
        )
    }
}
