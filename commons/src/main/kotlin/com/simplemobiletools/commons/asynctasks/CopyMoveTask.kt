package com.simplemobiletools.commons.asynctasks

import android.content.ContentValues
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.provider.DocumentFile
import android.support.v4.util.Pair
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.CONFLICT_OVERWRITE
import com.simplemobiletools.commons.helpers.CONFLICT_SKIP
import com.simplemobiletools.commons.interfaces.CopyMoveListener
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.*

class CopyMoveTask(val activity: BaseSimpleActivity, val copyOnly: Boolean = false, val copyMediaOnly: Boolean, val conflictResolutions: LinkedHashMap<String, Int>,
                   listener: CopyMoveListener) : AsyncTask<Pair<ArrayList<File>, File>, Void, Boolean>() {
    private var mListener: WeakReference<CopyMoveListener>? = null
    private var mMovedFiles: ArrayList<File> = ArrayList()
    private var mDocuments = LinkedHashMap<String, DocumentFile?>()
    lateinit var mFiles: ArrayList<File>

    init {
        mListener = WeakReference(listener)
    }

    override fun doInBackground(vararg params: Pair<ArrayList<File>, File>): Boolean? {
        if (params.isEmpty()) {
            return false
        }

        val pair = params[0]
        mFiles = pair.first!!

        for (file in mFiles) {
            try {
                val newFile = File(pair.second, file.name)
                if (newFile.exists()) {
                    val resolution = getConflictResolution(newFile)
                    if (resolution == CONFLICT_SKIP) {
                        continue
                    } else if (resolution == CONFLICT_OVERWRITE) {
                        activity.deleteFilesBg(arrayListOf(newFile), true)
                    }
                }

                copy(file, newFile)
            } catch (e: Exception) {
                activity.toast(e.toString())
                return false
            }
        }

        if (!copyOnly) {
            activity.deleteFiles(mMovedFiles) {}
        }

        activity.scanFiles(mFiles) {}
        return true
    }

    private fun getConflictResolution(file: File): Int {
        return if (conflictResolutions.size == 1 && conflictResolutions.containsKey("")) {
            conflictResolutions[""]!!
        } else if (conflictResolutions.containsKey(file.absolutePath)) {
            conflictResolutions[file.absolutePath]!!
        } else {
            CONFLICT_SKIP
        }
    }

    private fun copy(source: File, destination: File) {
        if (source.isDirectory) {
            copyDirectory(source, destination)
        } else {
            copyFile(source, destination)
        }
    }

    private fun copyDirectory(source: File, destination: File) {
        if (!activity.createDirectorySync(destination)) {
            val error = String.format(activity.getString(R.string.could_not_create_folder), destination.absolutePath)
            activity.showErrorToast(error)
            return
        }

        val children = source.list()
        for (child in children) {
            val newFile = File(destination, child)
            if (newFile.exists()) {
                continue
            }

            val oldFile = File(source, child)
            copy(oldFile, newFile)
        }
        mMovedFiles.add(source)
    }

    private fun copyFile(source: File, destination: File) {
        if (copyMediaOnly && !source.absolutePath.isImageVideoGif()) {
            return
        }

        val directory = destination.parentFile
        if (!activity.createDirectorySync(directory)) {
            val error = String.format(activity.getString(R.string.could_not_create_folder), directory.absolutePath)
            activity.showErrorToast(error)
            return
        }

        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            if (!mDocuments.containsKey(destination.parent) && activity.needsStupidWritePermissions(destination.absolutePath)) {
                mDocuments[destination.parent] = activity.getFileDocument(destination.parent)
            }

            out = activity.getFileOutputStreamSync(destination.absolutePath, source.getMimeType(), mDocuments[destination.parent])

            inputStream = FileInputStream(source)
            inputStream.copyTo(out!!)

            if (source.length() == destination.length()) {
                mMovedFiles.add(source)
                if (activity.baseConfig.keepLastModified) {
                    copyOldLastModified(source, destination)
                } else {
                    activity.scanFile(destination) {}
                }
            }
        } catch (e: Exception) {
            activity.showErrorToast(e)
        } finally {
            inputStream?.close()
            out?.close()
        }
    }

    override fun onPostExecute(success: Boolean) {
        val listener = mListener?.get() ?: return

        if (success) {
            listener.copySucceeded(copyOnly, mMovedFiles.size >= mFiles.size)
        } else {
            listener.copyFailed()
        }
    }

    private fun copyOldLastModified(source: File, destination: File) {
        val projection = arrayOf(
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_MODIFIED)
        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        var selectionArgs = arrayOf(source.absolutePath)
        val cursor = activity.applicationContext.contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (cursor.moveToFirst()) {
                val dateTaken = cursor.getLongValue(MediaStore.Images.Media.DATE_TAKEN)
                val dateModified = cursor.getIntValue(MediaStore.Images.Media.DATE_MODIFIED)

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, dateTaken)
                    put(MediaStore.Images.Media.DATE_MODIFIED, dateModified)
                }

                selectionArgs = arrayOf(destination.absolutePath)
                activity.scanFile(destination) {
                    activity.applicationContext.contentResolver.update(uri, values, selection, selectionArgs)
                }
            }
        }
    }
}
