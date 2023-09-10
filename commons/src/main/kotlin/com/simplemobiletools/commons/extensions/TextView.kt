package com.simplemobiletools.commons.extensions

import android.graphics.Paint
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged

val TextView.value: String get() = text.toString().trim()

fun TextView.underlineText() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.removeUnderlines() {
    val spannable = SpannableString(text)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(object : URLSpan(u.url) {
            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = false
            }
        }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
    }
    text = spannable
}

fun TextView.setTextOrBeGone(@StringRes textRes: Int?) {
    if (textRes != null) {
        beVisible()
        this.text = context.getString(textRes)
    } else {
        beGone()
    }
}

fun TextView.blink(count: Int = 3, duration: Long = 150L): Animation {
    return AlphaAnimation(0.0f, 1.0f).apply {
        this.duration = duration
        startOffset = 20
        repeatMode = Animation.REVERSE
        repeatCount = count
        startAnimation(this)
        doOnTextChanged { _, _, _, _ -> cancel() }
    }
}
