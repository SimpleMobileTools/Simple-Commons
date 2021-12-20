package com.simplemobiletools.commons.views.contextview

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

data class ContextViewItem(
    @IdRes val id: Int,
    val title: String,
    @DrawableRes val icon: Int = View.NO_ID,
    val showAsAction: Boolean,
    val isVisible: Boolean = true,
)
