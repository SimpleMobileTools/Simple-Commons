package com.simplemobiletools.commons.dialogs

import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.databinding.DialogChangeViewTypeBinding
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.VIEW_TYPE_GRID
import com.simplemobiletools.commons.helpers.VIEW_TYPE_LIST

class ChangeViewTypeDialog(val activity: BaseSimpleActivity, val path: String = "", val callback: () -> Unit) {
    private var view: DialogChangeViewTypeBinding
    private var config = activity.baseConfig

    init {
        view = DialogChangeViewTypeBinding.inflate(activity.layoutInflater, null, false).apply {
            val viewToCheck = when (config.viewType) {
                VIEW_TYPE_GRID -> changeViewTypeDialogRadioGrid.id
                else -> changeViewTypeDialogRadioList.id
            }

            changeViewTypeDialogRadio.check(viewToCheck)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this)
            }
    }

    private fun dialogConfirmed() {
        val viewType = if (view.changeViewTypeDialogRadioGrid.isChecked) {
            VIEW_TYPE_GRID
        } else {
            VIEW_TYPE_LIST
        }
        config.viewType = viewType
        callback()
    }
}
