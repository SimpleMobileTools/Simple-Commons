package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_radio_group.view.*

/**
 * A dialog for choosing between internal, root, SD card (optional) storage
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param currPath current path to decide which storage should be preselected
 * @param callback an anonymous function
 *
 */
class StoragePickerDialog(val activity: Activity, currPath: String, val callback: (pickedPath: String) -> Unit) {
    var mDialog: AlertDialog

    init {
        val inflater = LayoutInflater.from(activity)
        val resources = activity.resources
        val basePath = currPath.getBasePath(activity)
        val layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val view = inflater.inflate(R.layout.dialog_radio_group, null)
        val radioGroup = view.dialog_radio_group

        val internalButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
        internalButton.apply {
            text = resources.getString(R.string.internal)
            isChecked = basePath == context.internalStoragePath
            setOnClickListener { internalPicked() }
        }
        radioGroup.addView(internalButton, layoutParams)

        if (activity.hasExternalSDCard()) {
            val sdButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
            sdButton.apply {
                text = resources.getString(R.string.sd_card)
                isChecked = basePath == context.sdCardPath
                setOnClickListener { sdPicked() }
            }
            radioGroup.addView(sdButton, layoutParams)
        }

        val rootButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
        rootButton.apply {
            text = resources.getString(R.string.root)
            isChecked = basePath == "/"
            setOnClickListener { rootPicked() }
        }
        radioGroup.addView(rootButton, layoutParams)

        mDialog = AlertDialog.Builder(activity)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.select_storage)
        }
    }

    private fun internalPicked() {
        mDialog.dismiss()
        callback(activity.internalStoragePath)
    }

    private fun sdPicked() {
        mDialog.dismiss()
        callback(activity.sdCardPath)
    }

    private fun rootPicked() {
        mDialog.dismiss()
        callback("/")
    }
}
