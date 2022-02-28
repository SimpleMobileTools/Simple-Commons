package com.simplemobiletools.commons.extensions

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import com.simplemobiletools.commons.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.commons.models.FileDirItem

private const val DOWNLOAD_DIR = "Download"
val DIRS_INACCESSIBLE_WITH_SAF_SDK_30 = listOf(DOWNLOAD_DIR)

fun Context.isAccessibleWithSAFSdk30(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }

    val firstParentPath = path.getFirstParentPath(this)
    val firstParentDir = path.getFirstParentDirName(this)
    return isRPlus() && !Environment.isExternalStorageManager() && firstParentPath != path &&
        DIRS_INACCESSIBLE_WITH_SAF_SDK_30.all {
            firstParentDir != it
        }
}

fun Context.createDocumentUriFromFirstParentTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this)
    val treeUri = DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, "$storageId:")
    val documentId = "${storageId}:$rootParentDirName"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.createFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this)
    val firstParentId = "$storageId:$rootParentDirName"
    return DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, firstParentId)
}

fun Context.createDocumentUriUsingFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val relativePath = when {
        fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length).trim('/')
        else -> fullPath.substringAfter(storageId).trim('/')
    }
    val treeUri = createFirstParentTreeUri(fullPath)
    val documentId = "${storageId}:$relativePath"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.createSdk30SAFDirectory(path: String): Boolean {
    return try {
        val treeUri = createFirstParentTreeUri(path)
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExist(parentPath)) {
            createAndroidSAFDirectory(parentPath)
        }

        val documentId = createAndroidSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(contentResolver, parentUri, DocumentsContract.Document.MIME_TYPE_DIR, path.getFilenameFromPath()) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.deleteDocumentWithSAFSdk30(fileDirItem: FileDirItem, allowDeleteFolder: Boolean, callback: ((wasSuccess: Boolean) -> Unit)?) {
    try {
        var fileDeleted = false
        if (fileDirItem.isDirectory.not() || allowDeleteFolder) {
            val firstParentTreeUri = createFirstParentTreeUri(fileDirItem.path)
            val fileUri = createDocumentUriUsingFirstParentTreeUri(fileDirItem.path)
            fileDeleted = DocumentsContract.deleteDocument(contentResolver, fileUri)
        }

        if (fileDeleted) {
            deleteFromMediaStore(fileDirItem.path)
            callback?.invoke(true)
        }

    } catch (e: Exception) {
        callback?.invoke(false)
        showErrorToast(e)
    }
}
