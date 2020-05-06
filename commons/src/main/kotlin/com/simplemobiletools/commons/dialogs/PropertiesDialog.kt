package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.content.res.Resources
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.helpers.sumByInt
import com.simplemobiletools.commons.helpers.sumByLong
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.android.synthetic.main.dialog_properties.view.*
import kotlinx.android.synthetic.main.property_item.view.*
import java.util.*

class PropertiesDialog() {
    private lateinit var mInflater: LayoutInflater
    private lateinit var mPropertyView: ViewGroup
    private lateinit var mResources: Resources
    private lateinit var mActivity: Activity

    /**
     * A File Properties dialog constructor with an optional parameter, usable at 1 file selected
     *
     * @param activity request activity to avoid some Theme.AppCompat issues
     * @param path the file path
     * @param countHiddenItems toggle determining if we will count hidden files themselves and their sizes (reasonable only at directory properties)
     */
    constructor(activity: Activity, path: String, countHiddenItems: Boolean = false) : this() {
        if (!activity.getDoesFilePathExist(path) && !path.startsWith("content://")) {
            activity.toast(String.format(activity.getString(R.string.source_file_doesnt_exist), path))
            return
        }

        mActivity = activity
        mInflater = LayoutInflater.from(activity)
        mResources = activity.resources
        val view = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = view.properties_holder!!

        val fileDirItem = FileDirItem(path, path.getFilenameFromPath(), activity.getIsPathDirectory(path))
        addProperty(R.string.name, fileDirItem.name)
        addProperty(R.string.path, fileDirItem.getParentPath())
        addProperty(R.string.size, "…", R.id.properties_size)

        ensureBackgroundThread {
            val fileCount = fileDirItem.getProperFileCount(activity, countHiddenItems)
            val size = fileDirItem.getProperSize(activity, countHiddenItems).formatSize()
            activity.runOnUiThread {
                view.findViewById<TextView>(R.id.properties_size).property_value.text = size

                if (fileDirItem.isDirectory) {
                    view.findViewById<TextView>(R.id.properties_file_count).property_value.text = fileCount.toString()
                }
            }

            if (!fileDirItem.isDirectory) {
                val projection = arrayOf(MediaStore.Images.Media.DATE_MODIFIED)
                val uri = MediaStore.Files.getContentUri("external")
                val selection = "${MediaStore.MediaColumns.DATA} = ?"
                val selectionArgs = arrayOf(path)
                val cursor = activity.contentResolver.query(uri, projection, selection, selectionArgs, null)
                cursor?.use {
                    if (cursor.moveToFirst()) {
                        val dateModified = cursor.getLongValue(MediaStore.Images.Media.DATE_MODIFIED) * 1000L
                        updateLastModified(activity, view, dateModified)
                    } else {
                        updateLastModified(activity, view, fileDirItem.getLastModified(activity))
                    }
                }

                val exif = if (isNougatPlus() && activity.isPathOnOTG(fileDirItem.path)) {
                    ExifInterface((activity as BaseSimpleActivity).getFileInputStreamSync(fileDirItem.path)!!)
                } else if (isNougatPlus() && fileDirItem.path.startsWith("content://")) {
                    ExifInterface(activity.contentResolver.openInputStream(Uri.parse(fileDirItem.path)))
                } else {
                    ExifInterface(fileDirItem.path)
                }

                val latLon = FloatArray(2)
                if (exif.getLatLong(latLon)) {
                    activity.runOnUiThread {
                        addProperty(R.string.gps_coordinates, "${latLon[0]}, ${latLon[1]}")
                    }
                }

                val altitude = exif.getAltitude(0.0)
                if (altitude != 0.0) {
                    activity.runOnUiThread {
                        addProperty(R.string.altitude, "${altitude}m")
                    }
                }
            }
        }

        when {
            fileDirItem.isDirectory -> {
                addProperty(R.string.direct_children_count, fileDirItem.getDirectChildrenCount(activity, countHiddenItems).toString())
                addProperty(R.string.files_count, "…", R.id.properties_file_count)
            }
            fileDirItem.path.isImageSlow() -> {
                fileDirItem.getResolution(activity)?.let { addProperty(R.string.resolution, it.formatAsResolution()) }
            }
            fileDirItem.path.isAudioSlow() -> {
                fileDirItem.getDuration(activity)?.let { addProperty(R.string.duration, it) }
                fileDirItem.getTitle(activity)?.let { addProperty(R.string.song_title, it) }
                fileDirItem.getArtist(activity)?.let { addProperty(R.string.artist, it) }
                fileDirItem.getAlbum(activity)?.let { addProperty(R.string.album, it) }
            }
            fileDirItem.path.isVideoSlow() -> {
                fileDirItem.getDuration(activity)?.let { addProperty(R.string.duration, it) }
                fileDirItem.getResolution(activity)?.let { addProperty(R.string.resolution, it.formatAsResolution()) }
                fileDirItem.getArtist(activity)?.let { addProperty(R.string.artist, it) }
                fileDirItem.getAlbum(activity)?.let { addProperty(R.string.album, it) }
            }
        }

        if (fileDirItem.isDirectory) {
            addProperty(R.string.last_modified, fileDirItem.getLastModified(activity).formatDate(activity))
        } else {
            addProperty(R.string.last_modified, "…", R.id.properties_last_modified)
            try {
                addExifProperties(path, activity)
            } catch (e: Exception) {
                activity.showErrorToast(e)
                return
            }
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.properties)
            }
    }

    private fun updateLastModified(activity: Activity, view: View, timestamp: Long) {
        activity.runOnUiThread {
            view.findViewById<TextView>(R.id.properties_last_modified).property_value.text = timestamp.formatDate(activity)
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
        mActivity = activity
        mInflater = LayoutInflater.from(activity)
        mResources = activity.resources
        val view = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = view.properties_holder

        val fileDirItems = ArrayList<FileDirItem>(paths.size)
        paths.forEach {
            val fileDirItem = FileDirItem(it, it.getFilenameFromPath(), activity.getIsPathDirectory(it))
            fileDirItems.add(fileDirItem)
        }

        val isSameParent = isSameParent(fileDirItems)

        addProperty(R.string.items_selected, paths.size.toString())
        if (isSameParent) {
            addProperty(R.string.path, fileDirItems[0].getParentPath())
        }

        addProperty(R.string.size, "…", R.id.properties_size)
        addProperty(R.string.files_count, "…", R.id.properties_file_count)

        ensureBackgroundThread {
            val fileCount = fileDirItems.sumByInt { it.getProperFileCount(activity, countHiddenItems) }
            val size = fileDirItems.sumByLong { it.getProperSize(activity, countHiddenItems) }.formatSize()
            activity.runOnUiThread {
                view.findViewById<TextView>(R.id.properties_size).property_value.text = size
                view.findViewById<TextView>(R.id.properties_file_count).property_value.text = fileCount.toString()
            }
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.properties)
            }
    }

    private fun addExifProperties(path: String, activity: Activity) {
        val exif = if (isNougatPlus() && activity.isPathOnOTG(path)) {
            ExifInterface((activity as BaseSimpleActivity).getFileInputStreamSync(path)!!)
        } else if (isNougatPlus() && path.startsWith("content://")) {
            ExifInterface(activity.contentResolver.openInputStream(Uri.parse(path)))
        } else {
            ExifInterface(path)
        }

        val dateTaken = exif.getExifDateTaken(activity)
        if (dateTaken.isNotEmpty()) {
            addProperty(R.string.date_taken, dateTaken)
        }

        val cameraModel = exif.getExifCameraModel()
        if (cameraModel.isNotEmpty()) {
            addProperty(R.string.camera, cameraModel)
        }

        val exifString = exif.getExifProperties()
        if (exifString.isNotEmpty()) {
            addProperty(R.string.exif, exifString)
        }
    }

    private fun isSameParent(fileDirItems: List<FileDirItem>): Boolean {
        var parent = fileDirItems[0].getParentPath()
        for (file in fileDirItems) {
            val curParent = file.getParentPath()
            if (curParent != parent) {
                return false
            }

            parent = curParent
        }
        return true
    }

    private fun addProperty(labelId: Int, value: String?, viewId: Int = 0) {
        if (value == null)
            return

        mInflater.inflate(R.layout.property_item, mPropertyView, false).apply {
            property_label.text = mResources.getString(labelId)
            property_value.text = value
            mPropertyView.properties_holder.addView(this)

            setOnLongClickListener {
                mActivity.copyToClipboard(property_value.value)
                true
            }

            if (labelId == R.string.gps_coordinates) {
                setOnClickListener {
                    mActivity.showLocationOnMap(value)
                }
            }

            if (viewId != 0) {
                id = viewId
            }
        }
    }
}
