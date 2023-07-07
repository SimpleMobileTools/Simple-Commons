package com.simplemobiletools.commons.dialogs

import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.openAppSettings
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_open_app_settings.view.open_app_settings

class OpenAppSettingsDialog(val activity: BaseSimpleActivity, message: String, val callback: (() -> Unit)? = null) {

    init {
        activity.apply {
            val view = layoutInflater.inflate(R.layout.dialog_open_app_settings, null)
            view.open_app_settings.text = message
            getAlertDialogBuilder()
                .setNegativeButton(R.string.close, null)
                .setPositiveButton(R.string.go_to_settings) { _, _ ->
                    dialogConfirmed()
                }.apply {
                    setupDialogStuff(view, this)
                }
        }
    }

    private fun dialogConfirmed() {
        callback?.invoke()
        activity.openAppSettings()
    }
}
