package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.dialog_create_new_folder.view.*
import java.io.File

class CreateNewFolderDialog(val context: Context, val path: String, val callback: () -> Unit) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_new_folder, null)

        AlertDialog.Builder(context)
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

                    if (createDirectory(file)) {
                        callback.invoke()
                        dismiss()
                    } else {
                        context.toast(R.string.error_occurred)
                    }

                } else {
                    context.toast(R.string.invalid_name)
                }
            })
        }
    }

    private fun createDirectory(file: File): Boolean {
        return if (context.needsStupidWritePermissions(path)) {
            val documentFile = context.getFileDocument(file.absolutePath, BaseConfig.newInstance(context).treeUri)
            documentFile.createDirectory(file.name)
            true
        } else
            file.mkdirs()
    }
}
