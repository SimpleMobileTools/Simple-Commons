package com.simplemobiletools.commons.activities

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getContrastColor
import kotlinx.android.synthetic.main.activity_customization.*
import yuku.ambilwarna.AmbilWarnaDialog

class CustomizationActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        setupColorsPickers()
        updateTextColors(customization_holder)
        updateActionbarColor()

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
                undoChanges()
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

    }

    private fun undoChanges() {

    }

    private fun setupColorsPickers() {
        customization_text_color.setBackgroundColor(baseConfig.textColor)
        customization_primary_color.setBackgroundColor(baseConfig.primaryColor)
        customView(customization_background_color, baseConfig.backgroundColor, baseConfig.backgroundColor.getContrastColor())
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
        AmbilWarnaDialog(this, baseConfig.textColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                baseConfig.textColor = color
                setupColorsPickers()
                updateTextColors(customization_holder)
            }
        }).show()
    }

    private fun pickBackgroundColor() {
        AmbilWarnaDialog(this, baseConfig.backgroundColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                baseConfig.backgroundColor = color
                setupColorsPickers()
                updateBackgroundColor()
            }
        }).show()
    }

    private fun pickPrimaryColor() {
        AmbilWarnaDialog(this, baseConfig.primaryColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                baseConfig.primaryColor = color
                setupColorsPickers()
                updateActionbarColor()
            }
        }).show()
    }
}
