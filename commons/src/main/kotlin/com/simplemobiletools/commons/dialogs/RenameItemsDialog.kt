package com.simplemobiletools.commons.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_rename_items.*
import kotlinx.android.synthetic.main.dialog_rename_items.view.*
import java.util.*

class RenameItemsDialog(val activity: BaseSimpleActivity, val paths: ArrayList<String>, val callback: () -> Unit) {
    init {

        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_items, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.rename) {
                        showKeyboard(view.rename_items_value)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val valueToAdd = view.rename_items_value.value
                            val append = view.rename_items_radio_group.checkedRadioButtonId == rename_items_radio_append.id

                            if (valueToAdd.isEmpty()) {
                                callback()
                                dismiss()
                                return@setOnClickListener
                            }

                            if (!valueToAdd.isAValidFilename()) {
                                activity.toast(R.string.invalid_name)
                                return@setOnClickListener
                            }

                            val validPaths = paths.filter { activity.getDoesFilePathExist(it) }
                            val sdFilePath = validPaths.firstOrNull { activity.isPathOnSD(it) }
                                    ?: validPaths.firstOrNull()
                            if (sdFilePath == null) {
                                activity.toast(R.string.unknown_error_occurred)
                                dismiss()
                                return@setOnClickListener
                            }

                            var pathsCnt = validPaths.size

                            activity.handleSAFDialog(sdFilePath) {
                                for (path in validPaths) {
                                    val fullName = path.getFilenameFromPath()
                                    var dotAt = fullName.lastIndexOf(".")
                                    if (dotAt == -1) {
                                        dotAt = fullName.length
                                    }

                                    val name = fullName.substring(0, dotAt)
                                    val extension = if (fullName.contains(".")) ".${fullName.getFilenameExtension()}" else ""

                                    val newName = if (append) {
                                        "$name$valueToAdd$extension"
                                    } else {
                                        "$valueToAdd$fullName"
                                    }

                                    val newPath = "${path.getParentPath()}/$newName"

                                    if (activity.getDoesFilePathExist(newPath)) {
                                        continue
                                    }

                                    activity.renameFile(path, newPath) {
                                        if (it) {
                                            pathsCnt--
                                            if (pathsCnt == 0) {
                                                callback()
                                                dismiss()
                                            }
                                        } else {
                                            activity.toast(R.string.unknown_error_occurred)
                                            dismiss()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
    }
}
