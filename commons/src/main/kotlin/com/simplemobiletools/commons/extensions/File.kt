package com.simplemobiletools.commons.extensions

import java.util.*

private fun getFormattedDuration(duration: Int): String {
    val sb = StringBuilder(8)
    val hours = duration / (60 * 60)
    val minutes = duration % (60 * 60) / 60
    val seconds = duration % (60 * 60) % 60

    if (duration > 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}
