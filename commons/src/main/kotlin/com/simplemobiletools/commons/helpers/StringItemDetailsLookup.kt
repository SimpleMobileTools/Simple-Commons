package com.simplemobiletools.commons.helpers

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter

class StringItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            val holder = recyclerView.getChildViewHolder(view)
            if (holder is MyRecyclerViewAdapter.ViewHolder) {
                return holder.getItemDetails()
            }
        }
        return null
    }
}
