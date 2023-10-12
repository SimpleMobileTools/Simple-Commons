package com.simplemobiletools.commons.compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme

private val dropDownPaddings = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)

@Composable
fun SimpleDropDownMenuItem(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    indication: Indication? = LocalIndication.current,
    @StringRes text: Int,
    onClick: () -> Unit
) = SimpleDropDownMenuItem(modifier = modifier, text = stringResource(id = text), onClick = onClick, interactionSource = interactionSource, indication = indication)

@Composable
fun SimpleDropDownMenuItem(
    modifier: Modifier = Modifier,
    text: String,
    interactionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    indication: Indication? = LocalIndication.current,
    onClick: () -> Unit
) =
    SimpleDropDownMenuItem(
        modifier = modifier,
        interactionSource = interactionSource,
        indication = indication,
        onClick = onClick,
        text = {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth(),
                color = SimpleTheme.colorScheme.onSurface
            )
        }
    )

@Composable
fun SimpleDropDownMenuItem(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    indication: Indication? = LocalIndication.current,
    text: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit
) =
    Box(modifier = modifier
        .fillMaxWidth()
        .clickable(interactionSource = interactionSource, indication = indication, onClick = onClick)
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
