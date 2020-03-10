package com.simplemobiletools.commons.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.MyTheme
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.SharedTheme
import kotlinx.android.synthetic.main.activity_customization.*
import java.util.*

class CustomizationActivity : BaseSimpleActivity() {
    private val THEME_LIGHT = 0
    private val THEME_DARK = 1
    private val THEME_SOLARIZED = 2
    private val THEME_DARK_RED = 3
    private val THEME_BLACK_WHITE = 4
    private val THEME_CUSTOM = 5
    private val THEME_SHARED = 6

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAppIconColor = 0
    private var curSelectedThemeId = 0
    private var originalAppIconColor = 0
    private var lastSavePromptTS = 0L
    private var curNavigationBarColor = INVALID_NAVIGATION_BAR_COLOR
    private var hasUnsavedChanges = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
    private var storedSharedTheme: SharedTheme? = null
    private var menu: Menu? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        if (baseConfig.defaultNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR && baseConfig.navigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
            baseConfig.defaultNavigationBarColor = window.navigationBarColor
            baseConfig.navigationBarColor = window.navigationBarColor
        }

        initColorVariables()
        setupColorsPickers()

        if (isThankYouInstalled()) {
            val cursorLoader = getMyContentProviderCursorLoader()
            ensureBackgroundThread {
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
            }
        } else {
            setupThemes()
            baseConfig.isUsingSharedTheme = false
        }

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross_vector)
        updateTextColors(customization_holder)
        originalAppIconColor = baseConfig.appIconColor
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
        updateNavigationBarColor(curNavigationBarColor)
        setTheme(getThemeId(curPrimaryColor))

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customization, menu)
        menu.findItem(R.id.save).isVisible = hasUnsavedChanges
        updateMenuItemColors(menu, true, curPrimaryColor)
        this.menu = menu
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
        if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            put(THEME_LIGHT, MyTheme(R.string.light_theme, R.color.theme_light_text_color, R.color.theme_light_background_color, R.color.color_primary, R.color.color_primary))
            put(THEME_DARK, MyTheme(R.string.dark_theme, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.color_primary, R.color.color_primary))
            //put(THEME_SOLARIZED, MyTheme(R.string.solarized, R.color.theme_solarized_text_color, R.color.theme_solarized_background_color, R.color.theme_solarized_primary_color))
            put(THEME_DARK_RED, MyTheme(R.string.dark_red, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.theme_dark_red_primary_color, R.color.md_red_700))
            put(THEME_BLACK_WHITE, MyTheme(R.string.black_white, android.R.color.white, android.R.color.black, android.R.color.black, R.color.md_grey_black))
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))

            if (storedSharedTheme != null) {
                put(THEME_SHARED, MyTheme(R.string.shared, 0, 0, 0, 0))
            }
        }
        setupThemePicker()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        customization_theme.text = getThemeText()
        customization_theme_holder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                themePickerClicked()
            } else {
                ConfirmationDialog(this, "", R.string.app_icon_color_warning, R.string.ok, 0) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    themePickerClicked()
                }
            }
        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, getString(value.nameId)))
        }

        RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
            if (it == THEME_SHARED && !isThankYouInstalled()) {
                PurchaseThankYouDialog(this)
                return@RadioGroupDialog
            }

            updateColorTheme(it as Int, true)
            if (it != THEME_CUSTOM && it != THEME_SHARED && !baseConfig.wasCustomThemeSwitchDescriptionShown) {
                baseConfig.wasCustomThemeSwitchDescriptionShown = true
                toast(R.string.changing_color_description)
            }
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        customization_theme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    curNavigationBarColor = baseConfig.customNavigationBarColor
                    curAppIconColor = baseConfig.customAppIconColor
                    setTheme(getThemeId(curPrimaryColor))
                    updateMenuItemColors(menu, true, curPrimaryColor)
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                    baseConfig.customNavigationBarColor = curNavigationBarColor
                    baseConfig.customAppIconColor = curAppIconColor
                }
            } else if (curSelectedThemeId == THEME_SHARED) {
                if (useStored) {
                    storedSharedTheme?.apply {
                        curTextColor = textColor
                        curBackgroundColor = backgroundColor
                        curPrimaryColor = primaryColor
                        curAppIconColor = appIconColor
                        curNavigationBarColor = navigationBarColor
                    }
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                    updateMenuItemColors(menu, true, curPrimaryColor)
                }
            } else {
                val theme = predefinedThemes[curSelectedThemeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)
                curPrimaryColor = getColor(theme.primaryColorId)
                curAppIconColor = getColor(theme.appIconColorId)
                curNavigationBarColor = getThemeNavigationColor(curSelectedThemeId)
                setTheme(getThemeId(curPrimaryColor))
                colorChanged()
                updateMenuItemColors(menu, true, curPrimaryColor)
            }
        }

        hasUnsavedChanges = true
        invalidateOptionsMenu()
        updateTextColors(customization_holder, curTextColor)
        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
        updateNavigationBarColor(curNavigationBarColor)
    }

    private fun getCurrentThemeId(): Int {
        if (baseConfig.isUsingSharedTheme) {
            return THEME_SHARED
        }

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM && it.key != THEME_SHARED }) {
                if (curTextColor == getColor(value.textColorId) &&
                        curBackgroundColor == getColor(value.backgroundColorId) &&
                        curPrimaryColor == getColor(value.primaryColorId) &&
                        curAppIconColor == getColor(value.appIconColorId) &&
                        curNavigationBarColor == getThemeNavigationColor(key)
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

    private fun getThemeNavigationColor(themeId: Int) = if (themeId == THEME_BLACK_WHITE) Color.BLACK else baseConfig.defaultNavigationBarColor

    private fun promptSaveDiscard() {
        lastSavePromptTS = System.currentTimeMillis()
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
            navigationBarColor = curNavigationBarColor
        }

        if (didAppIconColorChange) {
            checkAppIconColor()
        }

        if (curSelectedThemeId == THEME_SHARED) {
            val newSharedTheme = SharedTheme(curTextColor, curBackgroundColor, curPrimaryColor, curAppIconColor, curNavigationBarColor)
            updateSharedTheme(newSharedTheme)
            Intent().apply {
                action = MyContentProvider.SHARED_THEME_UPDATED
                sendBroadcast(this)
            }
        }

        baseConfig.isUsingSharedTheme = curSelectedThemeId == THEME_SHARED
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
        updateNavigationBarColor()
        invalidateOptionsMenu()
        updateTextColors(customization_holder)
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAppIconColor = baseConfig.appIconColor
        curNavigationBarColor = baseConfig.navigationBarColor
    }

    private fun setupColorsPickers() {
        customization_text_color.setFillWithStroke(curTextColor, curBackgroundColor)
        customization_primary_color.setFillWithStroke(curPrimaryColor, curBackgroundColor)
        customization_background_color.setFillWithStroke(curBackgroundColor, curBackgroundColor)
        customization_app_icon_color.setFillWithStroke(curAppIconColor, curBackgroundColor)
        customization_navigation_bar_color.setFillWithStroke(curNavigationBarColor, curBackgroundColor)

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
        customization_navigation_bar_color_holder.setOnClickListener { pickNavigationBarColor() }
        apply_to_all_holder.setOnClickListener { applyToAll() }
        customization_app_icon_color_holder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                pickAppIconColor()
            } else {
                ConfirmationDialog(this, "", R.string.app_icon_color_warning, R.string.ok, 0) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    pickAppIconColor()
                }
            }
        }
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

    private fun setCurrentNavigationBarColor(color: Int) {
        curNavigationBarColor = color
        updateNavigationBarColor(color)
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
        if (!packageName.startsWith("com.simplemobiletools.", true) && baseConfig.appRunCount > 50) {
            finish()
            return
        }

        curPrimaryLineColorPicker = LineColorPickerDialog(this, curPrimaryColor, true, menu = menu) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
                updateMenuItemColors(menu, true, color)
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(menu, true, curPrimaryColor)
            }
        }
    }

    private fun pickNavigationBarColor() {
        ColorPickerDialog(this, curNavigationBarColor, true, true, currentColorCallback = {
            updateNavigationBarColor(it)
        }, callback = { wasPositivePressed, color ->
            if (wasPositivePressed) {
                setCurrentNavigationBarColor(color)
                colorChanged()
                updateColorTheme(getUpdatedTheme())
            } else {
                updateNavigationBarColor(curNavigationBarColor)
            }
        })
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

    private fun getUpdatedTheme() = if (curSelectedThemeId == THEME_SHARED) THEME_SHARED else THEME_CUSTOM

    private fun applyToAll() {
        if (isThankYouInstalled()) {
            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
                Intent().apply {
                    action = MyContentProvider.SHARED_THEME_ACTIVATED
                    sendBroadcast(this)
                }

                if (!predefinedThemes.containsKey(THEME_SHARED)) {
                    predefinedThemes[THEME_SHARED] = MyTheme(R.string.shared, 0, 0, 0, 0)
                }
                baseConfig.wasSharedThemeEverActivated = true
                apply_to_all_holder.beGone()
                updateColorTheme(THEME_SHARED)
                saveChanges(false)
            }
        } else {
            PurchaseThankYouDialog(this)
        }
    }
}
