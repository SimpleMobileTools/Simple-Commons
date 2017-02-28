package com.simplemobiletools.commons.dialogs

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_create_new_folder.view.*
import java.io.File

class CreateNewFolderDialog(val activity: BaseSimpleActivity, val path: String, val callback: (path: String) -> Unit) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_create_new_folder, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            context.setupDialogStuff(view, this, R.string.create_new_folder)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                val name = view.folder_name.value
                if (name.isEmpty()) {
                    context.toast(R.string.empty_name)
                } else if (name.isAValidFilename()) {
                    val file = File(path, name)
                    if (file.exists()) {
                        context.toast(R.string.name_taken)
                        return@OnClickListener
                    }

                    if (!createDirectory(file, this)) {
                        context.toast(R.string.unknown_error_occurred)
                    }
                } else {
                    context.toast(R.string.invalid_name)
                }
            })
        }
    }

    private fun createDirectory(file: File, alertDialog: AlertDialog): Boolean {
        return if (activity.needsStupidWritePermissions(path)) {
            if (activity.isShowingPermDialog(file)) {
                return true
            }
            val documentFile = activity.getFileDocument(file.absolutePath, activity.baseConfig.treeUri)
            documentFile?.createDirectory(file.name)
            sendSuccess(alertDialog, file)
            true
        } else if (file.mkdirs()) {
            sendSuccess(alertDialog, file)
            true
        } else {
            false
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, file: File) {
        callback.invoke(file.absolutePath)
        alertDialog.dismiss()
    }
}
