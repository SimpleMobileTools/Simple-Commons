package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import com.booking.rtlviewpager.RtlViewPager

class MyViewPager : RtlViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (ignored: Exception) {
        }

        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (ignored: Exception) {
        }

        return false
    }
}
