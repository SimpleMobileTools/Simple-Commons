package com.simplemobiletools.commons.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getDialogBackgroundColor
import com.simplemobiletools.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.dialog_message.view.*

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
class ConfirmationDialog(context: Context, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
                         negative: Int = R.string.no, val callback: () -> Unit) {
    var dialog: AlertDialog? = null

    init {
        val baseConfig = BaseConfig.newInstance(context)
        val backgroundColor = baseConfig.backgroundColor
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null)
        view.message.text = if (message.isEmpty()) context.resources.getString(messageId) else message
        view.message.setTextColor(backgroundColor.getContrastColor())

        val builder = AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(positive, { dialog, which -> dialogConfirmed() })

        if (negative != 0)
            builder.setNegativeButton(negative, null)

        val primaryColor = baseConfig.primaryColor
        dialog = builder.create().apply {
            setCanceledOnTouchOutside(true)
            show()
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primaryColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(primaryColor)
            window.setBackgroundDrawable(context.getDialogBackgroundColor(backgroundColor))
        }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback.invoke()
    }
}
