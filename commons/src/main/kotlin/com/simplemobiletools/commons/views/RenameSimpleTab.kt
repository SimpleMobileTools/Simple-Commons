package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.interfaces.RenameTab
import kotlinx.android.synthetic.main.tab_rename_simple.view.*

class RenameSimpleTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), RenameTab {
    var ignoreClicks = false
    var activity: BaseSimpleActivity? = null
    var paths = ArrayList<String>()

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(rename_simple_holder)
    }

    override fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>) {
        this.activity = activity
        this.paths = paths
    }

    override fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit) {
        val valueToAdd = rename_simple_value.text.toString()
        val append = rename_simple_radio_group.checkedRadioButtonId == rename_simple_radio_append.id

        if (valueToAdd.isEmpty()) {
            callback(false)
            return
        }

        if (!valueToAdd.isAValidFilename()) {
            activity?.toast(R.string.invalid_name)
            return
        }

        val validPaths = paths.filter { activity?.getDoesFilePathExist(it) == true }
        val sdFilePath = validPaths.firstOrNull { activity?.isPathOnSD(it) == true } ?: validPaths.firstOrNull()
        if (sdFilePath == null) {
            activity?.toast(R.string.unknown_error_occurred)
            return
        }

        activity?.handleSAFDialog(sdFilePath) {
            if (!it) {
                return@handleSAFDialog
            }

            ignoreClicks = true
            var pathsCnt = validPaths.size
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

                if (activity?.getDoesFilePathExist(newPath) == true) {
                    continue
                }

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
            }
        }
    }
}
