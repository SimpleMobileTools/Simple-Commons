package com.simplemobiletools.commons.extensions

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File

fun File.isImageVideoGif() = isImageFast() || isVideoFast() || isGif()

fun File.isGif() = path.endsWith(".gif", true)

// fast extension check, not guaranteed to be accurate
fun File.isImageFast(): Boolean {
    val photoExtensions = arrayOf("jpg", "png", "jpeg", "bmp", "webp", "tiff")
    return photoExtensions.any { path.endsWith(".$it", true) }
}

fun File.isImageSlow() = isImageFast() || getMimeType().startsWith("image")

// fast extension check, not guaranteed to be accurate
fun File.isVideoFast(): Boolean {
    val videoExtensions = arrayOf("webm", "mkv", "flv", "vob", "avi", "wmv", "mp4", "ogv", "qt", "m4p", "mpg", "m4v", "mp2", "mpeg", "3gp")
    return videoExtensions.any { path.endsWith(".$it", true) }
}

fun File.isVideoSlow() = isVideoFast() || getMimeType().startsWith("video")
fun File.isAudioSlow() = getMimeType().startsWith("audio")

fun File.getMimeType(default: String = ""): String {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
    } catch (ignored: Exception) {
        default
    }
}

fun File.getDuration(): String {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    val timeInMs = java.lang.Long.parseLong(time)
    return (timeInMs / 1000).toInt().getFormattedDuration()
}

fun File.getArtist(): String? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
}

fun File.getAlbum(): String? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
}

fun File.getVideoResolution(): String {
    try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        return "$width x $height ${getMPx(width, height)}"
    } catch (ignored: Exception) {

    }
    return ""
}

fun File.getImageResolution(): String {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    val width = options.outWidth
    val height = options.outHeight
    return "$width x $height ${getMPx(width, height)}"
}

fun getMPx(width: Int, height: Int): String {
    val px = width * height / 1000000.toFloat()
    val rounded = Math.round(px * 10) / 10.toFloat()
    return "(${rounded}MP)"
}
