package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R

class WhatsNewDialog(val activity: Activity) {
    var dialog: AlertDialog? = null

    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_whats_new, null)
        val builder = AlertDialog.Builder(activity)
                .setTitle(R.string.whats_new)
                .setView(view)
                .setPositiveButton(R.string.ok, null)

        dialog = builder.create()
        dialog!!.setCanceledOnTouchOutside(true)
        dialog!!.show()
    }
}
