package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff

class DonateDialog(val activity: Activity) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_donate, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }
}
