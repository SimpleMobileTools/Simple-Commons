package com.simplemobiletools.commons.dialogs

import android.support.v7.app.AlertDialog
import android.view.WindowManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_rename_folder.view.*
import java.io.File
import java.util.*

class RenameFolderDialog(val activity: BaseSimpleActivity, val dir: File, val callback: () -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_folder, null).apply {
            folder_name.setText(dir.name)
            folder_path.text = activity.humanizePath(dir.parent) + "/"
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this, R.string.rename_folder)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                val newDirName = view.folder_name.value
                if (newDirName.isEmpty()) {
                    activity.toast(R.string.rename_folder_empty)
                    return@setOnClickListener
                }

                if (!newDirName.isAValidFilename()) {
                    activity.toast(R.string.invalid_name)
                    return@setOnClickListener
                }

                val updatedFiles = ArrayList<File>()
                updatedFiles.add(dir)
                val newDir = File(dir.parent, newDirName)

                if (newDir.exists()) {
                    activity.toast(R.string.rename_folder_exists)
                    return@setOnClickListener
                }

                updatedFiles.add(newDir)
                if (activity.needsStupidWritePermissions(dir.absolutePath)) {
                    activity.handleSAFDialog(dir) {
                        val document = activity.getFileDocument(dir.absolutePath, activity.baseConfig.treeUri) ?: return@handleSAFDialog
                        if (document.canWrite())
                            document.renameTo(newDirName)
                        sendSuccess(updatedFiles)
                        dismiss()
                    }
                } else if (dir.renameTo(newDir)) {
                    sendSuccess(updatedFiles)
                    dismiss()
                } else {
                    activity.toast(R.string.rename_folder_error)
                }
            })
        }
    }

    private fun sendSuccess(updatedFiles: ArrayList<File>) {
        activity.apply {
            toast(R.string.renaming_folder)
            val files = updatedFiles[1].listFiles()
            if (files != null) {
                for (file in files) {
                    scanPath(file.absolutePath) {
                        callback()
                    }
                    break
                }
            }
        }
    }
}
