package com.simplemobiletools.commons.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.provider.DocumentFile
import com.simplemobiletools.commons.R
import java.io.File
import java.util.*

fun Context.hasReadStoragePermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
fun Context.hasWriteStoragePermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.getSDCardPath(): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !hasExternalSDCard()) {
        return ""
    }

    val dirs = File("/storage").listFiles()
    for (dir in dirs) {
        try {
            if (Environment.isExternalStorageRemovable(dir))
                return dir.absolutePath.trimEnd('/')
        } catch (e: Exception) {

        }
    }
    return ""
}

// http://stackoverflow.com/a/13648873/1967672
// dont try to understand, just copy it
fun Context.hasExternalSDCard(): Boolean {
    val reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*"
    var s = ""
    try {
        val process = ProcessBuilder().command("mount").redirectErrorStream(true).start()
        process.waitFor()
        val inputStream = process.inputStream
        val buffer = ByteArray(1024)
        while (inputStream.read(buffer) !== -1) {
            s += String(buffer)
        }
        inputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val lines = s.split("\n".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
    lines.filter { !it.toLowerCase(Locale.US).contains("asec") && it.matches(reg.toRegex()) }
            .map { it.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray() }
            .forEach {
                it.filter { it.startsWith("/") && !it.toLowerCase(Locale.US).contains("vold") }
                        .forEach { return true }
            }
    return false
}

fun Context.getHumanReadablePath(path: String): String {
    return getString(when (path) {
        "/" -> R.string.root
        getInternalStoragePath() -> R.string.internal
        else -> R.string.sd_card
    })
}

fun Context.humanizePath(path: String): String {
    val basePath = path.getBasePath(this)
    return if (basePath == "/")
        "${getHumanReadablePath(basePath)}$path"
    else
        path.replaceFirst(basePath, getHumanReadablePath(basePath))
}

fun Context.getInternalStoragePath() = Environment.getExternalStorageDirectory().toString().trimEnd('/')

@SuppressLint("NewApi")
fun Context.isPathOnSD(path: String) = getSDCardPath().isNotEmpty() && path.startsWith(getSDCardPath())

fun Context.isKitkatPlus() = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT

@SuppressLint("NewApi")
fun Context.needsStupidWritePermissions(path: String) = isPathOnSD(path) && isKitkatPlus() && !getSDCardPath().isEmpty()

@SuppressLint("NewApi")
fun Context.isAStorageRootFolder(path: String): Boolean {
    val trimmed = path.trimEnd('/')
    return trimmed.isEmpty() || trimmed == getInternalStoragePath() || trimmed == getSDCardPath()
}

@SuppressLint("NewApi")
fun Context.getFileDocument(path: String, treeUri: String): DocumentFile {
    var relativePath = path.substring(getSDCardPath().length)
    if (relativePath.startsWith(File.separator))
        relativePath = relativePath.substring(1)

    var document = DocumentFile.fromTreeUri(this, Uri.parse(treeUri))
    val parts = relativePath.split("/")
    for (part in parts) {
        val currDocument = document.findFile(part)
        if (currDocument != null)
            document = currDocument
    }
    return document
}

@SuppressLint("NewApi")
fun Context.tryFastDocumentDelete(file: File): Boolean {
    val document = getFastDocument(file)
    return if (document.isFile) {
        document.delete()
    } else
        false
}

@SuppressLint("NewApi")
fun Context.getFastDocument(file: File): DocumentFile {
    val sdCardPath = getSDCardPath()
    val relativePath = file.absolutePath.substring(sdCardPath.length).trim('/').replace("/", "%2F")
    val sdCardPathPart = sdCardPath.split("/").last().trim('/')
    val fullUri = "${baseConfig.treeUri}/document/$sdCardPathPart%3A$relativePath"
    return DocumentFile.fromSingleUri(this, Uri.parse(fullUri))
}

fun Context.scanFile(file: File, action: () -> Unit) {
    scanFiles(arrayListOf(file), action)
}

fun Context.scanPath(path: String, action: () -> Unit) {
    scanPaths(arrayListOf(path), action)
}

fun Context.scanFiles(files: ArrayList<File>, action: () -> Unit) {
    val allPaths = ArrayList<String>()
    for (file in files) {
        allPaths.addAll(getPaths(file))
    }
    rescanPaths(allPaths, action)
}

fun Context.scanPaths(paths: ArrayList<String>, action: () -> Unit) {
    val allPaths = ArrayList<String>()
    for (path in paths) {
        allPaths.addAll(getPaths(File(path)))
    }
    rescanPaths(allPaths, action)
}

fun Context.rescanPaths(paths: ArrayList<String>, action: () -> Unit) {
    var cnt = paths.size
    MediaScannerConnection.scanFile(this, paths.toTypedArray(), null, { s, uri ->
        if (--cnt == 0)
            action.invoke()
    })
}

fun getPaths(file: File): ArrayList<String> {
    val paths = ArrayList<String>()
    if (file.isDirectory) {
        val files = file.listFiles() ?: return paths
        for (curFile in files) {
            paths.addAll(getPaths(curFile))
        }
    } else {
        paths.add(file.absolutePath)
    }
    return paths
}

fun Context.getFileUri(file: File): Uri {
    return if (file.isImageSlow()) {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    } else if (file.isVideoSlow()) {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    } else {
        MediaStore.Files.getContentUri("external")
    }
}

// these functions update the mediastore instantly, MediaScannerConnection.scanFile takes some time to really get applied
fun Context.deleteFromMediaStore(file: File): Boolean {
    val where = "${MediaStore.MediaColumns.DATA} = ?"
    val args = arrayOf(file.absolutePath)
    return contentResolver.delete(getFileUri(file), where, args) == 1
}

fun Context.updateInMediaStore(oldFile: File, newFile: File): Boolean {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DATA, newFile.absolutePath)
        put(MediaStore.MediaColumns.DISPLAY_NAME, newFile.name)
        put(MediaStore.MediaColumns.TITLE, newFile.name)
    }
    val uri = getFileUri(oldFile)
    val where = "${MediaStore.MediaColumns.DATA} = ?"
    val args = arrayOf(oldFile.absolutePath)
    return contentResolver.update(uri, values, where, args) == 1
}
