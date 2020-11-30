package com.simplemobiletools.commons.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView

fun ImageView.setFillWithStroke(fillColor: Int, backgroundColor: Int, cornerRadiusSize: Float = 0f) {
    val strokeColor = backgroundColor.getContrastColor()
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fillColor)
        setStroke(2, strokeColor)
        setBackgroundDrawable(this)

        if (cornerRadiusSize != 0f) {
            cornerRadius = cornerRadiusSize
        }
    }
}

fun ImageView.applyColorFilter(color: Int) = setColorFilter(color, PorterDuff.Mode.SRC_IN)
