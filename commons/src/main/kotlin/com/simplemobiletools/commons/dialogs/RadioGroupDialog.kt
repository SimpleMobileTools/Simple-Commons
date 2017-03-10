package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.dialog_radio_group.view.*

class RadioGroupDialog(val activity: Activity, val items: Array<RadioItem>, val checkedItemId: Int, val callback: (newValue: Int) -> Unit) :
        AlertDialog.Builder(activity), RadioGroup.OnCheckedChangeListener {
    val dialog: AlertDialog?

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_radio_group, null)
        view.dialog_radio_group.apply {
            setOnCheckedChangeListener(this@RadioGroupDialog)

            items.map {
                (activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton).apply {
                    text = it.title
                    isChecked = it.id == checkedItemId
                    id = it.id
                }
            }.forEach { addView(it, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
        }

        dialog = AlertDialog.Builder(activity)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        callback.invoke(items[checkedId].id)
        dialog?.dismiss()
    }
}
