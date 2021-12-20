package com.simplemobiletools.commons.views.bottomactionmenu

interface BottomActionMenuCallback {
    fun onItemClicked(item: BottomActionMenuItem){}
    fun onCreateContextView(view: BottomActionMenuView){}
    fun onDestroyContextView(){}
}
