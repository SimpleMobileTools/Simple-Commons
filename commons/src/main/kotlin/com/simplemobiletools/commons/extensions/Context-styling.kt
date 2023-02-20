package com.simplemobiletools.commons.extensions

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.view.ViewGroup
import androidx.loader.content.CursorLoader
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.*

// handle system default theme (Material You) specially as the color is taken from the system, not hardcoded by us
fun Context.getProperTextColor() = if (this.baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_neutral_text_color, theme)
} else {
    this.baseConfig.textColor
}

fun Context.getProperBackgroundColor() = if (this.baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_background_color, theme)
} else {
    this.baseConfig.backgroundColor
}

fun Context.getProperPrimaryColor() = when {
    this.baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_primary_color, theme)
    isWhiteTheme() || isBlackAndWhiteTheme() -> this.baseConfig.accentColor
    else -> this.baseConfig.primaryColor
}

fun Context.getProperStatusBarColor() = when {
    this.baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
    else -> getProperBackgroundColor()
}

// get the color of the statusbar with material activity, if the layout is scrolled down a bit
fun Context.getColoredMaterialStatusBarColor(): Int {
    return if (this.baseConfig.isUsingSystemTheme) {
        resources.getColor(R.color.you_status_bar_color, theme)
    } else {
        getProperPrimaryColor()
    }
}

fun Context.updateTextColors(viewGroup: ViewGroup) {
    val textColor = when {
        this.baseConfig.isUsingSystemTheme -> getProperTextColor()
        else -> this.baseConfig.textColor
    }

    val backgroundColor = this.baseConfig.backgroundColor
    val accentColor = when {
        isWhiteTheme() || isBlackAndWhiteTheme() -> this.baseConfig.accentColor
        else -> getProperPrimaryColor()
    }

    val cnt = viewGroup.childCount
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        when (it) {
            is MyTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)
            is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
            is MyEditText -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAutoCompleteTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyFloatingActionButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
            is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyTextInputLayout -> it.setColors(textColor, accentColor, backgroundColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}

fun Context.isBlackAndWhiteTheme() = this.baseConfig.textColor == Color.WHITE && this.baseConfig.primaryColor == Color.BLACK && this.baseConfig.backgroundColor == Color.BLACK

fun Context.isWhiteTheme() = this.baseConfig.textColor == DARK_GREY && this.baseConfig.primaryColor == Color.WHITE && this.baseConfig.backgroundColor == Color.WHITE

fun Context.isUsingSystemDarkTheme() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

fun Context.getTimePickerDialogTheme() = when {
    this.baseConfig.isUsingSystemTheme -> if (isUsingSystemDarkTheme()) {
        R.style.MyTimePickerMaterialTheme_Dark
    } else {
        R.style.MyDateTimePickerMaterialTheme
    }
    this.baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> R.style.MyDialogTheme_Dark
    else -> R.style.MyDialogTheme
}

fun Context.getDatePickerDialogTheme() = when {
    this.baseConfig.isUsingSystemTheme -> R.style.MyDateTimePickerMaterialTheme
    this.baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> R.style.MyDialogTheme_Dark
    else -> R.style.MyDialogTheme
}

fun Context.getPopupMenuTheme(): Int {
    return if (isSPlus() && this.baseConfig.isUsingSystemTheme) {
        R.style.AppTheme_YouPopupMenuStyle
    } else if (isWhiteTheme()) {
        R.style.AppTheme_PopupMenuLightStyle
    } else {
        R.style.AppTheme_PopupMenuDarkStyle
    }
}

fun Context.getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
    if (!isThankYouInstalled()) {
        callback(null)
    } else {
        val cursorLoader = getMyContentProviderCursorLoader()
        ensureBackgroundThread {
            callback(getSharedThemeSync(cursorLoader))
        }
    }
}

fun Context.getSharedThemeSync(cursorLoader: CursorLoader): SharedTheme? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                val textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR)
                val backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR)
                val primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR)
                val accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR)
                val appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR)
                val lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS)
                return SharedTheme(textColor, backgroundColor, primaryColor, appIconColor, lastUpdatedTS, accentColor)
            } catch (e: Exception) {
            }
        }
    }
    return null
}

fun Context.checkAppIconColor() {
    val appId = this.baseConfig.appId
    if (appId.isNotEmpty() && this.baseConfig.lastIconColor != this.baseConfig.appIconColor) {
        getAppIconColors().forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        getAppIconColors().forEachIndexed { index, color ->
            if (this.baseConfig.appIconColor == color) {
                toggleAppIconColor(appId, index, color, true)
            }
        }
    }
}

fun Context.toggleAppIconColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
    val className = "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(ComponentName(appId, className), state, PackageManager.DONT_KILL_APP)
        if (enable) {
            this.baseConfig.lastIconColor = color
        }
    } catch (e: Exception) {
    }
}

fun Context.getAppIconColors() = resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())

@SuppressLint("NewApi")
fun Context.getBottomNavigationBackgroundColor(): Int {
    val baseColor = this.baseConfig.backgroundColor
    val bottomColor = when {
        this.baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
        baseColor == Color.WHITE -> resources.getColor(R.color.bottom_tabs_light_background)
        else -> this.baseConfig.backgroundColor.lightenColor(4)
    }
    return bottomColor
}
