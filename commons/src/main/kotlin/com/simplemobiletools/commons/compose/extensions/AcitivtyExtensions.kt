package com.simplemobiletools.commons.compose.extensions

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isOreoMr1Plus
import com.simplemobiletools.commons.models.Release

fun ComponentActivity.appLaunchedCompose(
    appId: String,
    showUpgradeDialog: () -> Unit,
    showDonateDialog: () -> Unit,
    showRateUsDialog: () -> Unit
) {
    baseConfig.internalStoragePath = getInternalStoragePath()
    updateSDCardPath()
    baseConfig.appId = appId
    if (baseConfig.appRunCount == 0) {
        baseConfig.wasOrangeIconChecked = true
        checkAppIconColor()
    } else if (!baseConfig.wasOrangeIconChecked) {
        baseConfig.wasOrangeIconChecked = true
        val primaryColor = ContextCompat.getColor(this, R.color.color_primary)
        if (baseConfig.appIconColor != primaryColor) {
            getAppIconColors().forEachIndexed { index, color ->
                toggleAppIconColor(appId, index, color, false)
            }

            val defaultClassName = "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity"
            packageManager.setComponentEnabledSetting(
                ComponentName(baseConfig.appId, defaultClassName),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
            )

            val orangeClassName = "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity.Orange"
            packageManager.setComponentEnabledSetting(
                ComponentName(baseConfig.appId, orangeClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            baseConfig.appIconColor = primaryColor
            baseConfig.lastIconColor = primaryColor
        }
    }

    baseConfig.appRunCount++
    if (baseConfig.appRunCount % 30 == 0 && !isAProApp()) {
        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            if (getCanAppBeUpgraded()) {
                showUpgradeDialog()
            } else if (!isOrWasThankYouInstalled()) {
                showDonateDialog()
            }
        }
    }

    if (baseConfig.appRunCount % 40 == 0 && !baseConfig.wasAppRated) {
        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            showRateUsDialog()
        }
    }
}

fun ComponentActivity.checkWhatsNewCompose(releases: List<Release>, currVersion: Int, showWhatsNewDialog: (List<Release>) -> Unit) {
    if (baseConfig.lastVersion == 0) {
        baseConfig.lastVersion = currVersion
        return
    }

    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty()) {
        showWhatsNewDialog(newReleases)
    }

    baseConfig.lastVersion = currVersion
}

fun ComponentActivity.upgradeToPro() {
    launchViewIntent("https://simplemobiletools.com/upgrade_to_pro")
}

const val DEVELOPER_PLAY_STORE_URL = "https://play.google.com/store/apps/dev?id=9070296388022589266"
const val FAKE_VERSION_APP_LABEL =
    "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"

fun Context.fakeVersionCheck(
    showConfirmationDialog: () -> Unit
) {
    if (!packageName.startsWith("com.simplemobiletools.", true)) {
        if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
            showConfirmationDialog()
        }
    }
}

fun ComponentActivity.appOnSdCardCheckCompose(
    showConfirmationDialog: () -> Unit
) {
    if (!baseConfig.wasAppOnSDShown && isAppInstalledOnSDCard()) {
        baseConfig.wasAppOnSDShown = true
        showConfirmationDialog()
    }
}

fun Activity.setShowWhenLockedCompat(showWhenLocked: Boolean) {
    if (isOreoMr1Plus()) {
        setShowWhenLocked(showWhenLocked)
    } else {
        val flagsToUpdate =
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        if (showWhenLocked) {
            window.addFlags(flagsToUpdate)
        } else {
            window.clearFlags(flagsToUpdate)
        }
    }
}

fun Activity.setTurnScreenOnCompat(turnScreenOn: Boolean) {
    if (isOreoMr1Plus()) {
        setTurnScreenOn(turnScreenOn)
    } else {
        val flagToUpdate = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        if (turnScreenOn) {
            window.addFlags(flagToUpdate)
        } else {
            window.clearFlags(flagToUpdate)
        }
    }
}

fun Activity.setFullscreenCompat(fullScreen: Boolean) {
    if (isOreoMr1Plus()) {
        WindowCompat.getInsetsController(window, window.decorView.rootView).hide(WindowInsetsCompat.Type.statusBars())
    } else {
        val flagToUpdate = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (fullScreen) {
            window.addFlags(flagToUpdate)
        } else {
            window.clearFlags(flagToUpdate)
        }
    }
}
