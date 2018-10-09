package com.simplemobiletools.commons.interfaces

interface MyAdapterListener {
    fun itemLongClicked(position: Int)

    fun getItemKey(position: Int): String
}
