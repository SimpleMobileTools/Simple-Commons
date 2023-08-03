package com.simplemobiletools.commons.compose.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.BooleanPreviewParameterProvider
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

@Composable
fun SettingsListItem(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int,
    isImage: Boolean = false,
    click: (() -> Unit)? = null,
    tint: Color? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier)
            )
        },
        leadingContent = {
            val imageSize = Modifier.size(48.dp)
                .padding(8.dp)
            if (isImage) {
                if (tint != null) {
                    Image(
                        modifier = imageSize,
                        painter = painterResource(id = icon),
                        contentDescription = text,
                        colorFilter = ColorFilter.tint(tint)
                    )
                } else {
                    Image(
                        modifier = imageSize,
                        painter = painterResource(id = icon),
                        contentDescription = text,
                    )
                }
            } else {
                if (tint != null) {
                    Icon(
                        modifier = imageSize,
                        painter = painterResource(id = icon),
                        contentDescription = text,
                        tint = tint
                    )
                } else {
                    Icon(
                        modifier = imageSize,
                        painter = painterResource(id = icon),
                        contentDescription = text,
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = click != null) { click?.invoke() }
    )
}

@MyDevices
@Composable
private fun SettingsListItem(@PreviewParameter(BooleanPreviewParameterProvider::class) isImage: Boolean) {
    AppThemeSurface {
        SettingsListItem(
            click = {},
            text = "Simple Mobile Tools",
            icon = if (isImage) R.drawable.ic_telegram_vector else R.drawable.ic_dollar_vector,
            isImage = isImage
        )
    }
}
