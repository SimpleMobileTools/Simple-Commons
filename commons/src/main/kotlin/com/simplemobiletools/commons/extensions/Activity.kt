package com.simplemobiletools.commons.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Looper
import android.os.TransactionTooLargeException
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.*
import com.simplemobiletools.commons.views.MyTextView
import kotlinx.android.synthetic.main.dialog_title.view.*
import java.io.*
import java.util.*

fun AppCompatActivity.updateActionBarTitle(text: String, color: Int = baseConfig.primaryColor) {
    supportActionBar?.title = Html.fromHtml("<font color='${color.getContrastColor().toHex()}'>$text</font>")
}

fun AppCompatActivity.updateActionBarSubtitle(text: String) {
    supportActionBar?.subtitle = Html.fromHtml("<font color='${baseConfig.primaryColor.getContrastColor().toHex()}'>$text</font>")
}

fun Activity.appLaunched(appId: String) {
    baseConfig.internalStoragePath = getInternalStoragePath()
    updateSDCardPath()
    baseConfig.appId = appId
    if (baseConfig.appRunCount == 0) {
        baseConfig.wasOrangeIconChecked = true
        checkAppIconColor()
    } else if (!baseConfig.wasOrangeIconChecked) {
        baseConfig.wasOrangeIconChecked = true
        val primaryColor = resources.getColor(R.color.color_primary)
        if (baseConfig.appIconColor != primaryColor) {
            getAppIconColors().forEachIndexed { index, color ->
                toggleAppIconColor(appId, index, color, false)
            }

            val defaultClassName = "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity"
            packageManager.setComponentEnabledSetting(ComponentName(baseConfig.appId, defaultClassName), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP)

            val orangeClassName = "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity.Orange"
            packageManager.setComponentEnabledSetting(ComponentName(baseConfig.appId, orangeClassName), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

            baseConfig.appIconColor = primaryColor
            baseConfig.lastIconColor = primaryColor
        }
    }
    baseConfig.appRunCount++
    if (baseConfig.appRunCount % 50 == 0 && !isAProApp()) {
        showDonateOrUpgradeDialog()
    }
}

fun Activity.showDonateOrUpgradeDialog() {
    if (getCanAppBeUpgraded()) {
        UpgradeToProDialog(this)
    } else if (!baseConfig.hadThankYouInstalled && !isThankYouInstalled()) {
        DonateDialog(this)
    }
}

fun Activity.isAppInstalledOnSDCard(): Boolean = try {
    val applicationInfo = packageManager.getPackageInfo(packageName, 0).applicationInfo
    (applicationInfo.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE
} catch (e: Exception) {
    false
}

@SuppressLint("InlinedApi")
fun Activity.isShowingSAFDialog(path: String, treeUri: String, requestCode: Int): Boolean {
    return if (needsStupidWritePermissions(path) && (treeUri.isEmpty() || !hasProperStoredTreeUri())) {
        runOnUiThread {
            WritePermissionDialog(this, false) {
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    putExtra("android.content.extra.SHOW_ADVANCED", true)
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

fun Activity.launchPurchaseThankYouIntent() = launchViewIntent(getString(R.string.thank_you_url))

fun Activity.launchUpgradeToProIntent() = launchViewIntent(getProUrl())

fun Activity.launchViewIntent(id: Int) = launchViewIntent(getString(id))

fun Activity.launchViewIntent(url: String) {
    Thread {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            if (resolveActivity(packageManager) != null) {
                startActivity(this)
            } else {
                toast(R.string.no_app_found)
            }
        }
    }.start()
}

fun Activity.sharePathIntent(path: String, applicationId: String) {
    Thread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@Thread
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, newUri)
            type = getUriMimeType(path, newUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                if (resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(this, getString(R.string.share_via)))
                } else {
                    toast(R.string.no_app_found)
                }
            } catch (e: RuntimeException) {
                if (e.cause is TransactionTooLargeException) {
                    toast(R.string.maximum_share_reached)
                } else {
                    showErrorToast(e)
                }
            }
        }
    }.start()
}

fun Activity.sharePathsIntent(paths: ArrayList<String>, applicationId: String) {
    Thread {
        if (paths.size == 1) {
            sharePathIntent(paths.first(), applicationId)
        } else {
            val uriPaths = ArrayList<String>()
            val newUris = paths.map {
                val uri = getFinalUriFromPath(it, applicationId) ?: return@Thread
                uriPaths.add(uri.path)
                uri
            } as ArrayList<Uri>

            var mimeType = uriPaths.getMimeType()
            if (mimeType.isEmpty() || mimeType == "*/*") {
                mimeType = paths.getMimeType()
            }

            Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, newUris)

                try {
                    if (resolveActivity(packageManager) != null) {
                        startActivity(Intent.createChooser(this, getString(R.string.share_via)))
                    } else {
                        toast(R.string.no_app_found)
                    }
                } catch (e: RuntimeException) {
                    if (e.cause is TransactionTooLargeException) {
                        toast(R.string.maximum_share_reached)
                    } else {
                        showErrorToast(e)
                    }
                }
            }
        }
    }.start()
}

fun Activity.setAsIntent(path: String, applicationId: String) {
    Thread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@Thread
        Intent().apply {
            action = Intent.ACTION_ATTACH_DATA
            setDataAndType(newUri, getUriMimeType(path, newUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(this, getString(R.string.set_as))

            if (resolveActivity(packageManager) != null) {
                startActivityForResult(chooser, REQUEST_SET_AS)
            } else {
                toast(R.string.no_app_found)
            }
        }
    }.start()
}

fun Activity.openEditorIntent(path: String, applicationId: String) {
    Thread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@Thread
        Intent().apply {
            action = Intent.ACTION_EDIT
            setDataAndType(newUri, getUriMimeType(path, newUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            val parent = path.getParentPath()
            val newFilename = "${path.getFilenameFromPath().substringBeforeLast('.')}_1"
            val extension = path.getFilenameExtension()
            val newFilePath = File(parent, "$newFilename.$extension")

            val outputUri = if (path.startsWith(OTG_PATH)) newUri else getFinalUriFromPath("$newFilePath", applicationId)
            val resInfoList = packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            putExtra(REAL_FILE_PATH, path)

            if (resolveActivity(packageManager) != null) {
                try {
                    startActivityForResult(this, REQUEST_EDIT_IMAGE)
                } catch (e: SecurityException) {
                    showErrorToast(e)
                }
            } else {
                toast(R.string.no_app_found)
            }
        }
    }.start()
}

fun Activity.openPathIntent(path: String, forceChooser: Boolean, applicationId: String, forceMimeType: String = "") {
    Thread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@Thread
        val mimeType = if (forceMimeType.isNotEmpty()) forceMimeType else getUriMimeType(path, newUri)
        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(newUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (applicationId == "com.simplemobiletools.gallery" || applicationId == "com.simplemobiletools.gallery.debug") {
                putExtra(IS_FROM_GALLERY, true)
            }

            putExtra(REAL_FILE_PATH, path)

            if (resolveActivity(packageManager) != null) {
                val chooser = Intent.createChooser(this, getString(R.string.open_with))
                try {
                    startActivity(if (forceChooser) chooser else this)
                } catch (e: NullPointerException) {
                    showErrorToast(e)
                }
            } else {
                if (!tryGenericMimeType(this, mimeType, newUri)) {
                    toast(R.string.no_app_found)
                }
            }
        }
    }.start()
}

fun Activity.getFinalUriFromPath(path: String, applicationId: String): Uri? {
    val uri = try {
        ensurePublicUri(path, applicationId)
    } catch (e: Exception) {
        showErrorToast(e)
        return null
    }

    if (uri == null) {
        toast(R.string.unknown_error_occurred)
        return null
    }

    return uri
}

fun Activity.tryGenericMimeType(intent: Intent, mimeType: String, uri: Uri): Boolean {
    var genericMimeType = mimeType.getGenericMimeType()
    if (genericMimeType.isEmpty()) {
        genericMimeType = "*/*"
    }

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

    if (newReleases.isNotEmpty()) {
        WhatsNewDialog(this, newReleases)
    }

    baseConfig.lastVersion = currVersion
}

fun BaseSimpleActivity.deleteFolders(folders: ArrayList<FileDirItem>, deleteMediaOnly: Boolean = true, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFoldersBg(folders, deleteMediaOnly, callback)
        }.start()
    } else {
        deleteFoldersBg(folders, deleteMediaOnly, callback)
    }
}

fun BaseSimpleActivity.deleteFoldersBg(folders: ArrayList<FileDirItem>, deleteMediaOnly: Boolean = true, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    var wasSuccess = false
    var needPermissionForPath = ""
    for (folder in folders) {
        if (needsStupidWritePermissions(folder.path) && baseConfig.treeUri.isEmpty()) {
            needPermissionForPath = folder.path
            break
        }
    }

    handleSAFDialog(needPermissionForPath) {
        folders.forEachIndexed { index, folder ->
            deleteFolderBg(folder, deleteMediaOnly) {
                if (it)
                    wasSuccess = true

                if (index == folders.size - 1) {
                    runOnUiThread {
                        callback?.invoke(wasSuccess)
                    }
                }
            }
        }
    }
}

fun BaseSimpleActivity.deleteFolder(folder: FileDirItem, deleteMediaOnly: Boolean = true, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFolderBg(folder, deleteMediaOnly, callback)
        }.start()
    } else {
        deleteFolderBg(folder, deleteMediaOnly, callback)
    }
}

fun BaseSimpleActivity.deleteFolderBg(fileDirItem: FileDirItem, deleteMediaOnly: Boolean = true, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    val folder = File(fileDirItem.path)
    if (folder.exists()) {
        val filesArr = folder.listFiles()
        if (filesArr == null) {
            runOnUiThread {
                callback?.invoke(true)
            }
            return
        }

        val files = filesArr.toMutableList().filter { !deleteMediaOnly || it.isMediaFile() }
        for (file in files) {
            deleteFileBg(file.toFileDirItem(applicationContext), false) { }
        }

        if (folder.listFiles()?.isEmpty() == true) {
            deleteFileBg(fileDirItem, true) { }
        }
    }
    runOnUiThread {
        callback?.invoke(true)
    }
}

fun BaseSimpleActivity.deleteFiles(files: ArrayList<FileDirItem>, allowDeleteFolder: Boolean = false, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFilesBg(files, allowDeleteFolder, callback)
        }.start()
    } else {
        deleteFilesBg(files, allowDeleteFolder, callback)
    }
}

fun BaseSimpleActivity.deleteFilesBg(files: ArrayList<FileDirItem>, allowDeleteFolder: Boolean = false, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    if (files.isEmpty()) {
        runOnUiThread {
            callback?.invoke(true)
        }
        return
    }

    var wasSuccess = false
    handleSAFDialog(files[0].path) {
        files.forEachIndexed { index, file ->
            deleteFileBg(file, allowDeleteFolder) {
                if (it) {
                    wasSuccess = true
                }

                if (index == files.size - 1) {
                    runOnUiThread {
                        callback?.invoke(wasSuccess)
                    }
                }
            }
        }
    }
}

fun BaseSimpleActivity.deleteFile(fileDirItem: FileDirItem, allowDeleteFolder: Boolean = false, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread {
            deleteFileBg(fileDirItem, allowDeleteFolder, callback)
        }.start()
    } else {
        deleteFileBg(fileDirItem, allowDeleteFolder, callback)
    }
}

fun BaseSimpleActivity.deleteFileBg(fileDirItem: FileDirItem, allowDeleteFolder: Boolean = false, callback: ((wasSuccess: Boolean) -> Unit)? = null) {
    val path = fileDirItem.path
    val file = File(path)
    if (file.absolutePath.startsWith(internalStoragePath) && !file.canWrite()) {
        callback?.invoke(false)
        return
    }

    var fileDeleted = !path.startsWith(OTG_PATH) && ((!file.exists() && file.length() == 0L) || file.delete())
    if (fileDeleted) {
        rescanDeletedPath(path) {
            runOnUiThread {
                callback?.invoke(true)
            }
        }
    } else {
        if (file.isDirectory && allowDeleteFolder) {
            fileDeleted = deleteRecursively(file)
        }

        if (!fileDeleted) {
            if (isPathOnSD(path)) {
                handleSAFDialog(path) {
                    trySAFFileDelete(fileDirItem, allowDeleteFolder, callback)
                }
            } else if (path.startsWith(OTG_PATH)) {
                trySAFFileDelete(fileDirItem, allowDeleteFolder, callback)
            }
        }
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

fun Activity.scanFileRecursively(file: File, callback: (() -> Unit)? = null) {
    applicationContext.scanFileRecursively(file, callback)
}

fun Activity.scanPathRecursively(path: String, callback: (() -> Unit)? = null) {
    applicationContext.scanPathRecursively(path, callback)
}

fun Activity.scanFilesRecursively(files: ArrayList<File>, callback: (() -> Unit)? = null) {
    applicationContext.scanFilesRecursively(files, callback)
}

fun Activity.scanPathsRecursively(paths: ArrayList<String>, callback: (() -> Unit)? = null) {
    applicationContext.scanPathsRecursively(paths, callback)
}

fun Activity.rescanPaths(paths: ArrayList<String>, callback: (() -> Unit)? = null) {
    applicationContext.rescanPaths(paths, callback)
}

fun BaseSimpleActivity.renameFile(oldPath: String, newPath: String, callback: ((success: Boolean) -> Unit)? = null) {
    if (needsStupidWritePermissions(newPath)) {
        handleSAFDialog(newPath) {
            val document = getDocumentFile(oldPath)
            if (document == null || (File(oldPath).isDirectory != document.isDirectory)) {
                runOnUiThread {
                    callback?.invoke(false)
                }
                return@handleSAFDialog
            }

            try {
                val uri = DocumentsContract.renameDocument(applicationContext.contentResolver, document.uri, newPath.getFilenameFromPath())
                if (document.uri != uri) {
                    updateInMediaStore(oldPath, newPath)
                    rescanPaths(arrayListOf(oldPath, newPath)) {
                        if (!baseConfig.keepLastModified) {
                            updateLastModified(newPath, System.currentTimeMillis())
                        }
                        runOnUiThread {
                            callback?.invoke(true)
                        }
                    }
                } else {
                    runOnUiThread {
                        callback?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                showErrorToast(e)
                runOnUiThread {
                    callback?.invoke(false)
                }
            }
        }
    } else if (File(oldPath).renameTo(File(newPath))) {
        if (File(newPath).isDirectory) {
            deleteFromMediaStore(oldPath)
            rescanPaths(arrayListOf(newPath)) {
                runOnUiThread {
                    callback?.invoke(true)
                }
                scanPathRecursively(newPath)
            }
        } else {
            if (!baseConfig.keepLastModified) {
                File(newPath).setLastModified(System.currentTimeMillis())
            }
            scanPathsRecursively(arrayListOf(newPath)) {
                runOnUiThread {
                    callback?.invoke(true)
                }
            }
        }
    } else {
        runOnUiThread {
            callback?.invoke(false)
        }
    }
}

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun BaseSimpleActivity.getFileOutputStream(fileDirItem: FileDirItem, allowCreatingNewFile: Boolean = false, callback: (outputStream: OutputStream?) -> Unit) {
    if (needsStupidWritePermissions(fileDirItem.path)) {
        handleSAFDialog(fileDirItem.path) {
            var document = getDocumentFile(fileDirItem.path)
            if (document == null && allowCreatingNewFile) {
                document = getDocumentFile(fileDirItem.getParentPath())
            }

            if (document == null) {
                showFileCreateError(fileDirItem.path)
                callback(null)
                return@handleSAFDialog
            }

            if (!File(fileDirItem.path).exists()) {
                document = document.createFile("", fileDirItem.name)
            }

            if (document?.exists() == true) {
                try {
                    callback(applicationContext.contentResolver.openOutputStream(document.uri))
                } catch (e: FileNotFoundException) {
                    showErrorToast(e)
                    callback(null)
                }
            } else {
                showFileCreateError(fileDirItem.path)
                callback(null)
            }
        }
    } else {
        val file = File(fileDirItem.path)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        try {
            callback(FileOutputStream(file))
        } catch (e: Exception) {
            callback(null)
        }
    }
}

fun BaseSimpleActivity.showFileCreateError(path: String) {
    val error = String.format(getString(R.string.could_not_create_file), path)
    baseConfig.treeUri = ""
    showErrorToast(error)
}

fun BaseSimpleActivity.getFileOutputStreamSync(path: String, mimeType: String, parentDocumentFile: DocumentFile? = null): OutputStream? {
    val targetFile = File(path)

    return if (needsStupidWritePermissions(path)) {
        var documentFile = parentDocumentFile
        if (documentFile == null) {
            if (targetFile.parentFile.exists()) {
                documentFile = getDocumentFile(targetFile.parent)
            } else {
                documentFile = getDocumentFile(targetFile.parentFile.parent)
                documentFile = documentFile!!.createDirectory(targetFile.parentFile.name)
            }
        }

        if (documentFile == null) {
            showFileCreateError(targetFile.parent)
            return null
        }

        val newDocument = documentFile.createFile(mimeType, path.getFilenameFromPath())
        applicationContext.contentResolver.openOutputStream(newDocument!!.uri)
    } else {
        if (!targetFile.parentFile.exists()) {
            targetFile.parentFile.mkdirs()
        }

        try {
            FileOutputStream(targetFile)
        } catch (e: Exception) {
            showErrorToast(e)
            null
        }
    }
}

fun BaseSimpleActivity.getFileInputStreamSync(path: String): InputStream? {
    return if (path.startsWith(OTG_PATH)) {
        val fileDocument = getSomeDocumentFile(path)
        applicationContext.contentResolver.openInputStream(fileDocument?.uri)
    } else {
        FileInputStream(File(path))
    }
}

fun Activity.handleHiddenFolderPasswordProtection(callback: () -> Unit) {
    if (baseConfig.isHiddenPasswordProtectionOn) {
        SecurityDialog(this, baseConfig.hiddenPasswordHash, baseConfig.hiddenProtectionType) { hash, type, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun Activity.handleAppPasswordProtection(callback: (success: Boolean) -> Unit) {
    if (baseConfig.isAppPasswordProtectionOn) {
        SecurityDialog(this, baseConfig.appPasswordHash, baseConfig.appProtectionType) { hash, type, success ->
            callback(success)
        }
    } else {
        callback(true)
    }
}

fun Activity.handleDeletePasswordProtection(callback: () -> Unit) {
    if (baseConfig.isDeletePasswordProtectionOn) {
        SecurityDialog(this, baseConfig.deletePasswordHash, baseConfig.deleteProtectionType) { hash, type, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun BaseSimpleActivity.createDirectorySync(directory: String): Boolean {
    if (getDoesFilePathExist(directory)) {
        return true
    }

    if (needsStupidWritePermissions(directory)) {
        val documentFile = getDocumentFile(directory.getParentPath()) ?: return false
        val newDir = documentFile.createDirectory(directory.getFilenameFromPath())
        return newDir != null
    }

    return File(directory).mkdirs()
}

fun Activity.updateSharedTheme(sharedTheme: SharedTheme) {
    try {
        val contentValues = MyContentProvider.fillThemeContentValues(sharedTheme)
        applicationContext.contentResolver.update(MyContentProvider.MY_CONTENT_URI, contentValues, null, null)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Activity.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.simple_commons), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = clip
    toast(R.string.value_copied_to_clipboard)
}

fun Activity.setupDialogStuff(view: View, dialog: AlertDialog, titleId: Int = 0, titleText: String = "", callback: (() -> Unit)? = null) {
    if (isDestroyed) {
        return
    }

    if (view is ViewGroup)
        updateTextColors(view)
    else if (view is MyTextView) {
        view.setColors(baseConfig.textColor, getAdjustedPrimaryColor(), baseConfig.backgroundColor)
    }

    var title: TextView? = null
    if (titleId != 0 || titleText.isNotEmpty()) {
        title = layoutInflater.inflate(R.layout.dialog_title, null) as TextView
        title.dialog_title_textview.apply {
            if (titleText.isNotEmpty()) {
                text = titleText
            } else {
                setText(titleId)
            }
            setTextColor(baseConfig.textColor)
        }
    }

    dialog.apply {
        setView(view)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCustomTitle(title)
        setCanceledOnTouchOutside(true)
        show()
        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(baseConfig.textColor)
        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(baseConfig.textColor)
        getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(baseConfig.textColor)
        window?.setBackgroundDrawable(ColorDrawable(baseConfig.backgroundColor))
    }
    callback?.invoke()
}

fun Activity.showPickSecondsDialogHelper(curMinutes: Int, isSnoozePicker: Boolean = false, showSecondsAtCustomDialog: Boolean = false,
                                         cancelCallback: (() -> Unit)? = null, callback: (seconds: Int) -> Unit) {
    val seconds = if (curMinutes > 0) curMinutes * 60 else curMinutes
    showPickSecondsDialog(seconds, isSnoozePicker, showSecondsAtCustomDialog, cancelCallback, callback)
}

fun Activity.showPickSecondsDialog(curSeconds: Int, isSnoozePicker: Boolean = false, showSecondsAtCustomDialog: Boolean = false,
                                   cancelCallback: (() -> Unit)? = null, callback: (seconds: Int) -> Unit) {
    hideKeyboard()
    val seconds = TreeSet<Int>()
    seconds.apply {
        if (!isSnoozePicker) {
            add(-1)
            add(0)
        }
        add(1 * MINUTE_SECONDS)
        add(5 * MINUTE_SECONDS)
        add(10 * MINUTE_SECONDS)
        add(30 * MINUTE_SECONDS)
        add(60 * MINUTE_SECONDS)
        add(curSeconds)
    }

    val items = ArrayList<RadioItem>(seconds.size + 1)
    seconds.mapIndexedTo(items) { index, value ->
        RadioItem(index, getFormattedSeconds(value, !isSnoozePicker), value)
    }

    var selectedIndex = 0
    seconds.forEachIndexed { index, value ->
        if (value == curSeconds) {
            selectedIndex = index
        }
    }

    items.add(RadioItem(-2, getString(R.string.custom)))

    RadioGroupDialog(this, items, selectedIndex, showOKButton = isSnoozePicker, cancelCallback = cancelCallback) {
        if (it == -2) {
            CustomIntervalPickerDialog(this, showSeconds = showSecondsAtCustomDialog) {
                callback(it)
            }
        } else {
            callback(it as Int)
        }
    }
}

fun BaseSimpleActivity.getAlarmSounds(type: Int, callback: (ArrayList<AlarmSound>) -> Unit) {
    val alarms = ArrayList<AlarmSound>()
    val manager = RingtoneManager(this)
    manager.setType(if (type == ALARM_SOUND_TYPE_NOTIFICATION) RingtoneManager.TYPE_NOTIFICATION else RingtoneManager.TYPE_ALARM)

    try {
        val cursor = manager.cursor
        var curId = 1
        val silentAlarm = AlarmSound(curId++, getString(R.string.no_sound), SILENT)
        alarms.add(silentAlarm)

        val defaultAlarm = getDefaultAlarmSound(type)
        alarms.add(defaultAlarm)

        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            var uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
            val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
            if (!uri.endsWith(id)) {
                uri += "/$id"
            }

            val alarmSound = AlarmSound(curId++, title, uri)
            alarms.add(alarmSound)
        }
        callback(alarms)
    } catch (e: Exception) {
        if (e is SecurityException) {
            handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    getAlarmSounds(type, callback)
                } else {
                    showErrorToast(e)
                    callback(ArrayList())
                }
            }
        } else {
            showErrorToast(e)
            callback(ArrayList())
        }
    }
}
