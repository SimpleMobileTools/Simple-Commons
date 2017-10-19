package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.dialog_radio_group.view.*
import java.util.*

class RadioGroupDialog(val activity: Activity, val items: ArrayList<RadioItem>, val checkedItemId: Int, val titleId: Int = 0, val callback: (newValue: Any) -> Unit) :
        AlertDialog.Builder(activity), RadioGroup.OnCheckedChangeListener {
    val dialog: AlertDialog
    var wasInit = false
    var selectedItemId = -1

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_radio_group, null)
        view.dialog_radio_group.apply {
            setOnCheckedChangeListener(this@RadioGroupDialog)

            for (i in 0..items.size - 1) {
                val radioButton = (activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton).apply {
                    text = items[i].title
                    isChecked = items[i].id == checkedItemId
                    id = i
                }
                if (items[i].id == checkedItemId)
                    selectedItemId = i
                addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        dialog = AlertDialog.Builder(activity)
                .create().apply {
            activity.setupDialogStuff(view, this, titleId)
        }

        if (selectedItemId != -1) {
            view.dialog_radio_holder.apply {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        scrollY = view.dialog_radio_group.findViewById(selectedItemId).bottom - height
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }

        wasInit = true
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        if (wasInit) {
            callback(items[checkedId].value)
            dialog.dismiss()
        }
    }
}
