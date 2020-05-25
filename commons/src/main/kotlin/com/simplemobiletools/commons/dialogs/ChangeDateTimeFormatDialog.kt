package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.R.id.*
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.*
import kotlinx.android.synthetic.main.dialog_change_date_time_format.view.*
import java.util.*

class ChangeDateTimeFormatDialog(val activity: Activity, val datesOnly: Boolean = false, val callback: () -> Unit) {
    val view = activity.layoutInflater.inflate(R.layout.dialog_change_date_time_format, null)!!
    val sampleTS = 1557964800000    // May 16, 2019

    init {
        view.apply {
            change_date_time_dialog_radio_one.text = formatDateSample(DATE_FORMAT_ONE)
            change_date_time_dialog_radio_two.text = formatDateSample(DATE_FORMAT_TWO)
            change_date_time_dialog_radio_three.text = formatDateSample(DATE_FORMAT_THREE)
            change_date_time_dialog_radio_four.text = formatDateSample(DATE_FORMAT_FOUR)
            change_date_time_dialog_radio_five.text = formatDateSample(DATE_FORMAT_FIVE)
            change_date_time_dialog_radio_six.text = formatDateSample(DATE_FORMAT_SIX)
            change_date_time_dialog_radio_seven.text = formatDateSample(DATE_FORMAT_SEVEN)
            change_date_time_dialog_radio_eight.text = formatDateSample(DATE_FORMAT_EIGHT)
            change_date_time_dialog_radio_nine.text = formatDateSample(DATE_FORMAT_NINE)

            change_date_time_dialog_24_hour.visibility = if (datesOnly) View.INVISIBLE else View.VISIBLE
            change_date_time_dialog_24_hour.isChecked = activity.baseConfig.use24HourFormat

            val formatButton = when (activity.baseConfig.dateFormat) {
                DATE_FORMAT_ONE -> change_date_time_dialog_radio_one
                DATE_FORMAT_TWO -> change_date_time_dialog_radio_two
                DATE_FORMAT_THREE -> change_date_time_dialog_radio_three
                DATE_FORMAT_FOUR -> change_date_time_dialog_radio_four
                DATE_FORMAT_FIVE -> change_date_time_dialog_radio_five
                DATE_FORMAT_SIX -> change_date_time_dialog_radio_six
                DATE_FORMAT_SEVEN -> change_date_time_dialog_radio_seven
                else -> change_date_time_dialog_radio_eight
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
            change_date_time_dialog_radio_four -> DATE_FORMAT_FOUR
            change_date_time_dialog_radio_five -> DATE_FORMAT_FIVE
            change_date_time_dialog_radio_six -> DATE_FORMAT_SIX
            change_date_time_dialog_radio_seven -> DATE_FORMAT_SEVEN
            else -> DATE_FORMAT_EIGHT
        }

        activity.baseConfig.use24HourFormat = view.change_date_time_dialog_24_hour.isChecked
        callback()
    }

    private fun formatDateSample(format: String): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = sampleTS
        return DateFormat.format(format, cal).toString()
    }
}
