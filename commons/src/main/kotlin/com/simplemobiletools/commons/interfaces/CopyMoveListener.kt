package com.simplemobiletools.commons.interfaces

interface CopyMoveListener {
    fun copySucceeded(copyOnly: Boolean, copiedAll: Boolean)

    fun copyFailed()
}
