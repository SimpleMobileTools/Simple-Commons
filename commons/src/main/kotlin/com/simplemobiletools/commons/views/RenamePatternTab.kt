package com.simplemobiletools.commons.views

import android.content.Context
import android.media.ExifInterface
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.interfaces.RenameTab
import kotlinx.android.synthetic.main.dialog_rename_items_pattern.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RenamePatternTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), RenameTab {
    var ignoreClicks = false
    var activity: BaseSimpleActivity? = null
    var paths = ArrayList<String>()

    override fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>) {
        this.activity = activity
        this.paths = paths
    }

    override fun dialogConfirmed(callback: () -> Unit) {
        if (ignoreClicks) {
            return
        }

        var newName = rename_items_value.value
        if (newName.isEmpty()) {
            callback()
            return
        }

        val validPaths = paths.filter { File(it).exists() }
        val sdFilePath = validPaths.firstOrNull { activity?.isPathOnSD(it) == true } ?: validPaths.firstOrNull()
        if (sdFilePath == null) {
            activity?.toast(R.string.unknown_error_occurred)
            return
        }

        activity?.handleSAFDialog(sdFilePath) {
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
                while (File(newPath).exists()) {
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

                activity?.renameFile(path, newPath) {
                    if (it) {
                        pathsCnt--
                        if (pathsCnt == 0) {
                            callback()
                        }
                    } else {
                        ignoreClicks = false
                        activity?.toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }
}
