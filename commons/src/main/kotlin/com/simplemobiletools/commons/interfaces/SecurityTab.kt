package com.simplemobiletools.commons.interfaces

interface SecurityTab {
    fun initTab(requiredHash: String, listener: HashListener)
}
