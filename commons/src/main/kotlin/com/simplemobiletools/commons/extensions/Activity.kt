package com.simplemobiletools.commons.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Looper
import android.os.TransactionTooLargeException
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.provider.DocumentFile
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.DonateDialog
import com.simplemobiletools.commons.dialogs.SecurityDialog
import com.simplemobiletools.commons.dialogs.WhatsNewDialog
import com.simplemobiletools.commons.dialogs.WritePermissionDialog
import com.simplemobiletools.commons.helpers.IS_FROM_GALLERY
import com.simplemobiletools.commons.helpers.REAL_FILE_PATH
import com.simplemobiletools.commons.helpers.REQUEST_EDIT_IMAGE
import com.simplemobiletools.commons.helpers.REQUEST_SET_AS
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

fun Activity.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.an_error_occurred), msg), length)
}

fun Activity.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}

@SuppressLint("NewApi")
fun Activity.storeStoragePaths() {
    baseConfig.internalStoragePath = getInternalStoragePath()

    Thread({
        baseConfig.sdCardPath = getSDCardPath().trimEnd('/')
    }).start()

    baseConfig.appRunCount++
    if (!isThankYouInstalled() && (baseConfig.appRunCount % 100 == 0)) {
        DonateDialog(this)
    }
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

fun Activity.shareUri(uri: Uri, applicationId: String) {
    val newUri = ensurePublicUri(uri, applicationId)
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, newUri)
        type = getMimeTypeFromUri(newUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(this, getString(R.string.share_via)))
        } else {
            toast(R.string.no_app_found)
        }
    }
}

fun Activity.shareUris(uris: ArrayList<Uri>, applicationId: String) {
    if (uris.size == 1) {
        shareUri(uris.first(), applicationId)
    } else {
        val newUris = uris.map { ensurePublicUri(it, applicationId) } as ArrayList<Uri>
        Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            type = newUris.getMimeType()
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, newUris)

            if (resolveActivity(packageManager) != null) {
                try {
                    startActivity(Intent.createChooser(this, getString(R.string.share_via)))
                } catch (e: TransactionTooLargeException) {
                    toast(R.string.maximum_share_reached)
                }
            } else {
                toast(R.string.no_app_found)
            }
        }
    }
}

fun Activity.setAs(uri: Uri, applicationId: String) {
    val newUri = ensurePublicUri(uri, applicationId)
    Intent().apply {
        action = Intent.ACTION_ATTACH_DATA
        setDataAndType(newUri, getMimeTypeFromUri(newUri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(this, getString(R.string.set_as))

        if (resolveActivity(packageManager) != null) {
            startActivityForResult(chooser, REQUEST_SET_AS)
        } else {
            toast(R.string.no_app_found)
        }
    }
}

fun Activity.openEditor(uri: Uri, applicationId: String) {
    val newUri = ensurePublicUri(uri, applicationId)
    Intent().apply {
        action = Intent.ACTION_EDIT
        setDataAndType(newUri, getMimeTypeFromUri(newUri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(MediaStore.EXTRA_OUTPUT, uri)

        if (resolveActivity(packageManager) != null) {
            startActivityForResult(this, REQUEST_EDIT_IMAGE)
        } else {
            toast(R.string.no_app_found)
        }
    }
}

fun Activity.openFile(uri: Uri, forceChooser: Boolean, applicationId: String) {
    val newUri = ensurePublicUri(uri, applicationId)
    val mimeType = getMimeTypeFromUri(newUri)
    Intent().apply {
        action = Intent.ACTION_VIEW
        setDataAndType(newUri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (applicationId == "com.simplemobiletools.gallery") {
            putExtra(IS_FROM_GALLERY, true)
        }

        putExtra(REAL_FILE_PATH, uri)

        if (resolveActivity(packageManager) != null) {
            val chooser = Intent.createChooser(this, getString(R.string.open_with))
            startActivity(if (forceChooser) chooser else this)
        } else {
            if (!tryGenericMimeType(this, mimeType, newUri)) {
                toast(R.string.no_app_found)
            }
        }
    }
}

fun Activity.tryGenericMimeType(intent: Intent, mimeType: String, uri: Uri): Boolean {
    val genericMimeType = mimeType.getGenericMimeType()
    intent.setDataAndType(uri, genericMimeType)
    return if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
        true
    } else {
        false
    }
}

fun BaseSimpleActivity.checkWhatsNew(releases: List<Release>, currVersion: Int) {
    if (baseConfig.lastVersion == 0) {
        baseConfig.lastVersion = currVersion
        return
    }

    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty())
        WhatsNewDialog(this, newReleases)

    baseConfig.lastVersion = currVersion
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

@SuppressLint("NewApi")
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
                    if (document != null && (file.isDirectory == document.isDirectory)) {
                        fileDeleted = (document.isFile == true || allowDeleteFolder) && DocumentsContract.deleteDocument(contentResolver, document.uri)
                    }
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
            } catch (e: Exception) {
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

@SuppressLint("NewApi")
fun BaseSimpleActivity.renameFile(oldFile: File, newFile: File, callback: (success: Boolean) -> Unit) {
    if (needsStupidWritePermissions(newFile.absolutePath)) {
        handleSAFDialog(newFile) {
            val document = getFileDocument(oldFile.absolutePath)
            if (document == null || (oldFile.isDirectory != document.isDirectory)) {
                callback(false)
                return@handleSAFDialog
            }

            try {
                val uri = DocumentsContract.renameDocument(contentResolver, document.uri, newFile.name)
                if (document.uri != uri) {
                    updateInMediaStore(oldFile, newFile)
                    scanFiles(arrayListOf(oldFile, newFile)) {
                        if (!baseConfig.keepLastModified) {
                            updateLastModified(newFile, System.currentTimeMillis())
                        }
                        callback(true)
                    }
                } else {
                    callback(false)
                }
            } catch (e: SecurityException) {
                showErrorToast(e)
                callback(false)
            }
        }
    } else if (oldFile.renameTo(newFile)) {
        if (newFile.isDirectory) {
            deleteFromMediaStore(oldFile)
            scanFile(newFile) {
                callback(true)
            }
        } else {
            if (!baseConfig.keepLastModified) {
                newFile.setLastModified(System.currentTimeMillis())
            }
            updateInMediaStore(oldFile, newFile)
            scanFile(newFile) {
                callback(true)
            }
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

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun BaseSimpleActivity.getFileOutputStream(file: File, callback: (outputStream: OutputStream?) -> Unit) {
    if (needsStupidWritePermissions(file.absolutePath)) {
        handleSAFDialog(file) {
            var document = getFileDocument(file.absolutePath)
            if (document == null) {
                val error = String.format(getString(R.string.could_not_create_file), file.absolutePath)
                showErrorToast(error)
                callback(null)
                return@handleSAFDialog
            }

            if (!file.exists()) {
                document = document.createFile("", file.name)
            }
            callback(contentResolver.openOutputStream(document!!.uri))
        }
    } else {
        callback(FileOutputStream(file))
    }
}

fun BaseSimpleActivity.getFileOutputStreamSync(targetPath: String, mimeType: String, parentDocumentFile: DocumentFile? = null): OutputStream? {
    val targetFile = File(targetPath)

    return if (needsStupidWritePermissions(targetPath)) {
        val documentFile = parentDocumentFile ?: getFileDocument(targetFile.parent)
        if (documentFile == null) {
            val error = String.format(getString(R.string.could_not_create_file), targetFile.parent)
            showErrorToast(error)
            return null
        }

        val newDocument = documentFile.createFile(mimeType, targetPath.getFilenameFromPath())
        contentResolver.openOutputStream(newDocument!!.uri)
    } else {
        FileOutputStream(targetFile)
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
    for (part in parts) {
        val currDocument = document.findFile(part)
        if (currDocument != null)
            document = currDocument
    }

    return document
}

fun Activity.handleHiddenFolderPasswordProtection(callback: () -> Unit) {
    if (baseConfig.isPasswordProtectionOn) {
        SecurityDialog(this, baseConfig.passwordHash, baseConfig.protectionType) { hash, type, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun Activity.handleAppPasswordProtection(callback: (success: Boolean) -> Unit) {
    if (baseConfig.appPasswordProtectionOn) {
        SecurityDialog(this, baseConfig.appPasswordHash, baseConfig.appProtectionType) { hash, type, success ->
            callback(success)
        }
    } else {
        callback(true)
    }
}

fun BaseSimpleActivity.createDirectorySync(directory: File): Boolean {
    if (directory.exists())
        return true

    if (needsStupidWritePermissions(directory.absolutePath)) {
        val documentFile = getFileDocument(directory.absolutePath) ?: return false
        val newDir = documentFile.createDirectory(directory.name)
        return newDir != null
    }
    return directory.mkdirs()
}

fun BaseSimpleActivity.useEnglishToggled() {
    val conf = resources.configuration
    conf.locale = if (baseConfig.useEnglish) Locale.ENGLISH else Locale.getDefault()
    resources.updateConfiguration(conf, resources.displayMetrics)
    restartActivity()
}

fun BaseSimpleActivity.restartActivity() {
    finish()
    startActivity(intent)
}
