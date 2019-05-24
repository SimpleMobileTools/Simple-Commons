package com.simplemobiletools.commons.dialogs

import android.media.ExifInterface
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isNougatPlus
import kotlinx.android.synthetic.main.dialog_rename_items_pattern.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RenameItemsPatternDialog(val activity: BaseSimpleActivity, val paths: ArrayList<String>, val callback: () -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_items_pattern, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.rename) {
                        showKeyboard(view.rename_items_value)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val validPaths = paths.filter { File(it).exists() }
                            val sdFilePath = validPaths.firstOrNull { activity.isPathOnSD(it) } ?: validPaths.firstOrNull()
                            if (sdFilePath == null) {
                                activity.toast(R.string.unknown_error_occurred)
                                dismiss()
                                return@setOnClickListener
                            }

                            activity.handleSAFDialog(sdFilePath) {
                                var pathsCnt = validPaths.size
                                for (path in validPaths) {
                                    val exif = ExifInterface(path)
                                    val dateTime = if (isNougatPlus()) {
                                        exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                                                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                                    } else {
                                        exif.getAttribute(ExifInterface.TAG_DATETIME)
                                    } ?: continue

                                    val simpleDateFormat = SimpleDateFormat("yyyy:MM:dd kk:mm:ss", Locale.ENGLISH)
                                    val dt = simpleDateFormat.parse(dateTime)
                                    val cal = Calendar.getInstance()
                                    cal.time = dt
                                    val year = ensureTwoDigits(cal.get(Calendar.YEAR))
                                    val month = ensureTwoDigits(cal.get(Calendar.MONTH) + 1)
                                    val day = ensureTwoDigits(cal.get(Calendar.DAY_OF_MONTH))
                                    val hours = ensureTwoDigits(cal.get(Calendar.HOUR_OF_DAY))
                                    val minutes = ensureTwoDigits(cal.get(Calendar.MINUTE))
                                    val seconds = ensureTwoDigits(cal.get(Calendar.SECOND))

                                    val newName = view.rename_items_value.value
                                            .replace("%Y", year, false)
                                            .replace("%M", month, false)
                                            .replace("%D", day, false)
                                            .replace("%h", hours, false)
                                            .replace("%m", minutes, false)
                                            .replace("%s", seconds, false)

                                    val newPath = "${path.getParentPath()}/$newName"

                                    if (File(newPath).exists()) {
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

    private fun ensureTwoDigits(value: Int): String {
        return if (value.toString().length == 1) {
            "0$value"
        } else {
            value.toString()
        }
    }
}
