package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.DialogFeatureLockedBinding
import com.simplemobiletools.commons.extensions.*

class FeatureLockedDialog(val activity: Activity, val callback: () -> Unit) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogFeatureLockedBinding.inflate(activity.layoutInflater, null, false)
        view.featureLockedImage.applyColorFilter(activity.getProperTextColor())

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.purchase, null)
            .setNegativeButton(R.string.later) { _, _ -> dismissDialog() }
            .setOnDismissListener { dismissDialog() }
            .apply {
                activity.setupDialogStuff(view.root, this, cancelOnTouchOutside = false) { alertDialog ->
                    dialog = alertDialog
                    view.featureLockedDescription.text = Html.fromHtml(activity.getString(R.string.features_locked))
                    view.featureLockedDescription.movementMethod = LinkMovementMethod.getInstance()

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        activity.launchPurchaseThankYouIntent()
                    }
                }
            }
    }

    fun dismissDialog() {
        dialog?.dismiss()
        callback()
    }
}
