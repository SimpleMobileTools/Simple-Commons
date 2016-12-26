package com.simplemobiletools.commons.models

data class FileDirItem(val path: String, val name: String, val isDirectory: Boolean, val children: Int, val size: Long) :
        Comparable<FileDirItem> {

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else
            name.toLowerCase().compareTo(other.name.toLowerCase())
    }
}
