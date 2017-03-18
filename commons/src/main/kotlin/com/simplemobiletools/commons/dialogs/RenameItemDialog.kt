package com.simplemobiletools.commons.dialogs

import android.support.v7.app.AlertDialog
import android.view.WindowManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_rename_item.view.*
import java.io.File
import java.util.*

class RenameItemDialog(val activity: BaseSimpleActivity, val path: String, val callback: (newPath: String) -> Unit) {
    init {
        val file = File(path)
        val fullName = file.name
        val dotAt = fullName.lastIndexOf(".")
        var name = fullName

        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_item, null).apply {
            if (dotAt > 0 && !file.isDirectory) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                rename_item_extension.setText(extension)
            } else {
                rename_item_extension_label.beGone()
                rename_item_extension.beGone()
            }

            rename_item_name.setText(name)
            rename_item_path.text = activity.humanizePath(file.parent ?: "") + "/"
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this, R.string.rename)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                var newName = view.rename_item_name.value
                val newExtension = view.rename_item_extension.value

                if (newName.isEmpty()) {
                    activity.toast(R.string.empty_name)
                    return@setOnClickListener
                }

                if (!newName.isAValidFilename()) {
                    activity.toast(R.string.invalid_name)
                    return@setOnClickListener
                }

                val updatedFiles = ArrayList<File>()
                updatedFiles.add(file)
                if (!newExtension.isEmpty())
                    newName += ".$newExtension"

                val newFile = File(file.parent, newName)
                if (newFile.exists()) {
                    activity.toast(R.string.name_taken)
                    return@setOnClickListener
                }

                updatedFiles.add(newFile)
                activity.renameFile(file, newFile) {
                    if (it) {
                        sendSuccess(updatedFiles)
                        dismiss()
                    } else {
                        activity.toast(R.string.unknown_error_occurred)
                    }
                }
            })
        }
    }

    private fun sendSuccess(updatedFiles: ArrayList<File>) {
        activity.apply {
            if (updatedFiles[1].isDirectory) {
                val files = updatedFiles[1].listFiles()
                if (files != null) {
                    if (files.isEmpty()) {
                        scanPath(updatedFiles[1].absolutePath) {
                            callback(updatedFiles[1].absolutePath.trimEnd('/'))
                        }
                    } else {
                        for (file in files) {
                            scanPath(file.absolutePath) {
                                callback(updatedFiles[1].absolutePath.trimEnd('/'))
                            }
                            break
                        }
                    }
                }
            } else {
                scanFiles(updatedFiles) {
                    callback(updatedFiles[1].absolutePath.trimEnd('/'))
                }
            }
        }
    }
}
