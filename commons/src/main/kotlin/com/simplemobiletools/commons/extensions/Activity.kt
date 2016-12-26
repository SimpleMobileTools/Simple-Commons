package com.simplemobiletools.commons.extensions

import android.app.Activity
import android.content.Intent
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.WhatsNewDialog
import com.simplemobiletools.commons.dialogs.WritePermissionDialog
import com.simplemobiletools.commons.models.Release
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

fun BaseSimpleActivity.checkWhatsNew(releases: List<Release>) {
    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty())
        WhatsNewDialog(this, newReleases)
}
