package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.*
import com.simplemobiletools.commons.compose.components.LinkifyTextComponent
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.composeDonateIntent
import com.simplemobiletools.commons.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.databinding.DialogFeatureLockedBinding
import com.simplemobiletools.commons.extensions.*

class FeatureLockedDialog(val activity: Activity, val callback: () -> Unit) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogFeatureLockedBinding.inflate(activity.layoutInflater, null, false)
        view.featureLockedImage.applyColorFilter(activity.getProperTextColor())

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.purchase, null)
            .setNegativeButton(R.string.later) { _, _ -> dismissDialog() }
            .setOnDismissListener { dismissDialog() }
            .apply {
                activity.setupDialogStuff(view.root, this, cancelOnTouchOutside = false) { alertDialog ->
                    dialog = alertDialog
                    view.featureLockedDescription.text = Html.fromHtml(activity.getString(R.string.features_locked))
                    view.featureLockedDescription.movementMethod = LinkMovementMethod.getInstance()

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        activity.launchPurchaseThankYouIntent()
                    }
                }
            }
    }

    fun dismissDialog() {
        dialog?.dismiss()
        callback()
    }
}

@Composable
fun FeatureLockedAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    cancelCallback: () -> Unit
) {
    val donateIntent = composeDonateIntent()
    androidx.compose.material3.AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
        onDismissRequest = cancelCallback,
        shape = dialogShape,
        tonalElevation = dialogElevation,
        dismissButton = {
            TextButton(onClick = {
                cancelCallback()
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.later))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                donateIntent()
            }) {
                Text(text = stringResource(id = R.string.purchase))
            }
        },
        title = {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .size(SimpleTheme.dimens.icon.large)
                        .clickable(
                            indication = null,
                            interactionSource = rememberMutableInteractionSource(),
                            onClick = {
                                donateIntent()
                            }
                        ),
                    colorFilter = ColorFilter.tint(dialogTextColor)
                )
            }
        },
        text = {
            val source = stringResource(id = R.string.features_locked)
            LinkifyTextComponent(
                fontSize = 16.sp,
                removeUnderlines = false,
                modifier = Modifier.fillMaxWidth(),
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            ) {
                source.fromHtml()
            }
        }
    )
}

@Composable
@MyDevices
private fun FeatureLockedAlertDialogPreview() {
    AppThemeSurface {
        FeatureLockedAlertDialog(alertDialogState = rememberAlertDialogState()) {

        }
    }
}
