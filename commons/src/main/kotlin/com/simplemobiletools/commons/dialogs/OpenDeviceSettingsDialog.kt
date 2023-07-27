package com.simplemobiletools.commons.dialogs

import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.databinding.DialogOpenDeviceSettingsBinding
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.openDeviceSettings
import com.simplemobiletools.commons.extensions.setupDialogStuff

class OpenDeviceSettingsDialog(val activity: BaseSimpleActivity, message: String) {

    init {
        activity.apply {
            val view = DialogOpenDeviceSettingsBinding.inflate(layoutInflater, null, false)
            view.openDeviceSettings.text = message
            getAlertDialogBuilder()
                .setNegativeButton(R.string.close, null)
                .setPositiveButton(R.string.go_to_settings) { _, _ ->
                    openDeviceSettings()
                }.apply {
                    setupDialogStuff(view.root, this)
                }
        }
    }
}
