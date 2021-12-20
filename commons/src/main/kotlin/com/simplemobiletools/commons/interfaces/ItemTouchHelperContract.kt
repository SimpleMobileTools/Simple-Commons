package com.simplemobiletools.commons.interfaces

import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperContract {
    fun onRowMoved(fromPosition: Int, toPosition: Int)

    fun onRowSelected(myViewHolder: RecyclerView.ViewHolder?)

    fun onRowClear(myViewHolder: RecyclerView.ViewHolder?)
}
