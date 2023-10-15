package com.simplemobiletools.commons.activities

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.extensions.onEventValue
import com.simplemobiletools.commons.compose.screens.ManageBlockedNumbersScreen
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.AddOrEditBlockedNumberAlertDialog
import com.simplemobiletools.commons.dialogs.ExportBlockedNumbersDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.BlockedNumber
import java.io.FileOutputStream
import java.io.OutputStream
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageBlockedNumbersActivity : BaseSimpleActivity() {

    private val config by lazy {
        baseConfig
    }

    private companion object {
        private const val PICK_IMPORT_SOURCE_INTENT = 11
        private const val PICK_EXPORT_FILE_INTENT = 21
    }

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    private val manageBlockedNumbersViewModel by viewModels<ManageBlockedNumbersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            val context = LocalContext.current
            val blockedNumbers by manageBlockedNumbersViewModel.blockedNumbers.collectAsStateWithLifecycle()
            LaunchedEffect(blockedNumbers) {
                if (blockedNumbers?.any { blockedNumber -> blockedNumber.number.isBlockedNumberPattern() } == true) {
                    maybeSetDefaultCallerIdApp()
                }
            }
            val isBlockingHiddenNumbers by config.isBlockingHiddenNumbers.collectAsStateWithLifecycle(initialValue = config.blockHiddenNumbers)
            val isBlockingUnknownNumbers by config.isBlockingUnknownNumbers.collectAsStateWithLifecycle(initialValue = config.blockUnknownNumbers)
            val isDialer = remember {
                config.appId.startsWith("com.simplemobiletools.dialer")
            }
            val isDefaultDialer: Boolean = onEventValue {
                context.isDefaultDialer()
            }

            AppThemeSurface {
                var clickedBlockedNumber by remember { mutableStateOf<BlockedNumber?>(null) }
                val addBlockedNumberDialogState = rememberAlertDialogState()

                addBlockedNumberDialogState.DialogMember {
                    AddOrEditBlockedNumberAlertDialog(
                        alertDialogState = addBlockedNumberDialogState,
                        blockedNumber = clickedBlockedNumber,
                        deleteBlockedNumber = { blockedNumber ->
                            deleteBlockedNumber(blockedNumber)
                            updateBlockedNumbers()
                        }
                    ) { blockedNumber ->
                        addBlockedNumber(blockedNumber)
                        clickedBlockedNumber = null
                        updateBlockedNumbers()
                    }
                }

                ManageBlockedNumbersScreen(
                    goBack = ::finish,
                    onAdd = {
                        clickedBlockedNumber = null
                        addBlockedNumberDialogState.show()
                    },
                    onImportBlockedNumbers = ::tryImportBlockedNumbers,
                    onExportBlockedNumbers = ::tryExportBlockedNumbers,
                    setAsDefault = ::maybeSetDefaultCallerIdApp,
                    isDialer = isDialer,
                    hasGivenPermissionToBlock = isDefaultDialer,
                    isBlockUnknownSelected = isBlockingUnknownNumbers,
                    onBlockUnknownSelectedChange = { isChecked ->
                        config.blockUnknownNumbers = isChecked
                        onCheckedSetCallerIdAsDefault(isChecked)
                    },
                    isHiddenSelected = isBlockingHiddenNumbers,
                    onHiddenSelectedChange = { isChecked ->
                        config.blockHiddenNumbers = isChecked
                        onCheckedSetCallerIdAsDefault(isChecked)
                    },
                    blockedNumbers = blockedNumbers,
                    onDelete = { selectedKeys ->
                        deleteBlockedNumbers(blockedNumbers, selectedKeys)
                    },
                    onEdit = { blockedNumber ->
                        clickedBlockedNumber = blockedNumber
                        addBlockedNumberDialogState.show()
                    },
                    onCopy = { blockedNumber ->
                        copyToClipboard(blockedNumber.number)
                    }
                )
            }
        }
    }

    private fun deleteBlockedNumbers(
        blockedNumbers: ImmutableList<BlockedNumber>?,
        selectedKeys: Set<Long>
    ) {
        if (blockedNumbers.isNullOrEmpty()) return
        blockedNumbers.filter { blockedNumber -> selectedKeys.contains(blockedNumber.id) }
            .forEach { blockedNumber ->
                deleteBlockedNumber(blockedNumber.number)
            }
        manageBlockedNumbersViewModel.updateBlockedNumbers()
    }

    private fun tryImportBlockedNumbers() {
        if (isQPlus()) {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"

                try {
                    startActivityForResult(this, PICK_IMPORT_SOURCE_INTENT)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        } else {
            handlePermission(PERMISSION_READ_STORAGE) { isAllowed ->
                if (isAllowed) {
                    pickFileToImportBlockedNumbers()
                }
            }
        }
    }

    private fun pickFileToImportBlockedNumbers() {
        FilePickerDialog(this) {
            importBlockedNumbers(it)
        }
    }

    private fun tryImportBlockedNumbersFromFile(uri: Uri) {
        when (uri.scheme) {
            "file" -> importBlockedNumbers(uri.path!!)
            "content" -> {
                val tempFile = getTempFile("blocked", "blocked_numbers.txt")
                if (tempFile == null) {
                    toast(R.string.unknown_error_occurred)
                    return
                }

                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val out = FileOutputStream(tempFile)
                    inputStream!!.copyTo(out)
                    importBlockedNumbers(tempFile.absolutePath)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }

            else -> toast(R.string.invalid_file_format)
        }
    }

    private fun importBlockedNumbers(path: String) {
        ensureBackgroundThread {
            val result = BlockedNumbersImporter(this).importBlockedNumbers(path)
            toast(
                when (result) {
                    BlockedNumbersImporter.ImportResult.IMPORT_OK -> R.string.importing_successful
                    BlockedNumbersImporter.ImportResult.IMPORT_FAIL -> R.string.no_items_found
                }
            )
            updateBlockedNumbers()
        }
    }

    private fun updateBlockedNumbers() {
        manageBlockedNumbersViewModel.updateBlockedNumbers()
    }

    private fun onCheckedSetCallerIdAsDefault(isChecked: Boolean) {
        if (isChecked) {
            maybeSetDefaultCallerIdApp()
        }
    }

    private fun maybeSetDefaultCallerIdApp() {
        if (isQPlus() && baseConfig.appId.startsWith("com.simplemobiletools.dialer")) {
            setDefaultCallerIdApp()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        when {
            requestCode == REQUEST_CODE_SET_DEFAULT_DIALER && isDefaultDialer() -> {
                updateBlockedNumbers()
            }

            requestCode == PICK_IMPORT_SOURCE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null -> {
                tryImportBlockedNumbersFromFile(resultData.data!!)
            }

            requestCode == PICK_EXPORT_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null -> {
                val outputStream = contentResolver.openOutputStream(resultData.data!!)
                exportBlockedNumbersTo(outputStream)
            }

            requestCode == REQUEST_CODE_SET_DEFAULT_CALLER_ID && resultCode != Activity.RESULT_OK -> {
                toast(R.string.must_make_default_caller_id_app, length = Toast.LENGTH_LONG)
                baseConfig.blockUnknownNumbers = false
                baseConfig.blockHiddenNumbers = false

            }
        }
    }

    private fun exportBlockedNumbersTo(outputStream: OutputStream?) {
        ensureBackgroundThread {
            val blockedNumbers = getBlockedNumbers()
            if (blockedNumbers.isEmpty()) {
                toast(R.string.no_entries_for_exporting)
            } else {
                BlockedNumbersExporter.exportBlockedNumbers(blockedNumbers, outputStream) {
                    toast(
                        when (it) {
                            ExportResult.EXPORT_OK -> R.string.exporting_successful
                            else -> R.string.exporting_failed
                        }
                    )
                }
            }
        }
    }

    private fun tryExportBlockedNumbers() {
        if (isQPlus()) {
            ExportBlockedNumbersDialog(this, baseConfig.lastBlockedNumbersExportPath, true) { file ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, file.name)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, PICK_EXPORT_FILE_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        showErrorToast(e)
                    }
                }
            }
        } else {
            handlePermission(PERMISSION_WRITE_STORAGE) { isAllowed ->
                if (isAllowed) {
                    ExportBlockedNumbersDialog(this, baseConfig.lastBlockedNumbersExportPath, false) { file ->
                        getFileOutputStream(file.toFileDirItem(this), true) { out ->
                            exportBlockedNumbersTo(out)
                        }
                    }
                }
            }
        }
    }

    internal class ManageBlockedNumbersViewModel(
        private val application: Application
    ) : AndroidViewModel(application) {


        private val _blockedNumbers: MutableStateFlow<ImmutableList<BlockedNumber>?> = MutableStateFlow(null)
        val blockedNumbers = _blockedNumbers.asStateFlow()

        init {
            updateBlockedNumbers()
        }

        fun updateBlockedNumbers() {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    application.getBlockedNumbersWithContact { list ->
                        _blockedNumbers.update { list.toImmutableList() }
                    }
                }
            }
        }
    }
}
