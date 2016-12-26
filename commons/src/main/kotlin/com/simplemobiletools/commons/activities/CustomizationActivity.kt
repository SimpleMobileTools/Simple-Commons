package com.simplemobiletools.commons.activities

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.updateTextColors
import kotlinx.android.synthetic.main.activity_customization.*
import yuku.ambilwarna.AmbilWarnaDialog

class CustomizationActivity : BaseSimpleActivity() {
    var curTextColor = 0
    var curBackgroundColor = 0
    var curPrimaryColor = 0
    var hasUnsavedChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        updateTextColors(customization_holder)
        initColorVariables()
        setupColorsPickers()

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
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
        customView(customization_background_color, curBackgroundColor, curBackgroundColor.getContrastColor())
    }

    fun customView(view: View, backgroundColor: Int, borderColor: Int) {
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(backgroundColor)
            setStroke(2, borderColor)
            view.setBackgroundDrawable(this)
        }
    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        invalidateOptionsMenu()
    }

    private fun pickTextColor() {
        AmbilWarnaDialog(this, curTextColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                if (hasColorChanged(curTextColor, color)) {
                    curTextColor = color
                    updateTextColors(customization_holder, color)
                    colorChanged()
                }
            }
        }).show()
    }

    private fun pickBackgroundColor() {
        AmbilWarnaDialog(this, curBackgroundColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    curBackgroundColor = color
                    updateBackgroundColor(color)
                    colorChanged()
                }
            }
        }).show()
    }

    private fun pickPrimaryColor() {
        AmbilWarnaDialog(this, curPrimaryColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    curPrimaryColor = color
                    updateActionbarColor(color)
                    colorChanged()
                }
            }
        }).show()
    }
}
