package com.simplemobiletools.commons.helpers

import android.content.Context
import android.content.SharedPreferences
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getSharedPrefs

open class BaseConfig(val context: Context) {
    protected val prefs: SharedPreferences = context.getSharedPrefs()

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }

    var isFirstRun: Boolean
        get() = prefs.getBoolean(IS_FIRST_RUN, true)
        set(firstRun) = prefs.edit().putBoolean(IS_FIRST_RUN, firstRun).apply()

    var lastVersion: Int
        get() = prefs.getInt(LAST_VERSION, 0)
        set(lastVersion) = prefs.edit().putInt(LAST_VERSION, lastVersion).apply()

    var treeUri: String
        get() = prefs.getString(TREE_URI, "")
        set(uri) = prefs.edit().putString(TREE_URI, uri).apply()

    var sdCardPath: String
        get() = prefs.getString(SD_CARD_PATH, "")
        set(sdCardPath) = prefs.edit().putString(SD_CARD_PATH, sdCardPath).apply()

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, context.resources.getColor(R.color.default_text_color))
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, context.resources.getColor(R.color.default_background_color))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, context.resources.getColor(R.color.color_primary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    var widgetBgColor: Int
        get() = prefs.getInt(WIDGET_BG_COLOR, 1)
        set(widgetBgColor) = prefs.edit().putInt(WIDGET_BG_COLOR, widgetBgColor).apply()

    var widgetTextColor: Int
        get() = prefs.getInt(WIDGET_TEXT_COLOR, context.resources.getColor(R.color.color_primary))
        set(widgetTextColor) = prefs.edit().putInt(WIDGET_TEXT_COLOR, widgetTextColor).apply()
}
