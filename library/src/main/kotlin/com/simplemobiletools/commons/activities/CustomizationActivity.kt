package com.simplemobiletools.commons.activities

import android.os.Bundle
import com.simplemobiletools.commons.R
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

    private fun setupColorsPickers() {
        customization_text_color.setBackgroundColor(baseConfig.textColor)
        customization_background_color.setBackgroundColor(baseConfig.backgroundColor)
        customization_primary_color.setBackgroundColor(baseConfig.primaryColor)
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
