package com.simplemobiletools.commons.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.extensions.config
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.databinding.DialogRadioGroupBinding
import com.simplemobiletools.commons.databinding.RadioButtonBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.collections.immutable.toImmutableList

private val ID_INTERNAL = 1
private val ID_SD = 2
private val ID_OTG = 3
private val ID_ROOT = 4

/**
 * A dialog for choosing between internal, root, SD card (optional) storage
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param currPath current path to decide which storage should be preselected
 * @param pickSingleOption if only one option like "Internal" is available, select it automatically
 * @param callback an anonymous function
 *
 */
class StoragePickerDialog(
    val activity: BaseSimpleActivity, val currPath: String, val showRoot: Boolean, pickSingleOption: Boolean,
    val callback: (pickedPath: String) -> Unit
) {
    private lateinit var radioGroup: RadioGroup
    private var dialog: AlertDialog? = null
    private var defaultSelectedId = 0
    private val availableStorages = ArrayList<String>()

    init {
        availableStorages.add(activity.internalStoragePath)
        when {
            activity.hasExternalSDCard() -> availableStorages.add(activity.sdCardPath)
            activity.hasOTGConnected() -> availableStorages.add("otg")
            showRoot -> availableStorages.add("root")
        }

        if (pickSingleOption && availableStorages.size == 1) {
            callback(availableStorages.first())
        } else {
            initDialog()
        }
    }

    private fun initDialog() {
        val inflater = LayoutInflater.from(activity)
        val resources = activity.resources
        val layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val view = DialogRadioGroupBinding.inflate(inflater, null, false)
        radioGroup = view.dialogRadioGroup
        val basePath = currPath.getBasePath(activity)

        val internalButton = RadioButtonBinding.inflate(inflater, null, false).root
        internalButton.apply {
            id = ID_INTERNAL
            text = resources.getString(R.string.internal)
            isChecked = basePath == context.internalStoragePath
            setOnClickListener { internalPicked() }
            if (isChecked) {
                defaultSelectedId = id
            }
        }
        radioGroup.addView(internalButton, layoutParams)

        if (activity.hasExternalSDCard()) {
            val sdButton = RadioButtonBinding.inflate(inflater, null, false).root
            sdButton.apply {
                id = ID_SD
                text = resources.getString(R.string.sd_card)
                isChecked = basePath == context.sdCardPath
                setOnClickListener { sdPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            radioGroup.addView(sdButton, layoutParams)
        }

        if (activity.hasOTGConnected()) {
            val otgButton = RadioButtonBinding.inflate(inflater, null, false).root
            otgButton.apply {
                id = ID_OTG
                text = resources.getString(R.string.usb)
                isChecked = basePath == context.otgPath
                setOnClickListener { otgPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            radioGroup.addView(otgButton, layoutParams)
        }

        // allow for example excluding the root folder at the gallery
        if (showRoot) {
            val rootButton = RadioButtonBinding.inflate(inflater, null, false).root
            rootButton.apply {
                id = ID_ROOT
                text = resources.getString(R.string.root)
                isChecked = basePath == "/"
                setOnClickListener { rootPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            radioGroup.addView(rootButton, layoutParams)
        }

        activity.getAlertDialogBuilder().apply {
            activity.setupDialogStuff(view.root, this, R.string.select_storage) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun internalPicked() {
        dialog?.dismiss()
        callback(activity.internalStoragePath)
    }

    private fun sdPicked() {
        dialog?.dismiss()
        callback(activity.sdCardPath)
    }

    private fun otgPicked() {
        activity.handleOTGPermission {
            if (it) {
                callback(activity.otgPath)
                dialog?.dismiss()
            } else {
                radioGroup.check(defaultSelectedId)
            }
        }
    }

    private fun rootPicked() {
        dialog?.dismiss()
        callback("/")
    }
}

@Composable
fun StoragePickerAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    currPath: String,
    showRoot: Boolean = false,
    pickSingleOption: Boolean = true,
    callback: (pickedPath: String) -> Unit
) {
    val context = LocalContext.current
    val internalString = stringResource(id = R.string.internal)
    val sdCardString = stringResource(id = R.string.sd_card)
    val usbString = stringResource(id = R.string.usb)
    val rootString = stringResource(id = R.string.root)
    val items by remember {
        derivedStateOf {
            val list = mutableListOf<RadioItem>()
            list.add(RadioItem(ID_INTERNAL, internalString, context.internalStoragePath))
            if (context.hasExternalSDCard()) {
                list.add(RadioItem(ID_SD, sdCardString, context.sdCardPath))
            }
            if (context.hasOTGConnected()) {
                list.add(RadioItem(ID_OTG, usbString, context.otgPath))
            }
            if (showRoot) {
                list.add(RadioItem(ID_ROOT, rootString, "/"))
            }
            list.toImmutableList()
        }
    }

    SideEffect {
        if (pickSingleOption && items.size == 1 && alertDialogState.isShown) {
            alertDialogState.hide()
            callback(items.first().value as String)
        }
    }

    val basePath by remember {
        derivedStateOf {
            currPath.getBasePath(context)
        }
    }
    val selectedId by remember {
        derivedStateOf {
            items.withIndex().first { it.value.value == basePath }.value.id
        }
    }

    val otgPermissionDialogState = getWritePermissionDialog(
        onResult = {
            alertDialogState.hide()
            callback(context.otgPath)
        },
        onCancel = alertDialogState::show
    )

    RadioGroupAlertDialog(
        alertDialogState = alertDialogState,
        modifier = modifier,
        items = items,
        titleId = R.string.select_storage,
        selectedItemId = selectedId,
        callback = {
            if (it == ID_OTG) {
                if (context.config.OTGTreeUri.isNotEmpty()) {
                    callback(context.otgPath)
                } else {
                    alertDialogState.show()
                    otgPermissionDialogState.show()
                }
            } else {
                callback(it as String)
            }
        }
    )
}


@Composable
@MyDevices
private fun StoragePickerAlertDialogPreview() {
    AppThemeSurface {
        StoragePickerAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            currPath = "",
            callback = {}
        )
    }
}
