package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.setBackgroundWithStroke
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.THEME_CUSTOM
import com.simplemobiletools.commons.helpers.THEME_DARK
import com.simplemobiletools.commons.helpers.THEME_LIGHT
import com.simplemobiletools.commons.models.RadioItem
import kotlinx.android.synthetic.main.activity_customization.*

class CustomizationActivity : BaseSimpleActivity() {
    var curTextColor = 0
    var curBackgroundColor = 0
    var curPrimaryColor = 0
    var hasUnsavedChanges = false

    var defaultTextColor = 0
    var defaultBackgroundColor = 0
    var defaultPrimaryColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        resources.apply {
            defaultTextColor = getColor(R.color.default_text_color)
            defaultBackgroundColor = getColor(R.color.default_background_color)
            defaultPrimaryColor = getColor(R.color.color_primary)
        }

        updateTextColors(customization_holder)
        initColorVariables()
        setupColorsPickers()

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
        customization_restore_defaults.setOnClickListener { restoreDefaultColors() }
        setupThemePicker()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customization, menu)
        menu.findItem(R.id.undo).isVisible = hasUnsavedChanges
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> confirmUndoChanges()
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

    private fun restoreDefaultColors() {
        setCurrentTextColor(defaultTextColor)
        setCurrentBackgroundColor(defaultBackgroundColor)
        setCurrentPrimaryColor(defaultPrimaryColor)
        colorChanged()
    }

    private fun setupThemePicker() {
        customization_theme.text = getThemeText()
        customization_theme_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(THEME_LIGHT, getString(R.string.light_theme)),
                    RadioItem(THEME_DARK, getString(R.string.dark_theme)),
                    RadioItem(THEME_CUSTOM, getString(R.string.custom)))

            RadioGroupDialog(this@CustomizationActivity, items, baseConfig.colorTheme) {
                baseConfig.colorTheme = it as Int
                customization_theme.text = getThemeText()
            }
        }
    }

    private fun getThemeText() = getString(when (baseConfig.colorTheme) {
        THEME_LIGHT -> R.string.light_theme
        THEME_DARK -> R.string.dark_theme
        else -> R.string.custom
    })

    private fun promptSaveDiscard() {
        ConfirmationAdvancedDialog(this, "", R.string.save_before_closing, R.string.save, R.string.discard, object : ConfirmationAdvancedDialog.Listener {
            override fun onPositive() {
                saveChanges()
            }

            override fun onNegative() {
                resetColors()
                finish()
            }
        })
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

    private fun confirmUndoChanges() {
        ConfirmationDialog(this, "", R.string.undo_changes_confirmation, R.string.yes, R.string.no) {
            resetColors()
        }
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
        customization_text_color.setBackgroundColor(curTextColor)
        customization_primary_color.setBackgroundColor(curPrimaryColor)
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
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor) {
            if (hasColorChanged(curBackgroundColor, it)) {
                setCurrentBackgroundColor(it)
                colorChanged()
            }
        }
    }

    private fun pickPrimaryColor() {
        ColorPickerDialog(this, curPrimaryColor) {
            if (hasColorChanged(curPrimaryColor, it)) {
                setCurrentPrimaryColor(it)
                colorChanged()
            }
        }
    }
}
