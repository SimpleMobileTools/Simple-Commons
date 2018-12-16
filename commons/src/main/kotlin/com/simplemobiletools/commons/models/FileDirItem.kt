package com.simplemobiletools.commons.models

import android.content.Context
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import java.io.File

data class FileDirItem(val path: String, val name: String = "", var isDirectory: Boolean = false, var children: Int = 0, var size: Long = 0L) :
        Comparable<FileDirItem> {
    companion object {
        var sorting: Int = 0
    }

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> result = name.toLowerCase().compareTo(other.name.toLowerCase())
                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }
                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    val file = File(path)
                    val otherFile = File(other.path)
                    result = when {
                        file.lastModified() == otherFile.lastModified() -> 0
                        file.lastModified() > otherFile.lastModified() -> 1
                        else -> -1
                    }
                }
                else -> {
                    result = getExtension().toLowerCase().compareTo(other.getExtension().toLowerCase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")

    fun getBubbleText() = when {
        sorting and SORT_BY_SIZE != 0 -> size.formatSize()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> File(path).lastModified().formatDate()
        sorting and SORT_BY_EXTENSION != 0 -> getExtension().toLowerCase()
        else -> name
    }

    fun getProperSize(context: Context, countHidden: Boolean): Long {
        return if (path.startsWith(OTG_PATH)) {
            context.getDocumentFile(path)?.getItemSize(countHidden) ?: 0
        } else {
            File(path).getProperSize(countHidden)
        }
    }

    fun getProperFileCount(context: Context, countHidden: Boolean): Int {
        return if (path.startsWith(OTG_PATH)) {
            context.getDocumentFile(path)?.getFileCount(countHidden) ?: 0
        } else {
            File(path).getFileCount(countHidden)
        }
    }

    fun getDirectChildrenCount(context: Context, countHiddenItems: Boolean): Int {
        return if (path.startsWith(OTG_PATH)) {
            context.getDocumentFile(path)?.listFiles()?.filter { if (countHiddenItems) true else !it.name!!.startsWith(".") }?.size
                    ?: 0
        } else {
            File(path).getDirectChildrenCount(countHiddenItems)
        }
    }

    fun getLastModified(context: Context): Long {
        return if (path.startsWith(OTG_PATH)) {
            context.getFastDocumentFile(path)?.lastModified() ?: 0L
        } else {
            File(path).lastModified()
        }
    }

    fun getParentPath() = path.getParentPath()

    fun getDuration() = path.getDuration()

    fun getFileDurationSeconds() = path.getFileDurationSeconds()

    fun getArtist() = path.getFileArtist()

    fun getAlbum() = path.getFileAlbum()

    fun getSongTitle() = path.getFileSongTitle()

    fun getResolution() = path.getResolution()

    fun getVideoResolution() = path.getVideoResolution()

    fun getImageResolution() = path.getImageResolution()

    fun getPublicUri(context: Context) = context.getDocumentFile(path)?.uri ?: ""
}
