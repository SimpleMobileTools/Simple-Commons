package com.simplemobiletools.commons.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.dialogs.AppSideloadedDialog
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getSharedTheme
import com.simplemobiletools.commons.extensions.isThankYouInstalled

abstract class BaseSplashActivity : AppCompatActivity() {

    abstract fun initActivity()

    abstract fun getAppPackageName(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = getAppPackageName()
        if (isAppSideloaded(packageName)) {
            AppSideloadedDialog(this, packageName) {
                finish()
            }
            return
        }

        if (isThankYouInstalled() && baseConfig.appRunCount == 0) {
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

    private fun isAppSideloaded(packageName: String): Boolean {
        return if (packageName == "-1" || packageName.endsWith(".debug")) {
            false
        } else {
            try {
                packageManager.getInstallerPackageName(packageName) == null
            } catch (e: Exception) {
                false
            }
        }
    }
}
