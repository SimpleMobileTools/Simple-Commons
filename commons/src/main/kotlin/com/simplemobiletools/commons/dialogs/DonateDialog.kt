package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff

class DonateDialog(val activity: Activity) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_donate, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.donate, { dialog, which -> activity.launchViewIntent(R.string.donate_url) })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }
}
