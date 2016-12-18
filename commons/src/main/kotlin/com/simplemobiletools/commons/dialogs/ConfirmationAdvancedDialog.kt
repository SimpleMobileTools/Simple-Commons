package com.simplemobiletools.commons.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getDialogBackgroundColor
import com.simplemobiletools.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.dialog_message.view.*

class ConfirmationAdvancedDialog(context: Context, message: String = "", messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
                                 negative: Int, val listener: Listener) {
    var dialog: AlertDialog? = null

    init {
        val baseConfig = BaseConfig.newInstance(context)
        val backgroundColor = baseConfig.backgroundColor
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null)
        view.message.text = if (message.isEmpty()) context.resources.getString(messageId) else message
        view.message.setTextColor(backgroundColor.getContrastColor())

        val builder = AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(positive, { dialog, which -> positivePressed() })
                .setNegativeButton(negative, { dialog, which -> negativePressed() })

        val primaryColor = baseConfig.primaryColor
        dialog = builder.create().apply {
            setCanceledOnTouchOutside(true)
            show()
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primaryColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(primaryColor)
            window.setBackgroundDrawable(context.getDialogBackgroundColor(backgroundColor))
        }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        listener.onPositive()
    }

    private fun negativePressed() {
        dialog?.dismiss()
        listener.onNegative()
    }

    interface Listener {
        fun onPositive()

        fun onNegative()
    }
}
