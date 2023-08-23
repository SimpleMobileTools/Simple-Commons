package com.simplemobiletools.commons.compose.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

@Composable
fun SettingsTitleTextComponent(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
        Text(
            text = text.uppercase(),
            modifier = modifier
                .padding(horizontal = 4.dp),
            color = color,
            fontSize = with(LocalDensity.current) {
                dimensionResource(id = com.simplemobiletools.commons.R.dimen.normal_text_size).toSp()
            },
        )
    }
}

@MyDevices
@Composable
private fun SettingsTitleTextComponentPreview() = AppThemeSurface {
    SettingsTitleTextComponent(text = "Color customization")
}
