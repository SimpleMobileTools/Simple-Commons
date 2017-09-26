package com.simplemobiletools.commons.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.isFingerPrintSensorAvailable
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab

class PasswordTypesAdapter(val context: Context, val requiredHash: String, val hashListener: HashListener) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layoutSelection(position), container, false)
        container.addView(view)
        (view as SecurityTab).initTab(requiredHash, hashListener)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    override fun getCount() = if (context.isFingerPrintSensorAvailable()) 3 else 2

    override fun isViewFromObject(view: View, item: Any) = view == item

    private fun layoutSelection(position: Int): Int = when (position) {
        0 -> R.layout.tab_pattern
        1 -> R.layout.tab_pin
        2 -> R.layout.tab_fingerprint
        else -> throw RuntimeException("Only 3 tabs allowed")
    }
}
