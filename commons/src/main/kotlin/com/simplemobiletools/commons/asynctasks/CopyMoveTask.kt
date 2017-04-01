package com.simplemobiletools.commons.asynctasks

import android.os.AsyncTask
import android.support.v4.util.Pair
import android.util.Log
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
                Log.e(TAG, "copy $e")
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
                val document = activity.getFileDocument(destination.absolutePath, activity.baseConfig.treeUri)
                document?.createDirectory(destination.name)
            } else if (!destination.mkdirs()) {
                throw IOException("Could not create dir ${destination.absolutePath}")
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
            throw IOException("Could not create dir ${directory.absolutePath}")
        }

        val inputStream = FileInputStream(source)
        val out: OutputStream?
        if (activity.needsStupidWritePermissions(destination.absolutePath)) {
            var document = activity.getFileDocument(destination.absolutePath, activity.baseConfig.treeUri) ?: return
            document = document.createFile("", destination.name)

            out = activity.contentResolver.openOutputStream(document.uri)
        } else {
            out = FileOutputStream(destination)
        }

        inputStream.copyTo(out!!)
        activity.scanFile(destination) {}
        if (destination.length() > 0)
            mMovedFiles.add(source)
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
