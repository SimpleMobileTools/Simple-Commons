package com.simplemobiletools.commons.compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

private val dropDownPaddings = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)

@Composable
fun SimpleDropDownMenuItem(
    modifier: Modifier = Modifier,
    @StringRes text: Int,
    onClick: () -> Unit
) = SimpleDropDownMenuItem(modifier, text = stringResource(id = text), onClick)

@Composable
fun SimpleDropDownMenuItem(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) =
    Box(modifier = modifier
        .fillMaxWidth()
        .clickable(onClickLabel = text) {
            onClick()
        }
        .then(dropDownPaddings)) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
        )
    }

@Composable
fun SimpleDropDownMenuItem(
    modifier: Modifier = Modifier,
    text: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit
) =
    Box(modifier = modifier
        .fillMaxWidth()
        .clickable {
            onClick()
        }
        .then(dropDownPaddings)) {
        text()
    }


@MyDevices
@Composable
private fun SimpleDropDownMenuItemPreview() {
    AppThemeSurface {
        SimpleDropDownMenuItem(text = com.simplemobiletools.commons.R.string.copy, onClick = {})
    }
}
