package com.simplemobiletools.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_message.view.*

// similar fo ConfirmationDialog, but has a callback for negative button too
class ConfirmationAdvancedDialog(
    activity: Activity, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
    negative: Int, val callback: (result: Boolean) -> Unit
) {
    var dialog: AlertDialog

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_message, null)
        view.message.text = if (message.isEmpty()) activity.resources.getString(messageId) else message

        val builder = AlertDialog.Builder(activity)
            .setPositiveButton(positive) { dialog, which -> positivePressed() }
            .setOnCancelListener { negativePressed() }

        if (negative != 0) {
            builder.setNegativeButton(negative) { dialog, which -> negativePressed() }
        }

        dialog = builder.create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    private fun positivePressed() {
        dialog.dismiss()
        callback(true)
    }

    private fun negativePressed() {
        dialog.dismiss()
        callback(false)
    }
}
