package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.FilepickerFavoritesAdapter
import com.simplemobiletools.commons.adapters.FilepickerItemsAdapter
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.DialogSurface
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.components.FolderBreadcrumbs
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppTheme
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.databinding.DialogFilepickerBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.Breadcrumbs
import kotlinx.collections.immutable.toImmutableList
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
        currPath = activity.updateCurrentPath(currPath)

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
                callback(activity.getRegularItems(path, lastModifieds, showHidden))
            }
        }
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

@Composable
fun FilePickerAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    startPath: String = Environment.getExternalStorageDirectory().toString(),
    pickFile: Boolean = true,
    showHidden: Boolean = false,
    showFAB: Boolean = false,
    canAddShowHiddenButton: Boolean = false,
    forceShowRoot: Boolean = false,
    showFavoritesButton: Boolean = false,
    enforceStorageRestrictions: Boolean = true,
    callback: (pickedPath: String) -> Unit
) {
    val context = LocalContext.current
    var currPath by remember { mutableStateOf(context.updateCurrentPath(startPath)) }
    var loading by remember { mutableStateOf(true) }
    var currentItems by remember { mutableStateOf(listOf<FileDirItem>()) }
    LaunchedEffect(key1 = currPath) {
        fun updateFolderItems(items: List<FileDirItem>) {
            currentItems = items
                .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                .toImmutableList()
            loading = false
        }

        loading = true
        when {
            context.isRestrictedSAFOnlyRoot(currPath) -> {
                context.getAndroidSAFFileItems(currPath, showHidden) {
                    updateFolderItems(it)
                }
            }

            context.isPathOnOTG(currPath) -> {
                context.getOTGItems(currPath, showHidden, false) {
                    updateFolderItems(it)
                }
            }

            else -> {
                val lastModifieds = context.getFolderLastModifieds(currPath)
                updateFolderItems(context.getRegularItems(currPath, lastModifieds, showHidden))
            }
        }
    }

    val storagePickerDialogState = getStoragePickerDialogState(
        currPath = currPath,
        forceShowRoot = forceShowRoot,
        callback = {
            currPath = it
        }
    )

    AlertDialog(
        onDismissRequest = alertDialogState::hide
    ) {
        AppTheme {
            DialogSurface {
                Column(
                    modifier = modifier
                        .fillMaxWidth(0.95f)
                        .padding(all = SimpleTheme.dimens.padding.extraLarge)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FolderBreadcrumbs(
                                items = currPath.toBreadcrumbs(context).toImmutableList(),
                                onBreadcrumbClicked = { index, item ->
                                    if (index == 0) {
                                        storagePickerDialogState.show()
                                    } else {
                                        if (currPath != item.path.trimEnd('/')) {
                                            currPath = item.path
                                        }
                                    }
                                }
                            )

                            if (loading) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    text = stringResource(id = R.string.loading)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(currentItems, key = FileDirItem::getSignature) {
                                        FilePickerItem(
                                            item = it,
                                            onClick = {
                                                if (pickFile && !it.isDirectory) {
                                                    callback(it.path)
                                                    alertDialogState.hide()
                                                } else {
                                                    currPath = it.path
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (showFAB) {
                            val createNewFolderDialog = rememberCreateNewFolderAlertDialogState(
                                path = currPath,
                                callback = {
                                    callback(it)
                                    alertDialogState.hide()
                                }
                            )
                            FloatingActionButton(
                                modifier = Modifier
                                    .padding(all = SimpleTheme.dimens.padding.extraLarge)
                                    .align(Alignment.BottomEnd),
                                containerColor = SimpleTheme.colorScheme.primary,
                                onClick = {
                                    createNewFolderDialog.show()
                                }
                            ) {
                                Icon(painter = painterResource(id = R.drawable.ic_plus_vector), contentDescription = null)
                            }
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            alertDialogState.hide()
                        }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }

                        if (!pickFile) {
                            TextButton(onClick = {
                                alertDialogState.hide()
                            }) {
                                Text(text = stringResource(id = R.string.ok))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilePickerItem(
    item: FileDirItem,
    onClick: () -> Unit
) {
//    if (fileDirItem.isDirectory) {
//        listItemIcon.setImageDrawable(folderDrawable)
//        listItemDetails.text = getChildrenCnt(fileDirItem)
//    } else {
//        listItemDetails.text = fileDirItem.size.formatSize()
//        val path = fileDirItem.path
//        val placeholder = fileDrawables.getOrElse(fileDirItem.name.substringAfterLast(".").lowercase(Locale.getDefault())) { fileDrawable }
//        val options = RequestOptions()
//            .signature(fileDirItem.getKey())
//            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//            .centerCrop()
//            .error(placeholder)
//
//        var itemToLoad = if (fileDirItem.name.endsWith(".apk", true)) {
//            val packageInfo = root.context.packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
//            if (packageInfo != null) {
//                val appInfo = packageInfo.applicationInfo
//                appInfo.sourceDir = path
//                appInfo.publicSourceDir = path
//                appInfo.loadIcon(root.context.packageManager)
//            } else {
//                path
//            }
//        } else {
//            path
//        }
//
//        if (!activity.isDestroyed && !activity.isFinishing) {
//            if (activity.isRestrictedSAFOnlyRoot(path)) {
//                itemToLoad = activity.getAndroidSAFUri(path)
//            } else if (hasOTGConnected && itemToLoad is String && activity.isPathOnOTG(itemToLoad)) {
//                itemToLoad = itemToLoad.getOTGPublicPath(activity)
//            }
//
//            if (itemToLoad.toString().isGif()) {
//                Glide.with(activity).asBitmap().load(itemToLoad).apply(options).into(listItemIcon)
//            } else {
//                Glide.with(activity)
//                    .load(itemToLoad)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .apply(options)
//                    .transform(CenterCrop(), RoundedCorners(cornerRadius))
//                    .into(listItemIcon)
//            }
//        }
//    }
//}
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SimpleTheme.dimens.padding.extraSmall)
            .padding(top = SimpleTheme.dimens.padding.extraLarge)
            .clickable(onClick = onClick)
    ) {
        Icon(
            modifier = Modifier
                .size(54.dp)
                .padding(all = SimpleTheme.dimens.padding.medium),
            painter = painterResource(id = R.drawable.ic_folder_vector),
            contentDescription = null
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp
            )
            Text(
                text = if (item.isDirectory)
                    pluralStringResource(id = R.plurals.items, count = item.children, item.children)
                else
                    item.size.formatSize(),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun rememberCreateNewFolderAlertDialogState(path: String, callback: (String) -> Unit) = rememberAlertDialogState().apply {
    DialogMember {
        CreateNewFolderAlertDialog(
            alertDialogState = this,
            path = path,
            callback = callback,
        )
    }
}

private fun Context.updateCurrentPath(currPath: String): String {
    var newCurrPath = currPath
    if (!getDoesFilePathExist(newCurrPath)) {
        newCurrPath = internalStoragePath
    }

    if (!getIsPathDirectory(newCurrPath)) {
        newCurrPath = newCurrPath.getParentPath()
    }

    // do not allow copying files in the recycle bin manually
    if (newCurrPath.startsWith(filesDir.absolutePath)) {
        newCurrPath = internalStoragePath
    }
    return newCurrPath
}

private fun String.toBreadcrumbs(context: Context): List<FileDirItem> {
    val basePath = getBasePath(context)
    var currPath = basePath
    val tempPath = context.humanizePath(this)

    val dirs = tempPath.split("/").dropLastWhile(String::isEmpty)
    val items = mutableListOf<FileDirItem>()
    for (i in dirs.indices) {
        val dir = dirs[i]
        if (i > 0) {
            currPath += dir + "/"
        }

        if (dir.isEmpty()) {
            continue
        }

        currPath = "${currPath.trimEnd('/')}/"
        items += FileDirItem(currPath, dir, true, 0, 0, 0)
    }
    return items
}

private fun Context.getRegularItems(path: String, lastModifieds: HashMap<String, Long>, showHidden: Boolean): List<FileDirItem> {
    val items = ArrayList<FileDirItem>()
    val files = File(path).listFiles()?.filterNotNull()
    if (files == null) {
        return items
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

        val children = if (isDirectory) file.getDirectChildrenCount(this, showHidden) else 0
        items.add(FileDirItem(curPath, curName, isDirectory, children, size, lastModified))
    }
    return items
}

@Composable
private fun getStoragePickerDialogState(
    currPath: String,
    forceShowRoot: Boolean,
    callback: (pickedPath: String) -> Unit
) = rememberAlertDialogState().apply {
    DialogMember {
        StoragePickerAlertDialog(
            alertDialogState = this,
            showRoot = forceShowRoot,
            pickSingleOption = true,
            currPath = currPath,
            callback = callback
        )
    }
}

@Composable
@MyDevices
private fun FilePickerAlertDialogPreview() {
    AppThemeSurface {
        FilePickerAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            callback = {}
        )
    }
}
