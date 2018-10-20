package com.simplemobiletools.commons.adapters

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.isFingerPrintSensorAvailable
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab
import com.simplemobiletools.commons.views.MyScrollView

class PasswordTypesAdapter(val context: Context, val requiredHash: String, val hashListener: HashListener, val scrollView: MyScrollView) : PagerAdapter() {
    private val tabs = SparseArray<SecurityTab>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layoutSelection(position), container, false)
        container.addView(view)
        tabs.put(position, view as SecurityTab)
        (view as SecurityTab).initTab(requiredHash, hashListener, scrollView)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        tabs.remove(position)
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

    fun isTabVisible(position: Int, isVisible: Boolean) {
        tabs[position]?.visibilityChanged(isVisible)
    }
}
