package com.simplemobiletools.commons.asynctasks

import android.os.AsyncTask
import android.support.v4.provider.DocumentFile
import android.support.v4.util.Pair
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import java.io.*
import java.lang.ref.WeakReference
import java.util.*

class CopyMoveTask(val activity: BaseSimpleActivity, val copyOnly: Boolean = false, val copyMediaOnly: Boolean,
                   listener: CopyMoveTask.CopyMoveListener) : AsyncTask<Pair<ArrayList<File>, File>, Void, Boolean>() {
    private val TAG = CopyMoveTask::class.java.simpleName
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
                activity.toast(e.localizedMessage)
                return false
            }
        }

        if (!copyOnly) {
            activity.deleteFiles(mMovedFiles) {}
        }

        activity.scanFiles(mFiles) {}
        return true
    }

    @Throws(Exception::class)
    private fun copy(source: File, destination: File) {
        if (source.isDirectory) {
            copyDirectory(source, destination)
        } else {
            copyFile(source, destination)
        }
    }

    private fun copyDirectory(source: File, destination: File) {
        if (!destination.exists()) {
            if (activity.needsStupidWritePermissions(destination.absolutePath)) {
                val document = activity.getFastDocument(destination) ?: return
                document.createDirectory(destination.name)
            } else if (!destination.mkdirs()) {
                val error = String.format(activity.getString(R.string.could_not_create_folder), destination.absolutePath)
                throw IOException(error)
            }
        }

        val children = source.list()
        for (child in children) {
            val newFile = File(destination, child)
            if (newFile.exists())
                continue

            val curFile = File(source, child)
            if (activity.needsStupidWritePermissions(destination.absolutePath)) {
                if (newFile.isDirectory) {
                    copyDirectory(curFile, newFile)
                } else {
                    copyFile(curFile, newFile)
                }
            } else {
                copy(curFile, newFile)
            }
        }
    }

    private fun copyFile(source: File, destination: File) {
        if (copyMediaOnly && !source.absolutePath.isImageVideoGif())
            return

        val directory = destination.parentFile
        if (!directory.exists() && !directory.mkdirs()) {
            val error = String.format(activity.getString(R.string.could_not_create_folder), directory.absolutePath)
            throw IOException(error)
        }

        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            if (activity.needsStupidWritePermissions(destination.absolutePath)) {
                if (mDocument == null) {
                    mDocument = activity.getFileDocument(destination.parent)
                }

                if (mDocument == null) {
                    val error = String.format(activity.getString(R.string.could_not_create_file), destination.parent)
                    throw IOException(error)
                }

                val newDocument = mDocument!!.createFile(source.getMimeType(), destination.name)
                out = activity.contentResolver.openOutputStream(newDocument!!.uri)
            } else {
                out = FileOutputStream(destination)
            }

            inputStream = FileInputStream(source)
            inputStream.copyTo(out!!)
            activity.scanFile(destination) {}
            if (source.length() == destination.length())
                mMovedFiles.add(source)
        } finally {
            inputStream?.close()
            out?.close()
        }
    }

    override fun onPostExecute(success: Boolean) {
        val listener = mListener?.get() ?: return

        if (success) {
            listener.copySucceeded(copyOnly, mFiles.size == mMovedFiles.size)
        } else {
            listener.copyFailed()
        }
    }

    interface CopyMoveListener {
        fun copySucceeded(copyOnly: Boolean, copiedAll: Boolean)

        fun copyFailed()
    }
}
