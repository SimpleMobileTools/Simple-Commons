package com.simplemobiletools.commons.extensions

import android.view.View

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) visibility = View.VISIBLE else visibility = View.GONE

fun View.beGoneIf(beGone: Boolean) = if (beGone) beGone() else beVisible()

fun View.beVisible() = { visibility = View.VISIBLE }

fun View.beGone() = { visibility = View.GONE }
