package com.simplemobiletools.commons.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyArrayAdapter<T>(context: Context, res: Int, items: Array<T>, val color: Int, val padding: Int) : ArrayAdapter<T>(context, res, items) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)

        (view.findViewById(android.R.id.text1) as TextView).apply {
            setTextColor(color)
            setPadding(padding, padding, padding, padding)
        }

        return view
    }
}
