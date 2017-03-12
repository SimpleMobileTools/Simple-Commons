package com.simplemobiletools.commons.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.WhatsNewDialog
import com.simplemobiletools.commons.dialogs.WritePermissionDialog
import com.simplemobiletools.commons.models.Release
import java.io.File

fun Activity.isShowingSAFDialog(file: File, treeUri: String, requestCode: Int): Boolean {
    return if ((needsStupidWritePermissions(file.absolutePath) && treeUri.isEmpty())) {
        runOnUiThread {
            WritePermissionDialog(this) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, requestCode)
            }
        }
        true
    } else {
        false
    }
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

fun BaseSimpleActivity.deleteFile(file: File, callback: (wasSuccess: Boolean) -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFileBg(file, callback)
        }.start()
    } else {
        deleteFileBg(file, callback)
    }
}

fun BaseSimpleActivity.deleteFileBg(file: File, callback: (wasSuccess: Boolean) -> Unit) {
    var fileDeleted = !file.exists() || file.delete()
    if (fileDeleted) {
        rescanDeletedFile(file) {
            callback(true)
        }
    } else {
        handleSAFDialog(file) {
            fileDeleted = tryFastDocumentDelete(file)
            if (!fileDeleted) {
                val document = getFileDocument(file.absolutePath, baseConfig.treeUri)
                fileDeleted = document?.isFile == true && document.delete()
            }

            if (fileDeleted) {
                rescanDeletedFile(file) {
                    callback(true)
                }
            }
        }
    }
}

fun BaseSimpleActivity.rescanDeletedFile(file: File, callback: () -> Unit) {
    if (deleteFromMediaStore(file)) {
        callback()
    } else {
        scanFile(file) {
            callback()
        }
    }
}

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}

fun Activity.showKeyboard(et: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}
