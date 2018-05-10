package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getSharedTheme
import com.simplemobiletools.commons.extensions.isThankYouInstalled

abstract class BaseSplashActivity : AppCompatActivity() {

    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((baseConfig.appRunCount == 0 || !baseConfig.wasSharedThemeAfterUpdateChecked) && isThankYouInstalled()) {
            baseConfig.wasSharedThemeAfterUpdateChecked = true
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = true
                        isUsingSharedTheme = true
                        wasSharedThemeEverActivated = true

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        appIconColor = it.appIconColor
                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }
}
