package com.simplemobiletools.commons.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.PREFS_KEY

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, id, length).show()

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, msg, length).show()

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

fun Context.getDialogBackgroundColor(backgroundColor: Int): Drawable {
    return ColorDrawable(if (backgroundColor.getContrastColor() == Color.WHITE) {
        getColor(R.color.dark_dialog_background)
    } else {
        backgroundColor
    })
}
