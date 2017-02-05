package com.simplemobiletools.commons.extensions

import android.view.View

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else beVisible()

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.beGoneIf(beGone: Boolean) = if (beGone) beGone() else beVisible()

fun View.beInvisible() = { visibility = View.INVISIBLE }

fun View.beVisible() = { visibility = View.VISIBLE }

fun View.beGone() = { visibility = View.GONE }
