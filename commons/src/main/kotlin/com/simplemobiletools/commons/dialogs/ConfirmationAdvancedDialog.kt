package com.simplemobiletools.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.DialogMessageBinding
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff

// similar fo ConfirmationDialog, but has a callback for negative button too
class ConfirmationAdvancedDialog(
    activity: Activity, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
    negative: Int = R.string.no, val cancelOnTouchOutside: Boolean = true, val callback: (result: Boolean) -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogMessageBinding.inflate(activity.layoutInflater, null, false)
        view.message.text = message.ifEmpty { activity.resources.getString(messageId) }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> positivePressed() }

        if (negative != 0) {
            builder.setNegativeButton(negative) { _, _ -> negativePressed() }
        }

        if (!cancelOnTouchOutside) {
            builder.setOnCancelListener { negativePressed() }
        }

        builder.apply {
            activity.setupDialogStuff(view.root, this, cancelOnTouchOutside = cancelOnTouchOutside) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }

    private fun negativePressed() {
        dialog?.dismiss()
        callback(false)
    }
}
