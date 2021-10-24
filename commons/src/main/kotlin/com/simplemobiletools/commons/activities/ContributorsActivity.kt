package com.simplemobiletools.commons.activities

import android.os.Bundle
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import java.util.*

class ContributorsActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contributors)
    }
}
