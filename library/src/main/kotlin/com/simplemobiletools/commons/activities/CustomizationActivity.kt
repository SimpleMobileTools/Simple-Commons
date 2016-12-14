package com.simplemobiletools.commons.activities

import android.os.Bundle
import com.simplemobiletools.commons.R
import kotlinx.android.synthetic.main.activity_customization.*
import yuku.ambilwarna.AmbilWarnaDialog

class CustomizationActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        setupColors()

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickbackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
    }

    private fun setupColors() {
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
                setupColors()
            }
        }).show()
    }

    private fun pickbackgroundColor() {
        AmbilWarnaDialog(this, baseConfig.backgroundColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                baseConfig.backgroundColor = color
                setupColors()
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
                setupColors()
            }
        }).show()
    }
}
