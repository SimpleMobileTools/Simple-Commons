package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.simplemobiletools.commons.interfaces.RenameTab

class RenameSimpleTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), RenameTab {
    var paths = ArrayList<String>()

    override fun initTab(paths: ArrayList<String>) {
        this.paths = paths
    }

    override fun dialogConfirmed() {
    }
}
