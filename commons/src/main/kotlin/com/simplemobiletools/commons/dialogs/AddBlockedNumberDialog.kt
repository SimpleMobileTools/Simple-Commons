package com.simplemobiletools.commons.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.databinding.DialogAddBlockedNumberBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.BlockedNumber

class AddBlockedNumberDialog(val activity: BaseSimpleActivity, val originalNumber: BlockedNumber? = null, val callback: () -> Unit) {
    init {
        val view = DialogAddBlockedNumberBinding.inflate(activity.layoutInflater, null, false).apply {
            if (originalNumber != null) {
                addBlockedNumberEdittext.setText(originalNumber.number)
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    alertDialog.showKeyboard(view.addBlockedNumberEdittext)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        var newBlockedNumber = view.addBlockedNumberEdittext.value
                        if (originalNumber != null && newBlockedNumber != originalNumber.number) {
                            activity.deleteBlockedNumber(originalNumber.number)
                        }

                        if (newBlockedNumber.isNotEmpty()) {
                            // in case the user also added a '.' in the pattern, remove it
                            if (newBlockedNumber.contains(".*")) {
                                newBlockedNumber = newBlockedNumber.replace(".*", "*")
                            }
                            activity.addBlockedNumber(newBlockedNumber)
                        }

                        callback()
                        alertDialog.dismiss()
                    }
                }
            }
    }
}
