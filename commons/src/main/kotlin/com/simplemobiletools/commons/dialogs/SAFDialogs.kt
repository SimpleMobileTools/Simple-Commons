package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.EXTRA_SHOW_ADVANCED
import com.simplemobiletools.commons.helpers.isRPlus

@Composable
fun getWritePermissionDialog(
    path: String = "",
    onResult: (String) -> Unit,
    onCancel: () -> Unit
): AlertDialogState {
    val context = LocalContext.current
    val openDocumentTreeLauncher = getDocumentTreeLauncher(
        onResult = {
            val partition = try {
                path.substring(9, 18)
            } catch (e: Exception) {
                ""
            }
            if (context.handleOtgTreeResult(partition, it)) {
                onResult(context.otgPath)
            } else {
                onCancel()
            }
        }
    )

    return getWritePermissionDialog(
        mode = WritePermissionDialog.WritePermissionDialogMode.Otg,
        callback = {
            openDocumentTreeLauncher.launch(null)
        },
        cancelCallback = onCancel
    )
}

@Composable
fun getSafSdk30DialogState(
    path: String,
    onResult: (Boolean) -> Unit,
    onCancel: () -> Unit
): AlertDialogState {
    val context = LocalContext.current

    val finalPath by remember {
        derivedStateOf {
            val level = context.getFirstParentLevel(path)
            path.getFirstParentPath(context, level)
        }
    }

    val openDocumentTreeLauncher = getAdvancedDocumentTreeLauncher(
        onResult = {
            onResult(context.handleOpenDocumentTreeSdk30Result(finalPath, it))
        }
    )

    val mode by remember {
        derivedStateOf {
            WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30(finalPath)
        }
    }

    return getWritePermissionDialog(
        mode = mode,
        callback = {
            openDocumentTreeLauncher.launch(context.createFirstParentTreeUriUsingRootTree(finalPath))
        },
        cancelCallback = onCancel
    )
}

@Composable
fun getSafCreateDocumentSdk30DialogState(
    path: String,
    onResult: (Boolean) -> Unit,
    onCancel: () -> Unit
): AlertDialogState {
    val context = LocalContext.current

    val openDocumentTreeLauncher = getCreateDocumentSdk30Launcher(
        onResult = {
            onResult(context.handleCreateDocumentTreeSdk30(path, it))
        }
    )

    return getWritePermissionDialog(
        mode = WritePermissionDialog.WritePermissionDialogMode.CreateDocumentSDK30,
        callback = {
            openDocumentTreeLauncher.launch(context.buildDocumentUriSdk30(path))
        },
        cancelCallback = onCancel
    )
}

@Composable
fun getSdDialogState(
    path: String,
    onResult: (Boolean) -> Unit,
    onCancel: () -> Unit
): AlertDialogState {
    val context = LocalContext.current

    val openDocumentTreeLauncher = getAdvancedDocumentTreeLauncher(
        onResult = {
            onResult(context.handleOpenDocumentTreeSd(path, it))
        }
    )

    return getWritePermissionDialog(
        mode = WritePermissionDialog.WritePermissionDialogMode.SdCard,
        callback = {
            openDocumentTreeLauncher.launch(null)
        },
        cancelCallback = onCancel
    )
}

@Composable
fun rememberSafSdk30DialogState(): SafDialogState {
    var safDialogSdk30Path by remember {
        mutableStateOf("")
    }
    var safDialogSdk30PathCallback by remember {
        mutableStateOf<(Boolean) -> Unit>({})
    }
    val safDialogSdk30 = getSafSdk30DialogState(
        path = safDialogSdk30Path,
        onResult = {
            safDialogSdk30PathCallback.invoke(it)
        },
        onCancel = {}
    )
    val context = LocalContext.current

    return remember {
        object : SafDialogState() {
            override fun handle(path: String, callback: (Boolean) -> Unit) {
                if (!context.packageName.startsWith("com.simplemobiletools")) {
                    callback(true)
                } else if (context.isAccessibleWithSAFSdk30(path) && !context.hasProperStoredFirstParentUri(path)) {
                    safDialogSdk30Path = path
                    safDialogSdk30PathCallback = callback
                    safDialogSdk30.show()
                } else {
                    callback(true)
                }
            }
        }
    }
}

@Composable
fun rememberSafDialogState(): SafDialogState {
    var safDialogPath by remember {
        mutableStateOf("")
    }
    var safDialogPathCallback by remember {
        mutableStateOf<(Boolean) -> Unit>({})
    }
    val safDialog = getSdDialogState(
        path = safDialogPath,
        onResult = {
            safDialogPathCallback.invoke(it)
        },
        onCancel = {}
    )
    val otgDialog = getWritePermissionDialog(
        path = safDialogPath,
        onResult = {
            safDialogPathCallback.invoke(true)
        },
        onCancel = {
            safDialogPathCallback.invoke(false)
        }
    )
    val context = LocalContext.current

    return remember {
        object : SafDialogState() {
            override fun handle(path: String, callback: (Boolean) -> Unit) {
                if (!context.packageName.startsWith("com.simplemobiletools")) {
                    callback(true)
                } else if ((!isRPlus() && context.isPathOnSD(path) && !context.isSDCardSetAsDefaultStorage() && (context.baseConfig.sdTreeUri.isEmpty() || !context.hasProperStoredTreeUri(false)))) {
                    safDialogPath = path
                    safDialogPathCallback = callback
                    safDialog.show()
                } else if (!isRPlus() && context.isPathOnOTG(path) && (context.baseConfig.OTGTreeUri.isEmpty() || !context.hasProperStoredTreeUri(true))) {
                    safDialogPath = path
                    safDialogPathCallback = callback
                    otgDialog.show()
                } else {
                    callback(true)
                }
            }
        }
    }
}

@Composable
fun rememberSafCreateDocumentSdk30DialogState(): SafDialogState {
    var safDialogPath by remember {
        mutableStateOf("")
    }
    var safDialogPathCallback by remember {
        mutableStateOf<(Boolean) -> Unit>({})
    }
    val safDialog = getSafCreateDocumentSdk30DialogState(
        path = safDialogPath,
        onResult = {
            safDialogPathCallback.invoke(it)
        },
        onCancel = {}
    )
    val context = LocalContext.current

    return remember {
        object : SafDialogState() {
            override fun handle(path: String, callback: (Boolean) -> Unit) {
                if (!context.packageName.startsWith("com.simplemobiletools")) {
                    callback(true)
                } else if (!context.hasProperStoredDocumentUriSdk30(path)) {
                    safDialogPath = path
                    safDialogPathCallback = callback
                    safDialog.show()
                } else {
                    callback(true)
                }
            }
        }
    }
}

abstract class SafDialogState {
    abstract fun handle(path: String, callback: (Boolean) -> Unit)
}

@Composable
private fun getDocumentTreeLauncher(
    onResult: (Uri?) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = onResult
)

@Composable
private fun getAdvancedDocumentTreeLauncher(
    onResult: (Uri?) -> Unit
) = rememberLauncherForActivityResult(
    contract = OpenDocumentTreeShowAdvanced(),
    onResult = onResult
)

@Composable
private fun getCreateDocumentSdk30Launcher(
    onResult: (Uri?) -> Unit
) = rememberLauncherForActivityResult(
    contract = CreateDocumentTreeSdk30(),
    onResult = onResult
)

@Composable
private fun getWritePermissionDialog(
    mode: WritePermissionDialog.WritePermissionDialogMode,
    callback: () -> Unit,
    cancelCallback: () -> Unit
) = rememberAlertDialogState().apply {
    DialogMember {
        WritePermissionAlertDialog(
            alertDialogState = this,
            writePermissionDialogMode = mode,
            callback = callback,
            onCancelCallback = cancelCallback
        )
    }
}

private class OpenDocumentTreeShowAdvanced : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return super.createIntent(context, input).apply {
            putExtra(EXTRA_SHOW_ADVANCED, true)
        }
    }
}

private class CreateDocumentTreeSdk30 : ActivityResultContracts.OpenDocumentTree() {
    override fun createIntent(context: Context, input: Uri?): Intent {
        return super.createIntent(context, input).apply {
            putExtra(EXTRA_SHOW_ADVANCED, true)
            type = DocumentsContract.Document.MIME_TYPE_DIR
            putExtra(EXTRA_SHOW_ADVANCED, true)
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, input.toString().getFilenameFromPath())
        }
    }
}
