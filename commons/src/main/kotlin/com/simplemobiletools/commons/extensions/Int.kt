package com.simplemobiletools.commons.extensions

import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import java.util.*

fun Int.getContrastColor(): Int {
    val darkGrey = -13421773
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 149) darkGrey else Color.WHITE
}

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this).toUpperCase()

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.getFormattedDuration(): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

fun Int.addBitIf(add: Boolean, bit: Int) =
        if (add) {
            addBit(bit)
        } else {
            removeBit(bit)
        }

// TODO: how to do "bits & ~bit" in kotlin?
fun Int.removeBit(bit: Int) = addBit(bit) - bit

fun Int.addBit(bit: Int) = this or bit

fun Int.flipBit(bit: Int) = if (this and bit == 0) addBit(bit) else removeBit(bit)

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start

// taken from https://stackoverflow.com/a/40964456/1967672
fun Int.darkenColor(): Int {
    if (this == Color.WHITE) {
        return -2105377
    } else if (this == Color.BLACK) {
        return Color.BLACK
    }

    val darkFactor = 8
    var hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    val hsl = hsv2hsl(hsv)
    hsl[2] -= darkFactor / 100f
    if (hsl[2] < 0)
        hsl[2] = 0f
    hsv = hsl2hsv(hsl)
    return Color.HSVToColor(hsv)
}

private fun hsl2hsv(hsl: FloatArray): FloatArray {
    val hue = hsl[0]
    var sat = hsl[1]
    val light = hsl[2]
    sat *= if (light < .5) light else 1 - light
    return floatArrayOf(hue, 2f * sat / (light + sat), light + sat)
}

private fun hsv2hsl(hsv: FloatArray): FloatArray {
    val hue = hsv[0]
    val sat = hsv[1]
    val value = hsv[2]

    val newHue = (2f - sat) * value
    var newSat = sat * value / if (newHue < 1f) newHue else 2f - newHue
    if (newSat > 1f)
        newSat = 1f

    return floatArrayOf(hue, newSat, newHue / 2f)
}

fun Int.orientationFromDegrees() = when (this) {
    270 -> ExifInterface.ORIENTATION_ROTATE_270
    180 -> ExifInterface.ORIENTATION_ROTATE_180
    90 -> ExifInterface.ORIENTATION_ROTATE_90
    else -> ExifInterface.ORIENTATION_NORMAL
}.toString()

fun Int.degreesFromOrientation() = when (this) {
    ExifInterface.ORIENTATION_ROTATE_270 -> 270
    ExifInterface.ORIENTATION_ROTATE_180 -> 180
    ExifInterface.ORIENTATION_ROTATE_90 -> 90
    else -> 0
}
