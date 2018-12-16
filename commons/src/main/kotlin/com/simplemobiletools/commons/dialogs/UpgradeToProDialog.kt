package com.simplemobiletools.commons.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.launchUpgradeToProIntent
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_upgrade_to_pro.view.*

class UpgradeToProDialog(val activity: AppCompatActivity) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_upgrade_to_pro, null).apply {
            upgrade_to_pro.text = activity.getString(R.string.upgrade_to_pro_long)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.upgrade) { dialog, which -> upgradeApp() }
                .setNeutralButton(R.string.more_info, null)     // do not dismiss the dialog on pressing More Info
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        moreInfo()
                    }
                }
    }

    private fun upgradeApp() {
        activity.launchUpgradeToProIntent()
    }

    private fun moreInfo() {
        activity.launchViewIntent("https://medium.com/@tibbi/some-simple-mobile-tools-apps-are-becoming-paid-d053268f0fb2")
    }
}
