package com.simplemobiletools.commons.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Looper
import android.provider.DocumentsContract
import android.support.v4.provider.DocumentFile
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.DonateDialog
import com.simplemobiletools.commons.dialogs.WhatsNewDialog
import com.simplemobiletools.commons.dialogs.WritePermissionDialog
import com.simplemobiletools.commons.models.Release
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

fun Activity.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    if (isOnMainThread()) {
        Toast.makeText(this, id, length).show()
    } else {
        runOnUiThread {
            Toast.makeText(this, id, length).show()
        }
    }
}

fun Activity.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    if (isOnMainThread()) {
        Toast.makeText(this, msg, length).show()
    } else {
        runOnUiThread {
            Toast.makeText(this, msg, length).show()
        }
    }
}

@SuppressLint("NewApi")
fun Activity.storeStoragePaths() {
    baseConfig.appRunCount++
    if (baseConfig.appRunCount == 50 || baseConfig.appRunCount == 300 || baseConfig.appRunCount == 1000) {
        DonateDialog(this)
    }

    Thread({
        baseConfig.internalStoragePath = getInternalStoragePath()
        baseConfig.sdCardPath = getSDCardPath().trimEnd('/')
    }).start()
}

fun Activity.isShowingSAFDialog(file: File, treeUri: String, requestCode: Int): Boolean {
    return if ((needsStupidWritePermissions(file.absolutePath) && treeUri.isEmpty())) {
        runOnUiThread {
            WritePermissionDialog(this) {
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    if (resolveActivity(packageManager) == null) {
                        type = "*/*"
                    }

                    if (resolveActivity(packageManager) != null) {
                        startActivityForResult(this, requestCode)
                    } else {
                        toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

fun Activity.launchViewIntent(id: Int) = launchViewIntent(resources.getString(id))

fun Activity.launchViewIntent(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun BaseSimpleActivity.checkWhatsNew(releases: List<Release>, currVersion: Int) {
    if (isFirstRunEver()) {
        baseConfig.lastVersion = currVersion
        return
    }

    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty())
        WhatsNewDialog(this, newReleases)

    baseConfig.lastVersion = currVersion
}

fun BaseSimpleActivity.isFirstRunEver(): Boolean {
    try {
        val firstInstallTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime
        val lastUpdateTime = packageManager.getPackageInfo(packageName, 0).lastUpdateTime
        return firstInstallTime == lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {

    }
    return false
}

fun BaseSimpleActivity.deleteFolders(folders: ArrayList<File>, deleteMediaOnly: Boolean = true, callback: (wasSuccess: Boolean) -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFoldersBg(folders, deleteMediaOnly, callback)
        }.start()
    } else {
        deleteFoldersBg(folders, deleteMediaOnly, callback)
    }
}

fun BaseSimpleActivity.deleteFoldersBg(folders: ArrayList<File>, deleteMediaOnly: Boolean = true, callback: (wasSuccess: Boolean) -> Unit) {
    var wasSuccess = false
    var needPermissionForPath = ""
    for (file in folders) {
        if (needsStupidWritePermissions(file.absolutePath) && baseConfig.treeUri.isEmpty()) {
            needPermissionForPath = file.absolutePath
            break
        }
    }

    handleSAFDialog(File(needPermissionForPath)) {
        folders.forEachIndexed { index, folder ->
            deleteFolderBg(folder, deleteMediaOnly) {
                if (it)
                    wasSuccess = true

                if (index == folders.size - 1) {
                    callback(wasSuccess)
                }
            }
        }
    }
}

fun BaseSimpleActivity.deleteFolder(folder: File, deleteMediaOnly: Boolean = true, callback: (wasSuccess: Boolean) -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFolderBg(folder, deleteMediaOnly, callback)
        }.start()
    } else {
        deleteFolderBg(folder, deleteMediaOnly, callback)
    }
}

fun BaseSimpleActivity.deleteFolderBg(folder: File, deleteMediaOnly: Boolean = true, callback: (wasSuccess: Boolean) -> Unit) {
    if (folder.exists()) {
        val filesArr = folder.listFiles()
        if (filesArr == null) {
            callback(true)
            return
        }

        val filesList = (filesArr as Array).toList()
        val files = filesList.filter { !deleteMediaOnly || it.isImageVideoGif() }
        for (file in files) {
            deleteFileBg(file, false) { }
        }

        if (folder.listFiles()?.isEmpty() == true) {
            deleteFileBg(folder, true) { }
        }
    }
    callback(true)
}

fun BaseSimpleActivity.deleteFiles(files: ArrayList<File>, allowDeleteFolder: Boolean = false, callback: (wasSuccess: Boolean) -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFilesBg(files, allowDeleteFolder, callback)
        }.start()
    } else {
        deleteFilesBg(files, allowDeleteFolder, callback)
    }
}

fun BaseSimpleActivity.deleteFilesBg(files: ArrayList<File>, allowDeleteFolder: Boolean = false, callback: (wasSuccess: Boolean) -> Unit) {
    if (files.isEmpty()) {
        callback(true)
        return
    }

    var wasSuccess = false
    handleSAFDialog(files[0]) {
        files.forEachIndexed { index, file ->
            deleteFileBg(file, allowDeleteFolder) {
                if (it)
                    wasSuccess = true

                if (index == files.size - 1) {
                    callback(wasSuccess)
                }
            }
        }
    }
}

fun BaseSimpleActivity.deleteFile(file: File, allowDeleteFolder: Boolean = false, callback: (wasSuccess: Boolean) -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFileBg(file, allowDeleteFolder, callback)
        }.start()
    } else {
        deleteFileBg(file, allowDeleteFolder, callback)
    }
}

fun BaseSimpleActivity.deleteFileBg(file: File, allowDeleteFolder: Boolean = false, callback: (wasSuccess: Boolean) -> Unit) {
    var fileDeleted = !file.exists() || file.delete()
    if (fileDeleted) {
        rescanDeletedFile(file) {
            callback(true)
        }
    } else {
        if (file.isDirectory || allowDeleteFolder) {
            fileDeleted = deleteRecursively(file)
        }

        if (!fileDeleted && isPathOnSD(file.absolutePath)) {
            handleSAFDialog(file) {
                fileDeleted = tryFastDocumentDelete(file, allowDeleteFolder)
                if (!fileDeleted) {
                    val document = getFileDocument(file.absolutePath)
                    fileDeleted = (document?.isFile == true || allowDeleteFolder) && DocumentsContract.deleteDocument(contentResolver, document?.uri)
                }

                if (fileDeleted) {
                    rescanDeletedFile(file) {
                        callback(true)
                    }
                }
            }
        }
    }
}

fun BaseSimpleActivity.rescanDeletedFile(file: File, callback: () -> Unit) {
    if (deleteFromMediaStore(file)) {
        callback()
    } else {
        MediaScannerConnection.scanFile(applicationContext, arrayOf(file.absolutePath), null, { s, uri ->
            try {
                contentResolver.delete(uri, null, null)
            } catch (ignored: Exception) {
            }
            callback()
        })
    }
}

private fun deleteRecursively(file: File): Boolean {
    if (file.isDirectory) {
        val files = file.listFiles() ?: return file.delete()
        for (child in files) {
            deleteRecursively(child)
        }
    }

    return file.delete()
}

fun BaseSimpleActivity.renameFile(oldFile: File, newFile: File, callback: (success: Boolean) -> Unit) {
    if (needsStupidWritePermissions(newFile.absolutePath)) {
        handleSAFDialog(newFile) {
            val document = getFileDocument(oldFile.absolutePath)
            if (document == null) {
                callback(false)
                return@handleSAFDialog
            }

            try {
                val uri = DocumentsContract.renameDocument(contentResolver, document.uri, newFile.name)
                if (document.uri != uri) {
                    scanFiles(arrayListOf(oldFile, newFile)) {
                        callback(true)
                    }
                } else {
                    callback(false)
                }
            } catch (e: SecurityException) {
                toast(R.string.unknown_error_occurred)
                callback(false)
            }
        }
    } else if (oldFile.renameTo(newFile)) {
        newFile.setLastModified(System.currentTimeMillis())
        if (newFile.isDirectory) {
            deleteFromMediaStore(oldFile)
            scanFile(newFile) {
                callback(true)
            }
        } else {
            updateInMediaStore(oldFile, newFile)
            callback(true)
        }
    } else {
        callback(false)
    }
}

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showKeyboard(et: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun BaseSimpleActivity.getFileOutputStream(file: File, callback: (outputStream: OutputStream) -> Unit) {
    if (needsStupidWritePermissions(file.absolutePath)) {
        handleSAFDialog(file) {
            var document = getFileDocument(file.absolutePath) ?: return@handleSAFDialog
            if (!file.exists()) {
                document = document.createFile("", file.name)
            }
            callback(contentResolver.openOutputStream(document.uri))
        }
    } else {
        callback(FileOutputStream(file))
    }
}

@SuppressLint("NewApi")
fun BaseSimpleActivity.getFileDocument(path: String): DocumentFile? {
    if (!isLollipopPlus())
        return null

    var relativePath = path.substring(sdCardPath.length)
    if (relativePath.startsWith(File.separator))
        relativePath = relativePath.substring(1)

    var document = DocumentFile.fromTreeUri(this, Uri.parse(baseConfig.treeUri))
    val parts = relativePath.split("/")
    for (i in 0..parts.size - 1) {
        var currDocument = document.findFile(parts[i])
        if (currDocument == null) {
            // We need to assure that we transverse to the right directory!
            if (i == parts.size - 1) {
                // The last document should be the file we're looking for, not a directory
                currDocument = document.createFile("", parts[i])
            } else {
                currDocument = document.createDirectory(parts[i])
            }

            if (currDocument == null) {
                toast(R.string.unknown_error_occurred)
                return null
            }
        }
        document = currDocument
    }
    return document
}
