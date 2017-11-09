package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_message.view.*

class ConfirmationAdvancedDialog(context: Context, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
                                 negative: Int, val callback: (result: Boolean) -> Unit) {
    var dialog: AlertDialog

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null)
        view.message.text = if (message.isEmpty()) context.resources.getString(messageId) else message

        dialog = AlertDialog.Builder(context)
                .setPositiveButton(positive, { dialog, which -> positivePressed() })
                .setNegativeButton(negative, { dialog, which -> negativePressed() })
                .create().apply {
            context.setupDialogStuff(view, this)
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
