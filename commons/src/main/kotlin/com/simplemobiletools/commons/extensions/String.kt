package com.simplemobiletools.commons.extensions

import android.content.Context

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getBasePath(context: Context): String {
    return if (startsWith(context.getInternalStoragePath()))
        context.getInternalStoragePath()
    else if (!context.getSDCardPath().isEmpty() && startsWith(context.getSDCardPath()))
        context.getSDCardPath()
    else
        "/"
}

fun String.isAValidFilename(): Boolean {
    val ILLEGAL_CHARACTERS = charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
    ILLEGAL_CHARACTERS.forEach {
        if (contains(it))
            return false
    }
    return true
}
