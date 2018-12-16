package com.simplemobiletools.commons.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.helpers.MyContentProvider
import com.simplemobiletools.commons.models.MyTheme
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.SharedTheme
import kotlinx.android.synthetic.main.activity_customization.*
import java.util.*

class CustomizationActivity : BaseSimpleActivity() {
    private val themeLight = 0
    private val themeDark = 1
    private val themeSolarized = 2
    private val themeDarkRed = 3
    private val themeBlackWhite = 4
    private val themeCustom = 5
    private val themeShared = 6

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAppIconColor = 0
    private var curSelectedThemeId = 0
    private var originalAppIconColor = 0
    private var hasUnsavedChanges = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
    private var storedSharedTheme: SharedTheme? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        initColorVariables()
        setupColorsPickers()

        if (isThankYouInstalled()) {
            val cursorLoader = getMyContentProviderCursorLoader()
            Thread {
                try {
                    storedSharedTheme = getSharedThemeSync(cursorLoader)
                    if (storedSharedTheme == null) {
                        baseConfig.isUsingSharedTheme = false
                    } else {
                        baseConfig.wasSharedThemeEverActivated = true
                    }

                    runOnUiThread {
                        setupThemes()
                        apply_to_all_holder.beVisibleIf(storedSharedTheme == null)
                    }
                } catch (e: Exception) {
                    toast(R.string.update_thank_you)
                    finish()
                }
            }.start()
        } else {
            setupThemes()
            baseConfig.isUsingSharedTheme = false
        }

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross)
        updateTextColors(customization_holder)
        originalAppIconColor = baseConfig.appIconColor
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
        setTheme(getThemeId(curPrimaryColor))

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customization, menu)
        menu.findItem(R.id.save).isVisible = hasUnsavedChanges
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> saveChanges(true)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges) {
            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            put(themeLight, MyTheme(R.string.light_theme, R.color.theme_light_text_color, R.color.theme_light_background_color, R.color.color_primary, R.color.color_primary))
            put(themeDark, MyTheme(R.string.dark_theme, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.color_primary, R.color.color_primary))
            //put(themeSolarized, MyTheme(R.string.solarized, R.color.theme_solarized_text_color, R.color.theme_solarized_background_color, R.color.theme_solarized_primary_color))
            put(themeDarkRed, MyTheme(R.string.dark_red, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.theme_dark_red_primary_color, R.color.md_red_700))
            put(themeBlackWhite, MyTheme(R.string.black_white, android.R.color.white, android.R.color.black, android.R.color.black, R.color.md_grey_black))
            put(themeCustom, MyTheme(R.string.custom, 0, 0, 0, 0))

            if (storedSharedTheme != null) {
                put(themeShared, MyTheme(R.string.shared, 0, 0, 0, 0))
            }
        }
        setupThemePicker()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        customization_theme.text = getThemeText()
        customization_theme_holder.setOnClickListener {
            val items = arrayListOf<RadioItem>()
            for ((key, value) in predefinedThemes) {
                items.add(RadioItem(key, getString(value.nameId)))
            }

            RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
                if (it == themeShared && !isThankYouInstalled()) {
                    PurchaseThankYouDialog(this)
                    return@RadioGroupDialog
                }

                updateColorTheme(it as Int, true)
                if (it != themeCustom && it != themeShared && !baseConfig.wasCustomThemeSwitchDescriptionShown) {
                    baseConfig.wasCustomThemeSwitchDescriptionShown = true
                    toast(R.string.changing_color_description)
                }
            }
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        customization_theme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == themeCustom) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    curAppIconColor = baseConfig.customAppIconColor
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                    baseConfig.appIconColor = curAppIconColor
                }
            } else if (curSelectedThemeId == themeShared) {
                if (useStored) {
                    storedSharedTheme?.apply {
                        curTextColor = textColor
                        curBackgroundColor = backgroundColor
                        curPrimaryColor = primaryColor
                        curAppIconColor = appIconColor
                    }
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                }
            } else {
                val theme = predefinedThemes[curSelectedThemeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)
                curPrimaryColor = getColor(theme.primaryColorId)
                curAppIconColor = getColor(theme.appIconColorId)
                setTheme(getThemeId(curPrimaryColor))
                colorChanged()
            }
        }

        hasUnsavedChanges = true
        invalidateOptionsMenu()
        updateTextColors(customization_holder, curTextColor)
        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
    }

    private fun getCurrentThemeId(): Int {
        if (baseConfig.isUsingSharedTheme) {
            return themeShared
        }

        var themeId = themeCustom
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != themeCustom && it.key != themeShared }) {
                if (curTextColor == getColor(value.textColorId) &&
                        curBackgroundColor == getColor(value.backgroundColorId) &&
                        curPrimaryColor == getColor(value.primaryColorId) &&
                        curAppIconColor == getColor(value.appIconColorId)
                ) {
                    themeId = key
                }
            }
        }
        return themeId
    }

    private fun getThemeText(): String {
        var nameId = R.string.custom
        for ((key, value) in predefinedThemes) {
            if (key == curSelectedThemeId) {
                nameId = value.nameId
            }
        }
        return getString(nameId)
    }

    private fun promptSaveDiscard() {
        ConfirmationAdvancedDialog(this, "", R.string.save_before_closing, R.string.save, R.string.discard) {
            if (it) {
                saveChanges(true)
            } else {
                resetColors()
                finish()
            }
        }
    }

    private fun saveChanges(finishAfterSave: Boolean) {
        val didAppIconColorChange = curAppIconColor != originalAppIconColor
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
            appIconColor = curAppIconColor
        }

        if (didAppIconColorChange) {
            checkAppIconColor()
        }

        if (curSelectedThemeId == themeShared) {
            val newSharedTheme = SharedTheme(curTextColor, curBackgroundColor, curPrimaryColor, curAppIconColor)
            updateSharedTheme(newSharedTheme)
            Intent().apply {
                action = MyContentProvider.SHARED_THEME_UPDATED
                sendBroadcast(this)
            }
        }

        baseConfig.isUsingSharedTheme = curSelectedThemeId == themeShared
        hasUnsavedChanges = false
        if (finishAfterSave) {
            finish()
        } else {
            invalidateOptionsMenu()
        }
    }

    private fun resetColors() {
        hasUnsavedChanges = false
        invalidateOptionsMenu()
        initColorVariables()
        setupColorsPickers()
        updateBackgroundColor()
        updateActionbarColor()
        invalidateOptionsMenu()
        updateTextColors(customization_holder)
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAppIconColor = baseConfig.appIconColor
    }

    private fun setupColorsPickers() {
        customization_text_color.setFillWithStroke(curTextColor, curBackgroundColor)
        customization_primary_color.setFillWithStroke(curPrimaryColor, curBackgroundColor)
        customization_background_color.setFillWithStroke(curBackgroundColor, curBackgroundColor)
        customization_app_icon_color.setFillWithStroke(curAppIconColor, curBackgroundColor)

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
        customization_app_icon_color_holder.setOnClickListener { pickAppIconColor() }
        apply_to_all_holder.setOnClickListener { applyToAll() }
    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        invalidateOptionsMenu()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateTextColors(customization_holder, color)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateBackgroundColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        updateActionbarColor(color)
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        curPrimaryLineColorPicker = LineColorPickerDialog(this, curPrimaryColor, true) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
            }
        }
    }

    private fun pickAppIconColor() {
        LineColorPickerDialog(this, curAppIconColor, false, R.array.md_app_icon_colors, getAppIconIDs()) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAppIconColor, color)) {
                    curAppIconColor = color
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
    }

    private fun getUpdatedTheme() = if (curSelectedThemeId == themeShared) themeShared else themeCustom

    private fun applyToAll() {
        if (isThankYouInstalled()) {
            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
                Intent().apply {
                    action = MyContentProvider.SHARED_THEME_ACTIVATED
                    sendBroadcast(this)
                }

                if (!predefinedThemes.containsKey(themeShared)) {
                    predefinedThemes[themeShared] = MyTheme(R.string.shared, 0, 0, 0, 0)
                }
                baseConfig.wasSharedThemeEverActivated = true
                apply_to_all_holder.beGone()
                updateColorTheme(themeShared)
                saveChanges(false)
            }
        } else {
            PurchaseThankYouDialog(this)
        }
    }
}
