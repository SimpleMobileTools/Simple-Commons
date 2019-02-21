package com.simplemobiletools.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.R.id.*
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.DATE_FORMAT_FOUR
import com.simplemobiletools.commons.helpers.DATE_FORMAT_ONE
import com.simplemobiletools.commons.helpers.DATE_FORMAT_THREE
import com.simplemobiletools.commons.helpers.DATE_FORMAT_TWO
import kotlinx.android.synthetic.main.dialog_change_date_time_format.view.*

class ChangeDateTimeFormatDialog(val activity: Activity, val callback: () -> Unit) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_change_date_time_format, null)!!

    init {
        view.apply {
            change_date_time_dialog_radio_one.text = DATE_FORMAT_ONE
            change_date_time_dialog_radio_two.text = DATE_FORMAT_TWO
            change_date_time_dialog_radio_three.text = DATE_FORMAT_THREE
            change_date_time_dialog_radio_four.text = DATE_FORMAT_FOUR

            change_date_time_dialog_24_hour.isChecked = activity.baseConfig.use24HourFormat

            val formatButton = when (activity.baseConfig.dateFormat) {
                DATE_FORMAT_ONE -> change_date_time_dialog_radio_one
                DATE_FORMAT_TWO -> change_date_time_dialog_radio_two
                DATE_FORMAT_THREE -> change_date_time_dialog_radio_three
                else -> change_date_time_dialog_radio_four
            }
            formatButton.isChecked = true
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private fun dialogConfirmed() {
        activity.baseConfig.dateFormat = when (view.change_date_time_dialog_radio_group.checkedRadioButtonId) {
            change_date_time_dialog_radio_one -> DATE_FORMAT_ONE
            change_date_time_dialog_radio_two -> DATE_FORMAT_TWO
            change_date_time_dialog_radio_three -> DATE_FORMAT_THREE
            else -> DATE_FORMAT_FOUR
        }

        activity.baseConfig.use24HourFormat = view.change_date_time_dialog_24_hour.isChecked
        callback()
    }
}
