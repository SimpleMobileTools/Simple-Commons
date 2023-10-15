package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.*
import com.simplemobiletools.commons.compose.components.LinkifyTextComponent
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.databinding.DialogTextviewBinding
import com.simplemobiletools.commons.extensions.*

class AppSideloadedDialog(val activity: Activity, val callback: () -> Unit) {
    private var dialog: AlertDialog? = null
    private val url = "https://play.google.com/store/apps/details?id=${activity.getStringsPackageName()}"

    init {
        val view = DialogTextviewBinding.inflate(activity.layoutInflater, null, false).apply {
            val text = String.format(activity.getString(R.string.sideloaded_app), url)
            textView.text = Html.fromHtml(text)
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel) { _, _ -> negativePressed() }
            .setPositiveButton(R.string.download, null)
            .setOnCancelListener { negativePressed() }
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.app_corrupt) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        downloadApp()
                    }
                }
            }
    }

    private fun downloadApp() {
        activity.launchViewIntent(url)
    }

    private fun negativePressed() {
        dialog?.dismiss()
        callback()
    }
}

@Composable
fun AppSideLoadedAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    onDownloadClick: (url: String) -> Unit,
    onCancelClick: () -> Unit
) {
    val context = LocalContext.current
    val url = remember { "https://play.google.com/store/apps/details?id=${context.getStringsPackageName()}" }
    AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder,
        onDismissRequest = alertDialogState::hide,
        confirmButton = {
            TextButton(onClick = {
                alertDialogState.hide()
                onDownloadClick(url)
            }) {
                Text(text = stringResource(id = R.string.download))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                alertDialogState.hide()
                onCancelClick()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        shape = dialogShape,
        text = {
            val source = stringResource(id = R.string.sideloaded_app, url)
            LinkifyTextComponent(fontSize = 16.sp, removeUnderlines = false) {
                source.fromHtml()
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.app_corrupt),
                color = dialogTextColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        tonalElevation = dialogElevation
    )
}

@Composable
@MyDevices
private fun AppSideLoadedAlertDialogPreview() {
    AppThemeSurface {
        AppSideLoadedAlertDialog(alertDialogState = rememberAlertDialogState(), onDownloadClick = {}) {}
    }
}
