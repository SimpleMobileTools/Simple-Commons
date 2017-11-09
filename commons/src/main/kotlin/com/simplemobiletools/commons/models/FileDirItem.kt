package com.simplemobiletools.commons.models

import com.simplemobiletools.commons.helpers.SORT_BY_DATE_MODIFIED
import com.simplemobiletools.commons.helpers.SORT_BY_NAME
import com.simplemobiletools.commons.helpers.SORT_BY_SIZE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import java.io.File

data class FileDirItem(val path: String, val name: String, val isDirectory: Boolean, val children: Int, val size: Long) :
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
                    val extension = if (isDirectory) name else path.substringAfterLast('.', "")
                    val otherExtension = if (other.isDirectory) other.name else other.path.substringAfterLast('.', "")
                    result = extension.toLowerCase().compareTo(otherExtension.toLowerCase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }
}
