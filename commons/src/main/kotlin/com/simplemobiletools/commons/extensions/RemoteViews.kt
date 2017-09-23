package com.simplemobiletools.commons.extensions

import android.widget.RemoteViews

fun RemoteViews.setBackgroundColor(id: Int, color: Int) {
    setInt(id, "setBackgroundColor", color)
}

fun RemoteViews.setTextSize(id: Int, size: Float) {
    setFloat(id, "setTextSize", size)
}

fun RemoteViews.setText(id: Int, text: String) {
    setTextViewText(id, text)
}
