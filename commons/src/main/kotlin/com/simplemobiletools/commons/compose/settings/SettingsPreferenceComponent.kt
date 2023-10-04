package com.simplemobiletools.commons.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.preferenceLabelColor
import com.simplemobiletools.commons.compose.theme.preferenceValueColor

@Composable
fun SettingsPreferenceComponent(
    modifier: Modifier = Modifier,
    label: String,
    value: String? = null,
    isPreferenceEnabled: Boolean = true,
    doOnPreferenceLongClick: (() -> Unit)? = null,
    doOnPreferenceClick: (() -> Unit)? = null,
    preferenceValueColor: Color = preferenceValueColor(isEnabled = isPreferenceEnabled),
    preferenceLabelColor: Color = preferenceLabelColor(isEnabled = isPreferenceEnabled)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = isPreferenceEnabled,
                onClick = { doOnPreferenceClick?.invoke() },
                onLongClick = { doOnPreferenceLongClick?.invoke() },
            )
            .padding(20.dp)
            .then(modifier),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            color = preferenceLabelColor,
            fontSize = 14.sp
        )
        AnimatedVisibility(visible = !value.isNullOrBlank()) {
            Text(
                text = value.toString(),
                modifier = Modifier
                    .fillMaxWidth(),
                color = preferenceValueColor,
                fontSize = 14.sp
            )
        }
    }
}

@MyDevices
@Composable
private fun SettingsPreferencePreview() {
    AppThemeSurface {
        SettingsPreferenceComponent(
            label = stringResource(id = R.string.language),
            value = stringResource(id = R.string.translation_english),
            isPreferenceEnabled = true,
        )
    }
}
