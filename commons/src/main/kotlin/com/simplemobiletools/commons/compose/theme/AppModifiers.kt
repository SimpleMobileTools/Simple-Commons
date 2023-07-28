package com.simplemobiletools.commons.compose.theme

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.getActivity
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.helpers.BaseConfig

fun Activity.getAppIconIds(): ArrayList<Int> = ArrayList(intent.getIntegerArrayListExtra(APP_ICON_IDS).orEmpty())
fun Activity.getAppLauncherName(): String = intent.getStringExtra(APP_LAUNCHER_NAME).orEmpty()
internal fun updateRecentsAppIcon(baseConfig: BaseConfig, context: Context) {
    if (baseConfig.isUsingModifiedAppIcon) {
        val appIconIDs = context.getAppIconIds()
        val currentAppIconColorIndex = baseConfig.getCurrentAppIconColorIndex(context)
        if (appIconIDs.size - 1 < currentAppIconColorIndex) {
            return
        }

        val recentsIcon = BitmapFactory.decodeResource(context.resources, appIconIDs[currentAppIconColorIndex])
        val title = context.getAppLauncherName()
        val color = baseConfig.primaryColor

        val description = ActivityManager.TaskDescription(title, recentsIcon, color)
        context.getActivity().setTaskDescription(description)
    }
}

private fun BaseConfig.getCurrentAppIconColorIndex(context: Context): Int {
    val appIconColor = appIconColor
    context.getAppIconColors().forEachIndexed { index, color ->
        if (color == appIconColor) {
            return index
        }
    }
    return 0
}

private fun Context.getAppIconColors() = resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())

private fun Context.getAppIconIds(): List<Int> = getActivity().getAppIconIds()

private fun Context.getAppLauncherName(): String = getActivity().getAppLauncherName()

