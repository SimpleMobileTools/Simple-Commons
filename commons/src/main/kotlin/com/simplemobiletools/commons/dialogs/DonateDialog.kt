package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_donate.view.*

class DonateDialog(val activity: Activity) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_donate, null).apply {
            donate.movementMethod = LinkMovementMethod.getInstance()
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.purchase, { dialog, which -> activity.launchViewIntent(R.string.thank_you_url) })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }
}
