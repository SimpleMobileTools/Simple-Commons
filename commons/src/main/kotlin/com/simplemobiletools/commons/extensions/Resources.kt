package com.simplemobiletools.commons.extensions

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable

fun Resources.getColoredIcon(newColor: Int, resourceId: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inMutable = true
    val bmp = BitmapFactory.decodeResource(this, resourceId, options)
    val paint = Paint()
    val filter = PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN)
    paint.colorFilter = filter
    val canvas = Canvas(bmp)
    canvas.drawBitmap(bmp, 0f, 0f, paint)
    return bmp
}

fun Resources.getColoredDrawable(resId: Int, colorId: Int): Drawable {
    val drawable = getDrawable(resId)
    drawable.mutate().setColorFilter(getColor(colorId), PorterDuff.Mode.SRC_IN)
    return drawable
}

fun Resources.getColoredDrawableWithColor(resId: Int, color: Int): Drawable {
    val drawable = getDrawable(resId)
    drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
    return drawable
}
