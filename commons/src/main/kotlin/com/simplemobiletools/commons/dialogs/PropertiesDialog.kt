package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.content.res.Resources
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_properties.view.*
import kotlinx.android.synthetic.main.property_item.view.*
import java.io.File
import java.util.*

class PropertiesDialog() {
    lateinit var mInflater: LayoutInflater
    lateinit var mPropertyView: ViewGroup
    lateinit var mResources: Resources

    private var mCountHiddenItems = false
    private var mFilesCnt = 0

    /**
     * A File Properties dialog constructor with an optional parameter, usable at 1 file selected
     *
     * @param context has to be activity context to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes (reasonable only at directory properties)
     */
    constructor(context: Context, path: String, countHiddenItems: Boolean = false) : this() {
        mCountHiddenItems = countHiddenItems
        mInflater = LayoutInflater.from(context)
        mResources = context.resources
        val view = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = view.properties_holder

        val file = File(path)
        addProperty(R.string.name, file.name)
        addProperty(R.string.path, file.parent)
        addProperty(R.string.size, getItemSize(file).formatSize())
        addProperty(R.string.last_modified, file.lastModified().formatLastModified())

        if (file.isDirectory) {
            addProperty(R.string.direct_children_count, getDirectChildrenCount(file, countHiddenItems))
            addProperty(R.string.files_count, mFilesCnt.toString())
        } else if (file.isImageSlow()) {
            addProperty(R.string.resolution, file.getImageResolution())
        } else if (file.isAudioSlow()) {
            addProperty(R.string.duration, file.getDuration())
            addProperty(R.string.artist, file.getArtist())
            addProperty(R.string.album, file.getAlbum())
        } else if (file.isVideoSlow()) {
            addProperty(R.string.duration, file.getDuration())
            addProperty(R.string.resolution, file.getVideoResolution())
            addProperty(R.string.artist, file.getArtist())
            addProperty(R.string.album, file.getAlbum())
        }

        AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
            context.setupDialogStuff(view, this, R.string.properties)
        }
    }

    /**
     * A File Properties dialog constructor with an optional parameter, usable at multiple items selected
     *
     * @param context has to be activity context to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes
     */
    constructor(context: Context, paths: List<String>, countHiddenItems: Boolean = false) : this() {
        mCountHiddenItems = countHiddenItems
        mInflater = LayoutInflater.from(context)
        mResources = context.resources
        val view = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = view.properties_holder

        val files = ArrayList<File>(paths.size)
        paths.forEach { files.add(File(it)) }

        val isSameParent = isSameParent(files)

        addProperty(R.string.items_selected, paths.size.toString())
        if (isSameParent)
            addProperty(R.string.path, files[0].parent)
        addProperty(R.string.size, getItemsSize(files).formatSize())
        addProperty(R.string.files_count, mFilesCnt.toString())

        AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
            context.setupDialogStuff(view, this, R.string.properties)
        }
    }

    private fun isSameParent(files: List<File>): Boolean {
        var parent = files[0].parent
        for (file in files) {
            val curParent = file.parent
            if (curParent != parent)
                return false

            parent = curParent
        }
        return true
    }

    private fun getDirectChildrenCount(file: File, countHiddenItems: Boolean): String {
        return file.listFiles().filter { !it.isHidden || (it.isHidden && countHiddenItems) }.size.toString()
    }

    private fun addProperty(labelId: Int, value: String?) {
        if (value == null)
            return

        mInflater.inflate(R.layout.property_item, mPropertyView, false).apply {
            property_label.text = mResources.getString(labelId)
            property_value.text = value
            mPropertyView.properties_holder.addView(this)
        }
    }

    private fun getItemsSize(files: ArrayList<File>): Long {
        var size = 0L
        files.forEach { size += getItemSize(it) }
        return size
    }

    private fun getItemSize(file: File): Long {
        if (file.isDirectory) {
            return getDirectorySize(File(file.path))
        }

        mFilesCnt++
        return file.length()
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    size += getDirectorySize(files[i])
                } else if (!files[i].isHidden && !dir.isHidden || mCountHiddenItems) {
                    mFilesCnt++
                    size += files[i].length()
                }
            }
        }
        return size
    }
}
