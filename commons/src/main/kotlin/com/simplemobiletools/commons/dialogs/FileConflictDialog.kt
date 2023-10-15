package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.DialogSurface
import com.simplemobiletools.commons.compose.alert_dialog.dialogTextColor
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.components.RadioGroupDialogComponent
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.databinding.DialogFileConflictBinding
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.CONFLICT_KEEP_BOTH
import com.simplemobiletools.commons.helpers.CONFLICT_MERGE
import com.simplemobiletools.commons.helpers.CONFLICT_OVERWRITE
import com.simplemobiletools.commons.helpers.CONFLICT_SKIP
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.FileDirItemReadOnly
import com.simplemobiletools.commons.models.asReadOnly
import kotlinx.collections.immutable.toImmutableList

class FileConflictDialog(
    val activity: Activity, val fileDirItem: FileDirItem, val showApplyToAllCheckbox: Boolean,
    val callback: (resolution: Int, applyForAll: Boolean) -> Unit
) {
    val view = DialogFileConflictBinding.inflate(activity.layoutInflater, null, false)

    init {
        view.apply {
            val stringBase = if (fileDirItem.isDirectory) R.string.folder_already_exists else R.string.file_already_exists
            conflictDialogTitle.text = String.format(activity.getString(stringBase), fileDirItem.name)
            conflictDialogApplyToAll.isChecked = activity.baseConfig.lastConflictApplyToAll
            conflictDialogApplyToAll.beVisibleIf(showApplyToAllCheckbox)
            conflictDialogDivider.root.beVisibleIf(showApplyToAllCheckbox)
            conflictDialogRadioMerge.beVisibleIf(fileDirItem.isDirectory)

            val resolutionButton = when (activity.baseConfig.lastConflictResolution) {
                CONFLICT_OVERWRITE -> conflictDialogRadioOverwrite
                CONFLICT_MERGE -> conflictDialogRadioMerge
                else -> conflictDialogRadioSkip
            }
            resolutionButton.isChecked = true
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this)
            }
    }

    private fun dialogConfirmed() {
        val resolution = when (view.conflictDialogRadioGroup.checkedRadioButtonId) {
            view.conflictDialogRadioSkip.id -> CONFLICT_SKIP
            view.conflictDialogRadioMerge.id -> CONFLICT_MERGE
            view.conflictDialogRadioKeepBoth.id -> CONFLICT_KEEP_BOTH
            else -> CONFLICT_OVERWRITE
        }

        val applyToAll = view.conflictDialogApplyToAll.isChecked
        activity.baseConfig.apply {
            lastConflictApplyToAll = applyToAll
            lastConflictResolution = resolution
        }

        callback(resolution, applyToAll)
    }
}

@Composable
fun FileConflictAlertDialog(
    alertDialogState: AlertDialogState,
    fileDirItem: FileDirItemReadOnly,
    showApplyToAll: Boolean,
    modifier: Modifier = Modifier,
    callback: (resolution: Int, applyForAll: Boolean) -> Unit
) {
    val context = LocalContext.current
    var isShowApplyForAllChecked by remember { mutableStateOf(context.baseConfig.lastConflictApplyToAll) }
    val selections = remember {
        buildFileConflictEntries(context, fileDirItem.isDirectory)
    }
    val kinds = remember {
        selections.values.toImmutableList()
    }
    val initiallySelected = remember {
        requireNotNull(selections[context.baseConfig.lastConflictResolution]) {
            "Incorrect format, please check selections"
        }
    }

    val (selected, setSelected) = remember { mutableStateOf(initiallySelected) }

    AlertDialog(
        onDismissRequest = alertDialogState::hide
    ) {
        DialogSurface {
            Box {
                Column(
                    modifier = modifier
                        .padding(bottom = 64.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = String.format(
                            stringResource(id = if (fileDirItem.isDirectory) R.string.folder_already_exists else R.string.file_already_exists),
                            fileDirItem.name
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = SimpleTheme.dimens.padding.medium)
                            .padding(horizontal = 24.dp),
                        color = dialogTextColor,
                        fontSize = 21.sp
                    )

                    RadioGroupDialogComponent(
                        items = kinds, selected = selected,
                        setSelected = setSelected,
                        modifier = Modifier.padding(
                            vertical = SimpleTheme.dimens.padding.extraLarge,
                        )
                    )

                    if (showApplyToAll) {
                        SettingsHorizontalDivider()

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            DialogCheckBoxWithRadioAlignmentComponent(
                                label = stringResource(id = R.string.apply_to_all),
                                initialValue = isShowApplyForAllChecked,
                                onChange = { isShowApplyForAllChecked = it },
                                modifier = Modifier.padding(horizontal = SimpleTheme.dimens.padding.medium)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = SimpleTheme.dimens.padding.extraLarge,
                            bottom = SimpleTheme.dimens.padding.extraLarge,
                            end = SimpleTheme.dimens.padding.extraLarge
                        )
                        .align(Alignment.BottomStart)
                ) {
                    TextButton(onClick = {
                        alertDialogState.hide()
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    TextButton(onClick = {
                        alertDialogState.hide()
                        callback(selections.filterValues { it == selected }.keys.first(), isShowApplyForAllChecked)
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

private fun buildFileConflictEntries(context: Context, directory: Boolean) =
    buildMap {
        this[CONFLICT_SKIP] = context.getString(R.string.skip)
        if (directory) {
            this[CONFLICT_SKIP] = context.getString(R.string.merge)
        }
        this[CONFLICT_OVERWRITE] = context.getString(R.string.overwrite)
        this[CONFLICT_KEEP_BOTH] = context.getString(R.string.keep_both)
    }


@MyDevices
@Composable
private fun FileConflictAlertDialogPreview() {
    AppThemeSurface {
        FileConflictAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            fileDirItem = FileDirItem("", name = "test", children = 1).asReadOnly(),
            showApplyToAll = true
        ) { _, _ -> }
    }
}
