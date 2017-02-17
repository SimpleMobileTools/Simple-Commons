package com.simplemobiletools.commons.asynctasks

import android.content.Context
import android.os.AsyncTask
import android.support.v4.util.Pair
import android.util.Log
import com.simplemobiletools.commons.extensions.*
import java.io.*
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.*

class CopyMoveTask(val context: Context, val deleteAfterCopy: Boolean = false, val treeUri: String = "", val copyMediaOnly: Boolean,
                   listener: CopyMoveTask.CopyMoveListener) : AsyncTask<Pair<ArrayList<File>, File>, Void, Boolean>() {
    private val TAG = CopyMoveTask::class.java.simpleName
    private var mListener: WeakReference<CopyMoveListener>? = null
    private var mMovedFiles: ArrayList<File> = ArrayList()
    lateinit var mFiles: ArrayList<File>

    init {
        mListener = WeakReference(listener)
    }

    override fun doInBackground(vararg params: Pair<ArrayList<File>, File>): Boolean? {
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

        if (deleteAfterCopy) {
            val needsPermissions = context.needsStupidWritePermissions(mMovedFiles[0].absolutePath)
            for (file in mMovedFiles) {
                if (!file.delete()) {
                    if (needsPermissions) {
                        val document = context.getFileDocument(file.absolutePath, treeUri)

                        // double check we have the uri to the proper file path, not some parent folder
                        val uri = URLDecoder.decode(document.uri.toString(), "UTF-8")
                        val filename = URLDecoder.decode(file.absolutePath.getFilenameFromPath(), "UTF-8")
                        if (uri.endsWith(filename)) {
                            document.delete()
                        }
                    }
                }
            }
        }

        context.scanFiles(mFiles) {}
        context.scanFiles(mMovedFiles) {}

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
            if (context.needsStupidWritePermissions(destination.absolutePath)) {
                val document = context.getFileDocument(destination.absolutePath, treeUri)
                document.createDirectory(destination.name)
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
            if (context.needsStupidWritePermissions(destination.absolutePath)) {
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
        if (copyMediaOnly && !source.isImageVideoGif())
            return

        val directory = destination.parentFile
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Could not create dir ${directory.absolutePath}")
        }

        val inputStream = FileInputStream(source)
        val out: OutputStream?
        if (context.needsStupidWritePermissions(destination.absolutePath)) {
            var document = context.getFileDocument(destination.absolutePath, treeUri)
            document = document.createFile("", destination.name)

            out = context.contentResolver.openOutputStream(document.uri)
        } else {
            out = FileOutputStream(destination)
        }

        copyStream(inputStream, out)
        context.scanFile(destination) {}
        mMovedFiles.add(source)
    }

    private fun copyStream(inputStream: InputStream, out: OutputStream?) {
        val buf = ByteArray(1024)
        var len: Int
        while (true) {
            len = inputStream.read(buf)
            if (len <= 0)
                break
            out?.write(buf, 0, len)
        }
    }

    override fun onPostExecute(success: Boolean) {
        val listener = mListener?.get() ?: return

        if (success) {
            listener.copySucceeded(deleteAfterCopy, mFiles.size == mMovedFiles.size)
        } else {
            listener.copyFailed()
        }
    }

    interface CopyMoveListener {
        fun copySucceeded(deleted: Boolean, copiedAll: Boolean)

        fun copyFailed()
    }
}
