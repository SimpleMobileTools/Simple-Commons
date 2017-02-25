package com.simplemobiletools.commons.extensions

import android.content.Context

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getBasePath(context: Context): String {
    return if (startsWith(context.internalStoragePath))
        context.internalStoragePath
    else if (!context.sdCardPath.isEmpty() && startsWith(context.sdCardPath))
        context.sdCardPath
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

val String.photoExtensions: Array<String> get() = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp")
val String.videoExtensions: Array<String> get() = arrayOf(".mp4", ".mkv", ".webm", ".avi")

fun String.isImageVideoGif() = isImageFast() || isVideoFast() || isGif()

fun String.isGif() = endsWith(".gif", true)

// fast extension check, not guaranteed to be accurate
fun String.isVideoFast() = videoExtensions.any { endsWith(it, true) }

// fast extension check, not guaranteed to be accurate
fun String.isImageFast() = photoExtensions.any { endsWith(it, true) }
