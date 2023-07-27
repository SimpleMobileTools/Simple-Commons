package com.simplemobiletools.commons.dialogs

import android.os.Environment
import android.os.Parcelable
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.FilepickerFavoritesAdapter
import com.simplemobiletools.commons.adapters.FilepickerItemsAdapter
import com.simplemobiletools.commons.databinding.DialogFilepickerBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.Breadcrumbs
import java.io.File

/**
 * The only filepicker constructor with a couple optional parameters
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param currPath initial path of the dialog, defaults to the external storage
 * @param pickFile toggle used to determine if we are picking a file or a folder
 * @param showHidden toggle for showing hidden items, whose name starts with a dot
 * @param showFAB toggle the displaying of a Floating Action Button for creating new folders
 * @param callback the callback used for returning the selected file/folder
 */
class FilePickerDialog(
    val activity: BaseSimpleActivity,
    var currPath: String = Environment.getExternalStorageDirectory().toString(),
    val pickFile: Boolean = true,
    var showHidden: Boolean = false,
    val showFAB: Boolean = false,
    val canAddShowHiddenButton: Boolean = false,
    val forceShowRoot: Boolean = false,
    val showFavoritesButton: Boolean = false,
    private val enforceStorageRestrictions: Boolean = true,
    val callback: (pickedPath: String) -> Unit
) : Breadcrumbs.BreadcrumbsListener {

    private var mFirstUpdate = true
    private var mPrevPath = ""
    private var mScrollStates = HashMap<String, Parcelable>()

    private var mDialog: AlertDialog? = null
    private var mDialogView = DialogFilepickerBinding.inflate(activity.layoutInflater, null, false)

    init {
        if (!activity.getDoesFilePathExist(currPath)) {
            currPath = activity.internalStoragePath
        }

        if (!activity.getIsPathDirectory(currPath)) {
            currPath = currPath.getParentPath()
        }

        // do not allow copying files in the recycle bin manually
        if (currPath.startsWith(activity.filesDir.absolutePath)) {
            currPath = activity.internalStoragePath
        }

        mDialogView.filepickerBreadcrumbs.apply {
            listener = this@FilePickerDialog
            updateFontSize(activity.getTextSize(), false)
            isShownInDialog = true
        }

        tryUpdateItems()
        setupFavorites()

        val builder = activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel, null)
            .setOnKeyListener { dialogInterface, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    val breadcrumbs = mDialogView.filepickerBreadcrumbs
                    if (breadcrumbs.getItemCount() > 1) {
                        breadcrumbs.removeBreadcrumb()
                        currPath = breadcrumbs.getLastItem().path.trimEnd('/')
                        tryUpdateItems()
                    } else {
                        mDialog?.dismiss()
                    }
                }
                true
            }

        if (!pickFile) {
            builder.setPositiveButton(R.string.ok, null)
        }

        if (showFAB) {
            mDialogView.filepickerFab.apply {
                beVisible()
                setOnClickListener { createNewFolder() }
            }
        }

        val secondaryFabBottomMargin = activity.resources.getDimension(if (showFAB) R.dimen.secondary_fab_bottom_margin else R.dimen.activity_margin).toInt()
        mDialogView.filepickerFabsHolder.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = secondaryFabBottomMargin
        }

        mDialogView.filepickerPlaceholder.setTextColor(activity.getProperTextColor())
        mDialogView.filepickerFastscroller.updateColors(activity.getProperPrimaryColor())
        mDialogView.filepickerFabShowHidden.apply {
            beVisibleIf(!showHidden && canAddShowHiddenButton)
            setOnClickListener {
                activity.handleHiddenFolderPasswordProtection {
                    beGone()
                    showHidden = true
                    tryUpdateItems()
                }
            }
        }

        mDialogView.filepickerFavoritesLabel.text = "${activity.getString(R.string.favorites)}:"
        mDialogView.filepickerFabShowFavorites.apply {
            beVisibleIf(showFavoritesButton && context.baseConfig.favorites.isNotEmpty())
            setOnClickListener {
                if (mDialogView.filepickerFavoritesHolder.isVisible()) {
                    hideFavorites()
                } else {
                    showFavorites()
                }
            }
        }

        builder.apply {
            activity.setupDialogStuff(mDialogView.root, this, getTitle()) { alertDialog ->
                mDialog = alertDialog
            }
        }

        if (!pickFile) {
            mDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                verifyPath()
            }
        }
    }

    private fun getTitle() = if (pickFile) R.string.select_file else R.string.select_folder

    private fun createNewFolder() {
        CreateNewFolderDialog(activity, currPath) {
            callback(it)
            mDialog?.dismiss()
        }
    }

    private fun tryUpdateItems() {
        ensureBackgroundThread {
            getItems(currPath) {
                activity.runOnUiThread {
                    mDialogView.filepickerPlaceholder.beGone()
                    updateItems(it as ArrayList<FileDirItem>)
                }
            }
        }
    }

    private fun updateItems(items: ArrayList<FileDirItem>) {
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile && !showFAB) {
            verifyPath()
            return
        }

        val sortedItems = items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        val adapter = FilepickerItemsAdapter(activity, sortedItems, mDialogView.filepickerList) {
            if ((it as FileDirItem).isDirectory) {
                activity.handleLockedFolderOpening(it.path) { success ->
                    if (success) {
                        currPath = it.path
                        tryUpdateItems()
                    }
                }
            } else if (pickFile) {
                currPath = it.path
                verifyPath()
            }
        }

        val layoutManager = mDialogView.filepickerList.layoutManager as LinearLayoutManager
        mScrollStates[mPrevPath.trimEnd('/')] = layoutManager.onSaveInstanceState()!!

        mDialogView.apply {
            filepickerList.adapter = adapter
            filepickerBreadcrumbs.setBreadcrumb(currPath)

            if (root.context.areSystemAnimationsEnabled) {
                filepickerList.scheduleLayoutAnimation()
            }

            layoutManager.onRestoreInstanceState(mScrollStates[currPath.trimEnd('/')])
        }

        mFirstUpdate = false
        mPrevPath = currPath
    }

    private fun verifyPath() {
        when {
            activity.isRestrictedSAFOnlyRoot(currPath) -> {
                val document = activity.getSomeAndroidSAFDocument(currPath) ?: return
                sendSuccessForDocumentFile(document)
            }
            activity.isPathOnOTG(currPath) -> {
                val fileDocument = activity.getSomeDocumentFile(currPath) ?: return
                sendSuccessForDocumentFile(fileDocument)
            }
            activity.isAccessibleWithSAFSdk30(currPath) -> {
                if (enforceStorageRestrictions) {
                    activity.handleSAFDialogSdk30(currPath) {
                        if (it) {
                            val document = activity.getSomeDocumentSdk30(currPath)
                            sendSuccessForDocumentFile(document ?: return@handleSAFDialogSdk30)
                        }
                    }
                } else {
                    sendSuccessForDirectFile()
                }

            }
            activity.isRestrictedWithSAFSdk30(currPath) -> {
                if (enforceStorageRestrictions) {
                    if (activity.isInDownloadDir(currPath)) {
                        sendSuccessForDirectFile()
                    } else {
                        activity.toast(R.string.system_folder_restriction, Toast.LENGTH_LONG)
                    }
                } else {
                    sendSuccessForDirectFile()
                }
            }
            else -> {
                sendSuccessForDirectFile()
            }
        }
    }

    private fun sendSuccessForDocumentFile(document: DocumentFile) {
        if ((pickFile && document.isFile) || (!pickFile && document.isDirectory)) {
            sendSuccess()
        }
    }

    private fun sendSuccessForDirectFile() {
        val file = File(currPath)
        if ((pickFile && file.isFile) || (!pickFile && file.isDirectory)) {
            sendSuccess()
        }
    }

    private fun sendSuccess() {
        currPath = if (currPath.length == 1) {
            currPath
        } else {
            currPath.trimEnd('/')
        }

        callback(currPath)
        mDialog?.dismiss()
    }

    private fun getItems(path: String, callback: (List<FileDirItem>) -> Unit) {
        when {
            activity.isRestrictedSAFOnlyRoot(path) -> {
                activity.handleAndroidSAFDialog(path) {
                    activity.getAndroidSAFFileItems(path, showHidden) {
                        callback(it)
                    }
                }
            }
            activity.isPathOnOTG(path) -> activity.getOTGItems(path, showHidden, false, callback)
            else -> {
                val lastModifieds = activity.getFolderLastModifieds(path)
                getRegularItems(path, lastModifieds, callback)
            }
        }
    }

    private fun getRegularItems(path: String, lastModifieds: HashMap<String, Long>, callback: (List<FileDirItem>) -> Unit) {
        val items = ArrayList<FileDirItem>()
        val files = File(path).listFiles()?.filterNotNull()
        if (files == null) {
            callback(items)
            return
        }

        for (file in files) {
            if (!showHidden && file.name.startsWith('.')) {
                continue
            }

            val curPath = file.absolutePath
            val curName = curPath.getFilenameFromPath()
            val size = file.length()
            var lastModified = lastModifieds.remove(curPath)
            val isDirectory = if (lastModified != null) false else file.isDirectory
            if (lastModified == null) {
                lastModified = 0    // we don't actually need the real lastModified that badly, do not check file.lastModified()
            }

            val children = if (isDirectory) file.getDirectChildrenCount(activity, showHidden) else 0
            items.add(FileDirItem(curPath, curName, isDirectory, children, size, lastModified))
        }
        callback(items)
    }

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    private fun setupFavorites() {
        FilepickerFavoritesAdapter(activity, activity.baseConfig.favorites.toMutableList(), mDialogView.filepickerFavoritesList) {
            currPath = it as String
            verifyPath()
        }.apply {
            mDialogView.filepickerFavoritesList.adapter = this
        }
    }

    private fun showFavorites() {
        mDialogView.apply {
            filepickerFavoritesHolder.beVisible()
            filepickerFilesHolder.beGone()
            val drawable = activity.resources.getColoredDrawableWithColor(R.drawable.ic_folder_vector, activity.getProperPrimaryColor().getContrastColor())
            filepickerFabShowFavorites.setImageDrawable(drawable)
        }
    }

    private fun hideFavorites() {
        mDialogView.apply {
            filepickerFavoritesHolder.beGone()
            filepickerFilesHolder.beVisible()
            val drawable = activity.resources.getColoredDrawableWithColor(R.drawable.ic_star_vector, activity.getProperPrimaryColor().getContrastColor())
            filepickerFabShowFavorites.setImageDrawable(drawable)
        }
    }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            StoragePickerDialog(activity, currPath, forceShowRoot, true) {
                currPath = it
                tryUpdateItems()
            }
        } else {
            val item = mDialogView.filepickerBreadcrumbs.getItem(id)
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                tryUpdateItems()
            }
        }
    }
}
