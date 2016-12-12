package com.simplemobiletools.commons.helpers

import android.content.Context
import android.content.SharedPreferences
import com.simplemobiletools.commons.extensions.getSharedPrefs

class Config(context: Context) {
    private val mPrefs: SharedPreferences

    companion object {
        fun newInstance(context: Context) = Config(context)
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
}
