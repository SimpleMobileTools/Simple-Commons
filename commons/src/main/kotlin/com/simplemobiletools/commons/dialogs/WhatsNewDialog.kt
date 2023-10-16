package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.*
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.databinding.DialogWhatsNewBinding
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.Release
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class WhatsNewDialog(val activity: Activity, val releases: List<Release>) {
    init {
        val view = DialogWhatsNewBinding.inflate(LayoutInflater.from(activity), null, false)
        view.whatsNewContent.text = getNewReleases()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.whats_new, cancelOnTouchOutside = false)
            }
    }

    private fun getNewReleases(): String {
        val sb = StringBuilder()

        releases.forEach {
            val parts = activity.getString(it.textId).split("\n").map(String::trim)
            parts.forEach {
                sb.append("- $it\n")
            }
        }

        return sb.toString()
    }
}

@Composable
fun WhatsNewAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    releases: ImmutableList<Release>
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        containerColor = dialogContainerColor,
        shape = dialogShape,
        tonalElevation = dialogElevation,
        modifier = modifier.dialogBorder,
        title = {
            Text(
                text = stringResource(id = R.string.whats_new),
                color = dialogTextColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = getNewReleases(releases), color = dialogTextColor)
                SettingsHorizontalDivider()
                Text(
                    text = stringResource(id = R.string.whats_new_disclaimer),
                    color = dialogTextColor.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    )
}

@Composable
private fun getNewReleases(releases: ImmutableList<Release>): String {
    val sb = StringBuilder()

    releases.forEach { release ->
        val parts = stringResource(release.textId).split("\n").map(String::trim)
        parts.forEach {
            sb.append("- $it\n")
        }
    }

    return sb.toString()
}

@MyDevices
@Composable
private fun WhatsNewAlertDialogPreview() {
    AppThemeSurface {
        WhatsNewAlertDialog(
            alertDialogState = rememberAlertDialogState(), releases =
            listOf(
                Release(14, R.string.temporarily_show_excluded),
                Release(3, R.string.temporarily_show_hidden)
            ).toImmutableList()
        )
    }
}
