package com.simplemobiletools.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_message.view.message

class PermissionRequiredDialog(
    activity: Activity,
    message: String = "",
    messageId: Int = R.string.no_permission,
    val cancelOnTouchOutside: Boolean = true,
    dialogTitle: String = "",
    dialogTitleId: Int = R.string.permission_required,
    val callback: () -> Unit,
) {
    private var dialog: AlertDialog? = null
    init {
        val title: String = if (dialogTitle.isEmpty()) {
            activity.getString(dialogTitleId)
        } else {
            dialogTitle
        }

        val view = activity.layoutInflater.inflate(R.layout.dialog_message, null)
        view.message.text = if (message.isEmpty()) {
            activity.getString(messageId)
        } else {
            message
        }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.grant_permission) { dialog, which -> dialogConfirmed() }

        builder.setNegativeButton(R.string.cancel, null)

        builder.apply {
            activity.setupDialogStuff(view, this, titleText = title, cancelOnTouchOutside = cancelOnTouchOutside) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}
