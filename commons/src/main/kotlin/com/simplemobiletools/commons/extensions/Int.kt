package com.simplemobiletools.commons.extensions

import android.graphics.Color
import java.util.*

fun Int.getContrastColor(): Int {
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 128) Color.BLACK else Color.WHITE
}

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.getFormattedDuration(): String {
    val sb = StringBuilder(8)
    val hours = this / (60 * 60)
    val minutes = this % (60 * 60) / 60
    val seconds = this % (60 * 60) % 60

    if (this > 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

// TODO: how to do "flags & ~flag" in kotlin?
fun Int.removeFlag(flag: Int) = (this or flag) - flag
