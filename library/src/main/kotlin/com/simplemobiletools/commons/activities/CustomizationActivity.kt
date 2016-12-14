package com.simplemobiletools.commons.activities

import android.os.Bundle
import com.simplemobiletools.commons.R
import kotlinx.android.synthetic.main.activity_customization.*

class CustomizationActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        customization_text_color_holder.setOnClickListener { }
        customization_background_color_holder.setOnClickListener { }
        customization_primary_color_holder.setOnClickListener { }
    }
}
