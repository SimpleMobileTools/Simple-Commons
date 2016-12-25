package com.simplemobiletools.commons.extensions

import android.app.Activity
import android.content.Intent
import com.simplemobiletools.filepicker.dialogs.WritePermissionDialog
import java.io.File

fun Activity.isShowingWritePermissions(file: File, treeUri: String, requestCode: Int): Boolean {
    return if ((needsStupidWritePermissions(file.absolutePath) && treeUri.isEmpty())) {
        WritePermissionDialog(this) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, requestCode)
        }
        true
    } else {
        false
    }
}
