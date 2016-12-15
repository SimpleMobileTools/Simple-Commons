package com.simplemobiletools.commons.samples.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.activities.CustomizationActivity
import com.simplemobiletools.commons.samples.R

class MainActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, CustomizationActivity::class.java))
        finish()
    }
}
