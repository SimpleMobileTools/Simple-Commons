package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.commons.helpers.DAY_MINUTES
import kotlinx.android.synthetic.main.dialog_custom_interval_picker.view.*

class CustomIntervalPickerDialog(val activity: Activity, val selectedMinutes: Int = 0, val callback: (minutes: Int) -> Unit) {
    var dialog: AlertDialog
    var view = (activity.layoutInflater.inflate(R.layout.dialog_custom_interval_picker, null) as ViewGroup)

    init {
        view.apply {
            when {
                selectedMinutes == 0 -> dialog_radio_view.check(R.id.dialog_radio_minutes)
                selectedMinutes % DAY_MINUTES == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_days)
                    dialog_custom_interval_value.setText((selectedMinutes / DAY_MINUTES).toString())
                }
                selectedMinutes % 60 == 0 -> {
                    dialog_radio_view.check(R.id.dialog_radio_hours)
                    dialog_custom_interval_value.setText((selectedMinutes / 60).toString())
                }
                else -> {
                    dialog_radio_view.check(R.id.dialog_radio_minutes)
                    dialog_custom_interval_value.setText(selectedMinutes.toString())
                }
            }
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialogInterface, i -> confirmReminder() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        showKeyboard(view.dialog_custom_interval_value)
                    }
                }
    }

    private fun confirmReminder() {
        val value = view.dialog_custom_interval_value.value
        val multiplier = getMultiplier(view.dialog_radio_view.checkedRadioButtonId)
        val minutes = Integer.valueOf(if (value.isEmpty()) "0" else value)
        callback(minutes * multiplier)
        activity.hideKeyboard()
        dialog.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        R.id.dialog_radio_hours -> 60
        R.id.dialog_radio_days -> DAY_MINUTES
        else -> 1
    }
}
