package com.simplemobiletools.commons.samples.activities

import android.os.Bundle
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.storeStoragePaths
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.samples.R

class MainActivity : BaseSimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        storeStoragePaths()

        startAboutActivity(R.string.app_version, LICENSE_KOTLIN or LICENSE_GLIDE or LICENSE_CROPPER or LICENSE_MULTISELECT or LICENSE_RTL
                or LICENSE_PHOTOVIEW or LICENSE_SUBSAMPLING, "2.10.5")
    }
}
