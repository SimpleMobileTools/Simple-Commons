package com.simplemobiletools.commons.dialogs

import android.app.AlertDialog
import android.content.Context
import com.simplemobiletools.commons.R

/**
 * A simple dialog without any view, just a messageId, a positive button and optionally a negative button
 *
 * @param context has to be activity context to avoid some Theme.AppCompat issues
 * @param message the dialogs message, can be any String. If empty, messageId is used
 * @param messageId the dialogs messageId ID. Used only if message is empty
 * @param positive positive buttons text ID
 * @param negative negative buttons text ID (optional)
 * @param callback an anonymous function
 */
class ConfirmationDialog(context: Context, message: String = "", messageId: Int = R.string.smtco_proceed_with_deletion, positive: Int = R.string.smtco_yes,
                         negative: Int = R.string.smtco_no, val callback: () -> Unit) {
    var dialog: AlertDialog? = null

    init {
        val builder = AlertDialog.Builder(context)
                .setPositiveButton(positive, { dialog, which -> dialogConfirmed() })

        if (message.isEmpty())
            builder.setMessage(messageId)
        else
            builder.setMessage(message)

        if (negative != 0)
            builder.setNegativeButton(negative, null)

        dialog = builder.create()
        dialog!!.setCanceledOnTouchOutside(true)
        dialog!!.show()
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback.invoke()
    }
}
