package com.simplemobiletools.commons.helpers

import android.content.Context
import android.content.SharedPreferences
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getSharedPrefs

open class BaseConfig(val context: Context) {
    private val mPrefs: SharedPreferences

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }

    init {
        mPrefs = context.getSharedPrefs()
    }

    var isFirstRun: Boolean
        get() = mPrefs.getBoolean(IS_FIRST_RUN, true)
        set(firstRun) = mPrefs.edit().putBoolean(IS_FIRST_RUN, firstRun).apply()

    var isDarkTheme: Boolean
        get() = mPrefs.getBoolean(IS_DARK_THEME, false)
        set(isDarkTheme) = mPrefs.edit().putBoolean(IS_DARK_THEME, isDarkTheme).apply()

    var lastVersion: Int
        get() = mPrefs.getInt(LAST_VERSION, 0)
        set(lastVersion) = mPrefs.edit().putInt(LAST_VERSION, lastVersion).apply()

    var treeUri: String
        get() = mPrefs.getString(TREE_URI, "")
        set(uri) = mPrefs.edit().putString(TREE_URI, uri).apply()

    var textColor: Int
        get() = mPrefs.getInt(TEXT_COLOR, 0xFF333333.toInt())
        set(textColor) = mPrefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = mPrefs.getInt(BACKGROUND_COLOR, 0xFFEEEEEE.toInt())
        set(backgroundColor) = mPrefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = mPrefs.getInt(PRIMARY_COLOR, context.getColor(R.color.color_primary))
        set(primaryColor) = mPrefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()
}
