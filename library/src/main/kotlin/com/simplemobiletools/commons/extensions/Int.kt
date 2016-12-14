package com.simplemobiletools.commons.extensions

import android.graphics.Color

fun Int.getContrastColor(): Int {
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 128) Color.BLACK else Color.WHITE
}
