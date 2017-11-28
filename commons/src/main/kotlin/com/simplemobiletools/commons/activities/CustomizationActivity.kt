package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.MyTheme
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.activity_customization.*
import java.util.*

class CustomizationActivity : BaseSimpleActivity() {
    private val THEME_LIGHT = 0
    private val THEME_DARK = 1
    private val THEME_SOLARIZED = 2
    private val THEME_DARK_RED = 3
    private val THEME_CUSTOM = 4
    private val THEME_SHARED = 5

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curSelectedThemeId = 0
    private var hasUnsavedChanges = false
    private var isLineColorPickerVisible = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        predefinedThemes.apply {
            put(THEME_LIGHT, MyTheme(R.string.light_theme, R.color.theme_light_text_color, R.color.theme_light_background_color, R.color.color_primary))
            put(THEME_DARK, MyTheme(R.string.dark_theme, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.color_primary))
            put(THEME_SOLARIZED, MyTheme(R.string.solarized, R.color.theme_solarized_text_color, R.color.theme_solarized_background_color, R.color.theme_solarized_primary_color))
            put(THEME_DARK_RED, MyTheme(R.string.dark_red, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.theme_dark_red_primary_color))
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0))
        }

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cross)
        updateTextColors(customization_holder)
        initColorVariables()
        setupColorsPickers()

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
        apply_to_all_holder.setOnClickListener { applyToAll() }
        apply_to_all_holder.beGoneIf(baseConfig.wasSharedThemeShown)
        setupThemePicker()
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> saveChanges()
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

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        customization_theme.text = getThemeText()
        customization_theme_holder.setOnClickListener {
            val items = arrayListOf<RadioItem>()
            for ((key, value) in predefinedThemes) {
                items.add(RadioItem(key, getString(value.nameId)))
            }

            RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
                updateColorTheme(it as Int, true)
                if (it != THEME_CUSTOM) {
                    toast(R.string.changing_color_description)
                }
            }
        }
    }

    private fun updateColorTheme(themeId: Int = THEME_CUSTOM, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        customization_theme.text = getThemeText()

        resources.apply {
            if (themeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                }
            } else {
                val theme = predefinedThemes[themeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)
                curPrimaryColor = getColor(theme.primaryColorId)
                setTheme(getThemeId(curPrimaryColor))
                colorChanged()
            }
        }

        updateTextColors(customization_holder, curTextColor)
        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
    }

    private fun getCurrentThemeId(): Int {
        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM }) {
                if (curTextColor == getColor(value.textColorId) && curBackgroundColor == getColor(value.backgroundColorId) && curPrimaryColor == getColor(value.primaryColorId)) {
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
                saveChanges()
            } else {
                resetColors()
                finish()
            }
        }
    }

    private fun saveChanges() {
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
        }
        hasUnsavedChanges = false
        finish()
    }

    private fun resetColors() {
        hasUnsavedChanges = false
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
    }

    private fun setupColorsPickers() {
        customization_text_color.setBackgroundWithStroke(curTextColor, curBackgroundColor)
        customization_primary_color.setBackgroundWithStroke(curPrimaryColor, curBackgroundColor)
        customization_background_color.setBackgroundWithStroke(curBackgroundColor, curBackgroundColor)
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
        ColorPickerDialog(this, curTextColor) {
            if (hasColorChanged(curTextColor, it)) {
                setCurrentTextColor(it)
                colorChanged()
                updateColorTheme()
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor) {
            if (hasColorChanged(curBackgroundColor, it)) {
                setCurrentBackgroundColor(it)
                colorChanged()
                updateColorTheme()
            }
        }
    }

    private fun pickPrimaryColor() {
        isLineColorPickerVisible = true
        curPrimaryLineColorPicker = LineColorPickerDialog(this, curPrimaryColor) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            isLineColorPickerVisible = false
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme()
                    setTheme(getThemeId(color))
                }
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
            }
        }
    }

    private fun applyToAll() {
        if (isThankYouInstalled()) {
            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
                baseConfig.wasSharedThemeShown = true
                apply_to_all_holder.beGone()
            }
        } else {
            PurchaseThankYouDialog(this)
        }
    }
}
