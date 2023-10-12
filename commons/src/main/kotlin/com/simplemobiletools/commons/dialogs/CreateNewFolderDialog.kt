package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.databinding.DialogCreateNewFolderBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isRPlus
import java.io.File

class CreateNewFolderDialog(val activity: BaseSimpleActivity, val path: String, val callback: (path: String) -> Unit) {
    init {
        val view = DialogCreateNewFolderBinding.inflate(activity.layoutInflater, null, false)
        view.folderPath.setText("${activity.humanizePath(path).trimEnd('/')}/")

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.create_new_folder) { alertDialog ->
                    alertDialog.showKeyboard(view.folderName)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                        val name = view.folderName.value
                        if (activity.validateName(path, name)) {
                            activity.createFolder(
                                "$path/$name",
                                onSuccess = {
                                    sendSuccess(alertDialog, it)
                                },
                                handleSAFDialogSdk30 = activity::handleSAFDialogSdk30,
                                handleSAFDialog = activity::handleSAFDialog,
                                handleSAFCreateDocumentDialogSdk30 = activity::handleSAFCreateDocumentDialogSdk30,
                            )
                        }
                    })
                }
            }
    }

    private fun sendSuccess(alertDialog: AlertDialog, path: String) {
        callback(path.trimEnd('/'))
        alertDialog.dismiss()
    }
}

@Composable
fun CreateNewFolderAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    path: String,
    callback: (path: String) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = alertDialogState::hide
    ) {
        DialogSurface {
            Column(
                modifier = modifier
                    .fillMaxWidth(0.95f)
                    .padding(SimpleTheme.dimens.padding.extraLarge)
            ) {
                Text(
                    text = stringResource(id = R.string.create_new_folder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = SimpleTheme.dimens.padding.medium)
                        .padding(horizontal = 24.dp),
                    color = dialogTextColor,
                    fontSize = 21.sp
                )

                TextField(
                    modifier = Modifier
                        .padding(horizontal = SimpleTheme.dimens.padding.extraLarge)
                        .padding(top = SimpleTheme.dimens.padding.extraLarge),
                    value = path,
                    enabled = false,
                    onValueChange = {}
                )

                var folderNameValue by remember { mutableStateOf("") }
                TextField(
                    modifier = Modifier
                        .padding(horizontal = SimpleTheme.dimens.padding.extraLarge)
                        .padding(top = SimpleTheme.dimens.padding.extraLarge),
                    value = folderNameValue,
                    onValueChange = {
                        folderNameValue = it
                    }
                )
                Row(
                    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        alertDialogState.hide()
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    val safDialogSdk30 = rememberSafSdk30DialogState()
                    val safDialog = rememberSafDialogState()
                    val safCreateDocumentSdk30Dialog = rememberSafCreateDocumentSdk30DialogState()

                    TextButton(onClick = {
                        if (context.validateName(path, folderNameValue)) {
                            context.createFolder(
                                "$path/$folderNameValue",
                                onSuccess = {
                                    alertDialogState.hide()
                                    callback(it.trimEnd('/'))
                                },
                                handleSAFDialogSdk30 = { path, callback ->
                                    alertDialogState.hide()
                                    safDialogSdk30.handle(path, callback)
                                },
                                handleSAFDialog = { path, callback ->
                                    alertDialogState.hide()
                                    safDialog.handle(path, callback)
                                },
                                handleSAFCreateDocumentDialogSdk30 = { path, callback ->
                                    alertDialogState.hide()
                                    safCreateDocumentSdk30Dialog.handle(path, callback)
                                }
                            )
                        }
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

private fun Context.validateName(
    path: String,
    name: String
): Boolean {
    when {
        name.isEmpty() -> toast(R.string.empty_name)
        name.isAValidFilename() -> {
            val file = File(path, name)
            if (file.exists()) {
                toast(R.string.name_taken)
                return false
            }

            return true
        }
        else -> toast(R.string.invalid_name)
    }

    return false
}

private fun Context.createFolder(
    path: String,
    onSuccess: (String) -> Unit,
    handleSAFDialogSdk30: (String, (Boolean) -> Unit) -> Unit,
    handleSAFDialog: (String, (Boolean) -> Unit) -> Unit,
    handleSAFCreateDocumentDialogSdk30: (String, (Boolean) -> Unit) -> Unit,
) {
    try {
        when {
            isRestrictedSAFOnlyRoot(path) && createAndroidSAFDirectory(path) -> onSuccess(path)
            isAccessibleWithSAFSdk30(path) -> handleSAFDialogSdk30(path) {
                if (it && createSAFDirectorySdk30(path)) {
                    onSuccess(path)
                }
            }
            needsStupidWritePermissions(path) -> handleSAFDialog(path) {
                if (it) {
                    try {
                        val documentFile = getDocumentFile(path.getParentPath())
                        val newDir = documentFile?.createDirectory(path.getFilenameFromPath()) ?: getDocumentFile(path)
                        if (newDir != null) {
                            onSuccess(path)
                        } else {
                            toast(R.string.unknown_error_occurred)
                        }
                    } catch (e: SecurityException) {
                        showErrorToast(e)
                    }
                }
            }
            File(path).mkdirs() -> onSuccess(path)
            isRPlus() && isAStorageRootFolder(path.getParentPath()) -> handleSAFCreateDocumentDialogSdk30(path) {
                if (it) {
                    onSuccess(path)
                }
            }
            else -> toast(getString(R.string.could_not_create_folder, path.getFilenameFromPath()))
        }
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

@Composable
@MyDevices
private fun CreateNewFolderAlertDialogPreview() {
    AppThemeSurface {
        CreateNewFolderAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            path = "/",
            callback = {}
        )
    }
}
