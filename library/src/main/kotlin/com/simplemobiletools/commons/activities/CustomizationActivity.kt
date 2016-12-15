package com.simplemobiletools.commons.activities

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.getContrastColor
import kotlinx.android.synthetic.main.activity_customization.*
import yuku.ambilwarna.AmbilWarnaDialog

class CustomizationActivity : BaseSimpleActivity() {
    var curTextColor = 0
    var curBackgroundColor = 0
    var curPrimaryColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        initColorVariables()
        setupColorsPickers()
        updateTextColors(customization_holder)

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customization, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.undo -> {
                confirmUndoChanges()
                true
            }
            R.id.save -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveChanges() {
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
        }
        finish()
    }

    private fun confirmUndoChanges() {
        ConfirmationDialog(this, "", R.string.undo_changes_confirmation, R.string.yes, R.string.no) {
            resetColors()
        }
    }

    private fun resetColors() {
        initColorVariables()
        setupColorsPickers()
        updateTextColors(customization_holder)
        updateBackgroundColor()
        updateActionbarColor()
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

    private fun pickTextColor() {
        AmbilWarnaDialog(this, curTextColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                curTextColor = color
                setupColorsPickers()
                updateTextColors(customization_holder, color)
            }
        }).show()
    }

    private fun pickBackgroundColor() {
        AmbilWarnaDialog(this, curBackgroundColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                curBackgroundColor = color
                setupColorsPickers()
                updateBackgroundColor(color)
            }
        }).show()
    }

    private fun pickPrimaryColor() {
        AmbilWarnaDialog(this, curPrimaryColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                curPrimaryColor = color
                setupColorsPickers()
                updateActionbarColor(color)
            }
        }).show()
    }
}
