package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.launchPurchaseThankYouIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_purchase_thank_you.view.*

class PurchaseThankYouDialog(val activity: Activity) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_purchase_thank_you, null).apply {
            var text = activity.getString(R.string.purchase_thank_you)
            if (activity.baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
                text += "<br><br>${activity.getString(R.string.shared_theme_note)}"
            }

            purchase_thank_you.text = Html.fromHtml(text)
            purchase_thank_you.movementMethod = LinkMovementMethod.getInstance()
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.purchase) { dialog, which -> activity.launchPurchaseThankYouIntent() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, cancelOnTouchOutside = false)
            }
    }
}
