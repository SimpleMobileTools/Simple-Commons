package com.simplemobiletools.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_upgrade_to_pro.view.*

class UpgradeToProDialog(val activity: Activity) {
    val packageName = activity.baseConfig.appId.removeSuffix(".debug")
    val GALLERY_FREE_TILL = 1543104000000L     // November 25
    val NOV_25 = "Nov 25 2018"

    init {
        var text = activity.getString(R.string.upgrade_to_pro)
        if (packageName.endsWith("gallery") && System.currentTimeMillis() < GALLERY_FREE_TILL) {
            val freeTill = String.format(activity.getString(R.string.it_is_free), NOV_25)
            text += "\n$freeTill"
        }

        val view = activity.layoutInflater.inflate(R.layout.dialog_upgrade_to_pro, null).apply {
            upgrade_to_pro.text = text
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.upgrade) { dialog, which -> upgradeApp() }
                .setNeutralButton(R.string.more_info) { dialog, which -> moreInfo() }
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private fun upgradeApp() {
        activity.launchViewIntent("https://play.google.com/store/apps/details?id=com.simplemobiletools.gallery.pro")
    }

    private fun moreInfo() {
        activity.launchViewIntent("https://medium.com/@tibbi/some-simple-mobile-tools-apps-are-becoming-paid-d053268f0fb2")
    }
}
