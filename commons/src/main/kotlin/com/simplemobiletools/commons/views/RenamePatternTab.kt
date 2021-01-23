package com.simplemobiletools.commons.views

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.exifinterface.media.ExifInterface
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

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(rename_items_holder)
    }

    override fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>) {
        this.activity = activity
        this.paths = paths
        rename_items_value.setText(activity.baseConfig.lastRenamePatternUsed)
    }

    override fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit) {
        if (ignoreClicks) {
            return
        }

        val newNameRaw = rename_items_value.value
        if (newNameRaw.isEmpty()) {
            callback(false)
            return
        }

        val validPaths = paths.filter { activity?.getDoesFilePathExist(it) == true }
        val sdFilePath = validPaths.firstOrNull { activity?.isPathOnSD(it) == true } ?: validPaths.firstOrNull()
        if (sdFilePath == null) {
            activity?.toast(R.string.unknown_error_occurred)
            return
        }

        activity?.baseConfig?.lastRenamePatternUsed = rename_items_value.value
        activity?.handleSAFDialog(sdFilePath) {
            if (!it) {
                return@handleSAFDialog
            }

            ignoreClicks = true
            var currentIncrementalNumber = 1
            var pathsCnt = validPaths.size
            val numbersCnt = pathsCnt.toString().length
            for (path in validPaths) {
                val exif = ExifInterface(path)
                var dateTime = if (isNougatPlus()) {
                    exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                } else {
                    exif.getAttribute(ExifInterface.TAG_DATETIME)
                }

                if (dateTime == null) {
                    val calendar = Calendar.getInstance(Locale.ENGLISH)
                    calendar.timeInMillis = File(path).lastModified()
                    dateTime = DateFormat.format("yyyy:MM:dd kk:mm:ss", calendar).toString()
                }

                try {
                    val pattern = if (dateTime.substring(4, 5) == "-") "yyyy-MM-dd kk:mm:ss" else "yyyy:MM:dd kk:mm:ss"
                    val simpleDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)

                    val dt = simpleDateFormat.parse(dateTime.replace("T", " "))
                    val cal = Calendar.getInstance()
                    cal.time = dt
                    val year = cal.get(Calendar.YEAR).toString()
                    val month = (cal.get(Calendar.MONTH) + 1).ensureTwoDigits()
                    val day = (cal.get(Calendar.DAY_OF_MONTH)).ensureTwoDigits()
                    val hours = (cal.get(Calendar.HOUR_OF_DAY)).ensureTwoDigits()
                    val minutes = (cal.get(Calendar.MINUTE)).ensureTwoDigits()
                    val seconds = (cal.get(Calendar.SECOND)).ensureTwoDigits()

                    var newName = rename_items_value.value
                        .replace("%Y", year, false)
                        .replace("%M", month, false)
                        .replace("%D", day, false)
                        .replace("%h", hours, false)
                        .replace("%m", minutes, false)
                        .replace("%s", seconds, false)
                        .replace("%i", String.format("%0${numbersCnt}d", currentIncrementalNumber))

                    if (newName.isEmpty()) {
                        continue
                    }

                    if ((!newName.contains(".") && path.contains(".")) || (useMediaFileExtension && !".${newName.substringAfterLast(".")}".isMediaFile())) {
                        val extension = path.substringAfterLast(".")
                        newName += ".$extension"
                    }

                    var newPath = "${path.getParentPath()}/$newName"

                    var currentIndex = 0
                    while (activity?.getDoesFilePathExist(newPath) == true) {
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

                    currentIncrementalNumber++
                    activity?.renameFile(path, newPath) {
                        if (it) {
                            pathsCnt--
                            if (pathsCnt == 0) {
                                callback(true)
                            }
                        } else {
                            ignoreClicks = false
                            activity?.toast(R.string.unknown_error_occurred)
                        }
                    }
                } catch (e: Exception) {
                    activity?.showErrorToast(e)
                }
            }
        }
    }
}
