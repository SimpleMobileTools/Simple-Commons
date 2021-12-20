package com.simplemobiletools.commons.views.contextview

interface ContextViewCallback {
    fun onItemClicked(item: ContextViewItem){}
    fun onCreateContextView(view: ContextView){}
    fun onDestroyContextView(){}
}
