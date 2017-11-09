package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.content.res.Resources
import android.media.ExifInterface
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.sumByLong
import kotlinx.android.synthetic.main.dialog_properties.view.*
import kotlinx.android.synthetic.main.property_item.view.*
import java.io.File
import java.util.*

class PropertiesDialog() {
    private lateinit var mInflater: LayoutInflater
    private lateinit var mPropertyView: ViewGroup
    private lateinit var mResources: Resources

    private var mCountHiddenItems = false
    private var mFilesCnt = 0

    /**
     * A File Properties dialog constructor with an optional parameter, usable at 1 file selected
     *
     * @param activity request activity to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes (reasonable only at directory properties)
     */
    constructor(activity: Activity, path: String, countHiddenItems: Boolean = false) : this() {
        mCountHiddenItems = countHiddenItems
        mInflater = LayoutInflater.from(activity)
        mResources = activity.resources
        val view = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = view.properties_holder

        val file = File(path)
        addProperty(R.string.name, file.name)
        addProperty(R.string.path, file.parent)
        addProperty(R.string.size, "...", R.id.properties_size)

        Thread({
            val size = getItemSize(file).formatSize()
            activity.runOnUiThread {
                view.findViewById<TextView>(R.id.properties_size).property_value.text = size

                if (file.isDirectory) {
                    view.findViewById<TextView>(R.id.properties_file_count).property_value.text = mFilesCnt.toString()
                }
            }

            if (!file.isDirectory) {
                val projection = arrayOf(MediaStore.Images.Media.DATE_MODIFIED)
                val uri = MediaStore.Files.getContentUri("external")
                val selection = "${MediaStore.MediaColumns.DATA} = ?"
                val selectionArgs = arrayOf(path)
                val cursor = activity.contentResolver.query(uri, projection, selection, selectionArgs, null)
                cursor?.use {
                    if (cursor.moveToFirst()) {
                        val dateModified = cursor.getIntValue(MediaStore.Images.Media.DATE_MODIFIED)
                        activity.runOnUiThread {
                            view.findViewById<TextView>(R.id.properties_last_modified).property_value.text = (dateModified * 1000L).formatLastModified()
                        }
                    }
                }
            }
        }).start()

        when {
            file.isDirectory -> {
                addProperty(R.string.direct_children_count, getDirectChildrenCount(file, countHiddenItems))
                addProperty(R.string.files_count, "...", R.id.properties_file_count)
            }
            file.isImageSlow() -> addProperty(R.string.resolution, file.getResolution().formatAsResolution())
            file.isAudioSlow() -> {
                file.getDuration()?.let { addProperty(R.string.duration, it) }
                file.getArtist()?.let { addProperty(R.string.artist, it) }
                file.getAlbum()?.let { addProperty(R.string.album, it) }
            }
            file.isVideoSlow() -> {
                file.getDuration()?.let { addProperty(R.string.duration, it) }
                addProperty(R.string.resolution, file.getResolution().formatAsResolution())
                file.getArtist()?.let { addProperty(R.string.artist, it) }
                file.getAlbum()?.let { addProperty(R.string.album, it) }
            }
        }

        if (file.isDirectory) {
            addProperty(R.string.last_modified, file.lastModified().formatLastModified())
        } else {
            addProperty(R.string.last_modified, "...", R.id.properties_last_modified)
            addExifProperties(path)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.properties)
        }
    }

    /**
     * A File Properties dialog constructor with an optional parameter, usable at multiple items selected
     *
     * @param activity request activity to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes
     */
    constructor(activity: Activity, paths: List<String>, countHiddenItems: Boolean = false) : this() {
        mCountHiddenItems = countHiddenItems
        mInflater = LayoutInflater.from(activity)
        mResources = activity.resources
        val view = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = view.properties_holder

        val files = ArrayList<File>(paths.size)
        paths.forEach { files.add(File(it)) }

        val isSameParent = isSameParent(files)

        addProperty(R.string.items_selected, paths.size.toString())
        if (isSameParent) {
            addProperty(R.string.path, files[0].parent)
        }

        addProperty(R.string.size, "...", R.id.properties_size)
        addProperty(R.string.files_count, "...", R.id.properties_file_count)

        Thread({
            val size = files.sumByLong { getItemSize(it) }.formatSize()
            activity.runOnUiThread {
                view.findViewById<TextView>(R.id.properties_size).property_value.text = size.toString()
                view.findViewById<TextView>(R.id.properties_file_count).property_value.text = mFilesCnt.toString()
            }
        }).start()

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.properties)
        }
    }

    private fun addExifProperties(path: String) {
        val exif = ExifInterface(path)
        val dateTaken = path.getExifDateTaken(exif)
        if (dateTaken.isNotEmpty()) {
            addProperty(R.string.date_taken, dateTaken)
        }

        val cameraModel = path.getExifCameraModel(exif)
        if (cameraModel.isNotEmpty()) {
            addProperty(R.string.camera, cameraModel)
        }

        val exifString = path.getExifProperties(exif)
        if (exifString.isNotEmpty()) {
            addProperty(R.string.exif, exifString)
        }
    }

    private fun isSameParent(files: List<File>): Boolean {
        var parent = files[0].parent
        for (file in files) {
            val curParent = file.parent
            if (curParent != parent) {
                return false
            }

            parent = curParent
        }
        return true
    }

    private fun getDirectChildrenCount(file: File, countHiddenItems: Boolean): String {
        return if (file.listFiles() == null) {
            "0"
        } else {
            file.listFiles().filter { it != null && (!it.isHidden || (it.isHidden && countHiddenItems)) }.size.toString()
        }
    }

    private fun addProperty(labelId: Int, value: String?, viewId: Int = 0) {
        if (value == null)
            return

        mInflater.inflate(R.layout.property_item, mPropertyView, false).apply {
            property_label.text = mResources.getString(labelId)
            property_value.text = value
            mPropertyView.properties_holder.addView(this)

            if (viewId != 0) {
                id = viewId
            }
        }
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
            if (files != null) {
                for (i in files.indices) {
                    if (files[i].isDirectory) {
                        size += getDirectorySize(files[i])
                    } else if (!files[i].isHidden && !dir.isHidden || mCountHiddenItems) {
                        mFilesCnt++
                        size += files[i].length()
                    }
                }
            }
        }
        return size
    }
}
