package com.simplemobiletools.commons.compose.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme

@Composable
fun SettingsTitleTextComponent(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = SimpleTheme.colorScheme.primary,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Box(modifier = Modifier.padding(top = SimpleTheme.dimens.padding.extraLarge)) {
        Text(
            text = text.uppercase(),
            modifier = modifier
                .padding(horizontal = SimpleTheme.dimens.padding.small),
            color = color,
            fontSize = 14.sp,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

@MyDevices
@Composable
private fun SettingsTitleTextComponentPreview() = AppThemeSurface {
    SettingsTitleTextComponent(text = "Color customization")
}
