package com.simplemobiletools.commons.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.adapters.ManageBlockedNumbersAdapter
import com.simplemobiletools.commons.databinding.ActivityManageBlockedNumbersBinding
import com.simplemobiletools.commons.dialogs.AddBlockedNumberDialog
import com.simplemobiletools.commons.dialogs.ExportBlockedNumbersDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.models.BlockedNumber
import java.io.FileOutputStream
import java.io.OutputStream

class ManageBlockedNumbersActivity : BaseSimpleActivity(), RefreshRecyclerViewListener {
    private val PICK_IMPORT_SOURCE_INTENT = 11
    private val PICK_EXPORT_FILE_INTENT = 21

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    private val binding by viewBinding(ActivityManageBlockedNumbersBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateBlockedNumbers()
        setupOptionsMenu()

        updateMaterialActivityViews(
            binding.blockNumbersCoordinator,
            binding.manageBlockedNumbersList,
            useTransparentNavigation = true,
            useTopSearchMenu = false
        )
        setupMaterialScrollListener(binding.manageBlockedNumbersList, binding.blockNumbersToolbar)
        updateTextColors(binding.manageBlockedNumbersWrapper)
        updatePlaceholderTexts()

        setupBlockUnknown()
        setupBlockHidden()

        binding.manageBlockedNumbersPlaceholder2.apply {
            underlineText()
            setTextColor(getProperPrimaryColor())
            setOnClickListener {
                if (isDefaultDialer()) {
                    addOrEditBlockedNumber()
                } else {
                    launchSetDefaultDialerIntent()
                }
            }
        }
    }

    private fun setupBlockHidden() {
        val blockHiddenTitleRes =
            if (baseConfig.appId.startsWith("com.simplemobiletools.dialer")) R.string.block_hidden_calls else R.string.block_hidden_messages

        binding.blockHidden.apply {
            setText(blockHiddenTitleRes)
            isChecked = baseConfig.blockHiddenNumbers
            if (isChecked) {
                maybeSetDefaultCallerIdApp()
            }
        }

        binding.blockHiddenHolder.setOnClickListener {
            binding.blockHidden.toggle()
            baseConfig.blockHiddenNumbers = binding.blockHidden.isChecked
            if (binding.blockHidden.isChecked) {
                maybeSetDefaultCallerIdApp()
            }
        }
    }

    private fun setupBlockUnknown() {
        val blockUnknownTitleRes =
            if (baseConfig.appId.startsWith("com.simplemobiletools.dialer")) R.string.block_not_stored_calls else R.string.block_not_stored_messages

        binding.blockUnknown.apply {
            setText(blockUnknownTitleRes)
            isChecked = baseConfig.blockUnknownNumbers
            if (isChecked) {
                maybeSetDefaultCallerIdApp()
            }
        }

        binding.blockUnknownHolder.setOnClickListener {
            binding.blockUnknown.toggle()
            baseConfig.blockUnknownNumbers = binding.blockUnknown.isChecked
            if (binding.blockUnknown.isChecked) {
                maybeSetDefaultCallerIdApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.blockNumbersToolbar, NavigationIcon.Arrow)
    }

    private fun setupOptionsMenu() {
        binding.blockNumbersToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_blocked_number -> {
                    addOrEditBlockedNumber()
                    true
                }

                R.id.import_blocked_numbers -> {
                    tryImportBlockedNumbers()
                    true
                }

                R.id.export_blocked_numbers -> {
                    tryExportBlockedNumbers()
                    true
                }

                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER && isDefaultDialer()) {
            updatePlaceholderTexts()
            updateBlockedNumbers()
        } else if (requestCode == PICK_IMPORT_SOURCE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            tryImportBlockedNumbersFromFile(resultData.data!!)
        } else if (requestCode == PICK_EXPORT_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            exportBlockedNumbersTo(outputStream)
        } else if (requestCode == REQUEST_CODE_SET_DEFAULT_CALLER_ID && resultCode != Activity.RESULT_OK) {
            toast(R.string.must_make_default_caller_id_app, length = Toast.LENGTH_LONG)
            baseConfig.blockUnknownNumbers = false
            baseConfig.blockHiddenNumbers = false
            binding.blockUnknown.isChecked = false
            binding.blockHidden.isChecked = false
        }
    }

    override fun refreshItems() {
        updateBlockedNumbers()
    }

    private fun updatePlaceholderTexts() {
        binding.manageBlockedNumbersPlaceholder.text = getString(if (isDefaultDialer()) R.string.not_blocking_anyone else R.string.must_make_default_dialer)
        binding.manageBlockedNumbersPlaceholder2.text = getString(if (isDefaultDialer()) R.string.add_a_blocked_number else R.string.set_as_default)
    }

    private fun updateBlockedNumbers() {
        ensureBackgroundThread {
            getBlockedNumbersWithContact { blockedNumbers ->
                runOnUiThread {
                    ManageBlockedNumbersAdapter(this, blockedNumbers, this, binding.manageBlockedNumbersList) {
                        addOrEditBlockedNumber(it as BlockedNumber)
                    }.apply {
                        binding.manageBlockedNumbersList.adapter = this
                    }

                    binding.manageBlockedNumbersPlaceholder.beVisibleIf(blockedNumbers.isEmpty())
                    binding.manageBlockedNumbersPlaceholder2.beVisibleIf(blockedNumbers.isEmpty())

                    if (blockedNumbers.any { it.number.isBlockedNumberPattern() }) {
                        maybeSetDefaultCallerIdApp()
                    }
                }
            }
        }
    }

    private fun addOrEditBlockedNumber(currentNumber: BlockedNumber? = null) {
        AddBlockedNumberDialog(this, currentNumber) {
            updateBlockedNumbers()
        }
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
            handlePermission(PERMISSION_READ_STORAGE) {
                if (it) {
                    pickFileToImportBlockedNumbers()
                }
            }
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

    private fun pickFileToImportBlockedNumbers() {
        FilePickerDialog(this) {
            importBlockedNumbers(it)
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
            handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    ExportBlockedNumbersDialog(this, baseConfig.lastBlockedNumbersExportPath, false) { file ->
                        getFileOutputStream(file.toFileDirItem(this), true) { out ->
                            exportBlockedNumbersTo(out)
                        }
                    }
                }
            }
        }
    }

    private fun exportBlockedNumbersTo(outputStream: OutputStream?) {
        ensureBackgroundThread {
            getBlockedNumbersWithContact { blockedNumbers ->
                if (blockedNumbers.isEmpty()) {
                    toast(R.string.no_entries_for_exporting)
                } else {
                    BlockedNumbersExporter().exportBlockedNumbers(blockedNumbers, outputStream) {
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
    }

    private fun maybeSetDefaultCallerIdApp() {
        if (isQPlus() && baseConfig.appId.startsWith("com.simplemobiletools.dialer")) {
            setDefaultCallerIdApp()
        }
    }
}
