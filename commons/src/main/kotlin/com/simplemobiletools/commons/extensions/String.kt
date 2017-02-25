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

val String.photoExtensions: Array<String> get() = arrayOf("jpg", "png", "jpeg", "bmp", "webp", "tiff")
val String.videoExtensions: Array<String> get() = arrayOf("webm", "mkv", "flv", "vob", "avi", "wmv", "mp4", "ogv", "qt", "m4p", "mpg", "m4v", "mp2", "mpeg", "3gp")

fun String.isImageVideoGif() = isImageFast() || isVideoFast() || isGif()

fun String.isGif() = endsWith(".gif", true)

// fast extension check, not guaranteed to be accurate
fun String.isVideoFast(): Boolean {
    return videoExtensions.any { endsWith(".$it", true) }
}

// fast extension check, not guaranteed to be accurate
fun String.isImageFast(): Boolean {
    return photoExtensions.any { endsWith(".$it", true) }
}
