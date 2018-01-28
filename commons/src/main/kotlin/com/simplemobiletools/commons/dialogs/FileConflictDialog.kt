package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_file_conflict.view.*

class FileConflictDialog(val activity: Activity, val filename: String) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_file_conflict, null).apply {
            conflict_dialog_title.text = String.format(activity.getString(R.string.file_already_exists), filename)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }
}
