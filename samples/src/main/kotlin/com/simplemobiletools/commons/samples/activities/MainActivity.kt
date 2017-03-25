package com.simplemobiletools.commons.samples.activities

import android.os.Bundle
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.storeStoragePaths
import com.simplemobiletools.commons.samples.R

class MainActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storeStoragePaths()
        FilePickerDialog(this) {

        }
    }
}
