package com.simplemobiletools.commons.asynctasks

import android.os.AsyncTask
import android.support.v4.provider.DocumentFile
import android.support.v4.util.Pair
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.*

class CopyMoveTask(val activity: BaseSimpleActivity, val copyOnly: Boolean = false, val copyMediaOnly: Boolean,
                   listener: CopyMoveTask.CopyMoveListener) : AsyncTask<Pair<ArrayList<File>, File>, Void, Boolean>() {
    private var mListener: WeakReference<CopyMoveListener>? = null
    private var mMovedFiles: ArrayList<File> = ArrayList()
    private var mDocument: DocumentFile? = null
    lateinit var mFiles: ArrayList<File>

    init {
        mListener = WeakReference(listener)
    }

    override fun doInBackground(vararg params: Pair<ArrayList<File>, File>): Boolean? {
        if (params.isEmpty())
            return false

        val pair = params[0]
        mFiles = pair.first

        for (file in mFiles) {
            try {
                val curFile = File(pair.second, file.name)
                if (curFile.exists())
                    continue

                copy(file, curFile)
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
            if (newFile.exists())
                continue

            val oldFile = File(source, child)
            copy(oldFile, newFile)
        }
        mMovedFiles.add(source)
    }

    private fun copyFile(source: File, destination: File) {
        if (copyMediaOnly && !source.absolutePath.isImageVideoGif())
            return

        val directory = destination.parentFile
        if (!activity.createDirectorySync(directory)) {
            val error = String.format(activity.getString(R.string.could_not_create_folder), directory.absolutePath)
            activity.showErrorToast(error)
            return
        }

        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            if (mDocument == null && activity.needsStupidWritePermissions(destination.absolutePath)) {
                mDocument = activity.getFileDocument(destination.parent)
            }

            out = activity.getFileOutputStreamSync(destination.absolutePath, source.getMimeType(), mDocument)

            inputStream = FileInputStream(source)
            inputStream.copyTo(out!!)
            activity.scanFile(destination) {
                activity.copyDates(source, destination)
            }
            if (source.length() == destination.length()) {
                mMovedFiles.add(source)
            }
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

    interface CopyMoveListener {
        fun copySucceeded(copyOnly: Boolean, copiedAll: Boolean)

        fun copyFailed()
    }
}
