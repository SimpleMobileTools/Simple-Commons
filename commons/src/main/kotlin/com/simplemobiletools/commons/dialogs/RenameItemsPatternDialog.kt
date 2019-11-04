package com.simplemobiletools.commons.dialogs

import android.media.ExifInterface
import android.text.format.DateFormat
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
        var ignoreClicks = false
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_items_pattern, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.rename) {
                        showKeyboard(view.rename_items_value)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (ignoreClicks) {
                                return@setOnClickListener
                            }

                            val validPaths = paths.filter { activity.getDoesFilePathExist(it) }
                            val sdFilePath = validPaths.firstOrNull { activity.isPathOnSD(it) } ?: validPaths.firstOrNull()
                            if (sdFilePath == null) {
                                activity.toast(R.string.unknown_error_occurred)
                                dismiss()
                                return@setOnClickListener
                            }

                            activity.handleSAFDialog(sdFilePath) {
                                ignoreClicks = true
                                var pathsCnt = validPaths.size
                                for (path in validPaths) {
                                    val exif = ExifInterface(path)
                                    var dateTime = if (isNougatPlus()) {
                                        exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                                                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                                    } else {
                                        exif.getAttribute(ExifInterface.TAG_DATETIME)
                                    }

                                    if (dateTime == null) {
                                        val calendar = Calendar.getInstance(Locale.ENGLISH)
                                        calendar.timeInMillis = File(path).lastModified()
                                        dateTime = DateFormat.format("yyyy:MM:dd kk:mm:ss", calendar).toString()
                                    }

                                    var newName = view.rename_items_value.value
                                    val simpleDateFormat = SimpleDateFormat("yyyy:MM:dd kk:mm:ss", Locale.ENGLISH)
                                    val dt = simpleDateFormat.parse(dateTime)
                                    val cal = Calendar.getInstance()
                                    cal.time = dt
                                    val year = cal.get(Calendar.YEAR).toString()
                                    val month = (cal.get(Calendar.MONTH) + 1).ensureTwoDigits()
                                    val day = (cal.get(Calendar.DAY_OF_MONTH)).ensureTwoDigits()
                                    val hours = (cal.get(Calendar.HOUR_OF_DAY)).ensureTwoDigits()
                                    val minutes = (cal.get(Calendar.MINUTE)).ensureTwoDigits()
                                    val seconds = (cal.get(Calendar.SECOND)).ensureTwoDigits()

                                    newName = newName
                                            .replace("%Y", year, false)
                                            .replace("%M", month, false)
                                            .replace("%D", day, false)
                                            .replace("%h", hours, false)
                                            .replace("%m", minutes, false)
                                            .replace("%s", seconds, false)

                                    if (newName.isEmpty()) {
                                        continue
                                    }

                                    if (!newName.contains(".") && path.contains(".")) {
                                        val extension = path.substringAfterLast(".")
                                        newName += ".$extension"
                                    }

                                    var newPath = "${path.getParentPath()}/$newName"

                                    var currentIndex = 0
                                    while (activity.getDoesFilePathExist(newPath)) {
                                        currentIndex++
                                        var extension = ""
                                        val name = if (newName.contains(".")) {
                                            extension = ".${newName.substringAfterLast(".")}"
                                            newName.substringBeforeLast(".")
                                        } else {
                                            newName
                                        }

                                        newPath = "${path.getParentPath()}/$name~$currentIndex$extension"
                                    }

                                    activity.renameFile(path, newPath) {
                                        if (it) {
                                            pathsCnt--
                                            if (pathsCnt == 0) {
                                                callback()
                                                dismiss()
                                            }
                                        } else {
                                            ignoreClicks = false
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
