package com.simplemobiletools.commons.interfaces

import com.simplemobiletools.commons.activities.BaseSimpleActivity

interface RenameTab {
    fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>)

    fun dialogConfirmed(callback: (success: Boolean) -> Unit)
}
