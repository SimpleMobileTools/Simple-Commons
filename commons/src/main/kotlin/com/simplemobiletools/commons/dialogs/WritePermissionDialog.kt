package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff

/**
 * A dialog for displaying the steps needed to confirm SD card write access on Android 5+
 *
 * @param context has to be activity context to avoid some Theme.AppCompat issues
 * @param callback an anonymous function
 *
 */
class WritePermissionDialog(context: Context, val callback: () -> Unit) {
    var dialog: AlertDialog

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_write_permission, null)

        dialog = AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setOnCancelListener { BaseSimpleActivity.funAfterSAFPermission = null }
                .create().apply {
            context.setupDialogStuff(view, this, R.string.confirm_storage_access_title)
        }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
