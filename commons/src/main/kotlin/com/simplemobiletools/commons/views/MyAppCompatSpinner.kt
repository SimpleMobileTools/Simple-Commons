package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.PorterDuff
import android.support.v7.widget.AppCompatSpinner
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.adapters.MyArrayAdapter

class MyAppCompatSpinner : AppCompatSpinner {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        val cnt = adapter.count
        val items = kotlin.arrayOfNulls<Any>(cnt)
        for (i in 0..cnt - 1)
            items[i] = adapter.getItem(i)

        val padding = resources.getDimension(R.dimen.activity_margin).toInt()
        adapter = MyArrayAdapter(context, android.R.layout.simple_spinner_item, items, textColor, backgroundColor, padding)

        val superListener = onItemSelectedListener
        onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, arg1: View?, arg2: Int, arg3: Long) {
                (parent.getChildAt(0) as TextView).setTextColor(textColor)
                superListener.onItemSelected(parent, arg1, arg2, arg3)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        background.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP)
    }
}
