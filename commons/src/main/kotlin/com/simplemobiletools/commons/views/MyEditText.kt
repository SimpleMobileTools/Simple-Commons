package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.EditText
import com.simplemobiletools.commons.extensions.adjustAlpha

class MyEditText : EditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        background?.mutate()?.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP)

        // requires android:textCursorDrawable="@null" in xml to color the cursor too
        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(0.5f))
        setLinkTextColor(accentColor)
    }
}
