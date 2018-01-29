package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.R.id.conflict_dialog_radio_skip
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.CONFLICT_OVERWRITE
import com.simplemobiletools.commons.helpers.CONFLICT_SKIP
import kotlinx.android.synthetic.main.dialog_file_conflict.view.*

class FileConflictDialog(val activity: Activity, val filename: String, val callback: (resolution: Int, applyForAll: Boolean) -> Unit) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_file_conflict, null)

    init {
        view.apply {
            conflict_dialog_title.text = String.format(activity.getString(R.string.file_already_exists), filename)
            conflict_dialog_apply_to_all.isChecked = activity.baseConfig.lastConflictApplyToAll

            val resolutionButton = when (activity.baseConfig.lastConflictResolution) {
                CONFLICT_OVERWRITE -> conflict_dialog_radio_overwrite
                else -> conflict_dialog_radio_skip
            }
            resolutionButton.isChecked = true
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    private fun dialogConfirmed() {
        val resolution = when (view.conflict_dialog_radio_group.checkedRadioButtonId) {
            conflict_dialog_radio_skip -> CONFLICT_SKIP
            else -> CONFLICT_OVERWRITE
        }

        val applyToAll = view.conflict_dialog_apply_to_all.isChecked
        activity.baseConfig.apply {
            lastConflictApplyToAll = applyToAll
            lastConflictResolution = resolution
        }
        callback(resolution, applyToAll)
    }
}
