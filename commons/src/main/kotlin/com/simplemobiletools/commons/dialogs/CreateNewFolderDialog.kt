package com.simplemobiletools.commons.dialogs

import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_create_new_folder.view.*
import java.io.File

class CreateNewFolderDialog(val activity: BaseSimpleActivity, val path: String, val callback: (path: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_create_new_folder, null)
        view.folder_path.text = activity.humanizePath(path).trimEnd('/') + "/"

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            context.setupDialogStuff(view, this, R.string.create_new_folder)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                val name = view.folder_name.value
                when {
                    name.isEmpty() -> activity.toast(R.string.empty_name)
                    name.isAValidFilename() -> {
                        val file = File(path, name)
                        if (file.exists()) {
                            activity.toast(R.string.name_taken)
                            return@OnClickListener
                        }

                        createFolder(file, this)
                    }
                    else -> activity.toast(R.string.invalid_name)
                }
            })
        }
    }

    private fun createFolder(file: File, alertDialog: AlertDialog) {
        if (activity.needsStupidWritePermissions(file.absolutePath)) {
            activity.handleSAFDialog(file) {
                try {
                    val documentFile = activity.getFileDocument(file.absolutePath)
                    documentFile?.createDirectory(file.name)
                    sendSuccess(alertDialog, file)
                } catch (e: SecurityException) {
                    activity.showErrorToast(e)
                }
            }
        } else if (file.mkdirs()) {
            sendSuccess(alertDialog, file)
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, file: File) {
        callback(file.absolutePath.trimEnd('/'))
        alertDialog.dismiss()
    }
}
