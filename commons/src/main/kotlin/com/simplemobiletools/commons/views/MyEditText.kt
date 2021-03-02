package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.simplemobiletools.commons.extensions.applyColorFilter

class MyEditText : EditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        background?.mutate()?.applyColorFilter(textColor)

        // requires android:textCursorDrawable="@null" in xml to color the cursor too
        setTextColor(textColor)
        setLinkTextColor(accentColor)
    }
}
