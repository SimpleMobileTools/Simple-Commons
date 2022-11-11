package com.simplemobiletools.commons.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.item_simple_list.view.*

open class SimpleListItemAdapter(val activity: Activity, val onItemClicked: (SimpleListItem) -> Unit) :
    ListAdapter<SimpleListItem, SimpleListItemAdapter.SimpleItemViewHolder>(SimpleListItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.item_simple_list, parent, false)
        return SimpleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        val route = getItem(position)
        holder.bindView(route)
    }

    open inner class SimpleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(item: SimpleListItem) {
            itemView.apply {
                val color = if (item.selected) {
                    val primaryColor = context.getProperPrimaryColor()
                    bottom_sheet_selected_icon.beVisible()
                    bottom_sheet_selected_icon.applyColorFilter(primaryColor)
                    primaryColor
                } else {
                    context.getProperTextColor()
                }

                bottom_sheet_item_title.setText(item.textRes)
                bottom_sheet_item_title.setTextColor(color)
                bottom_sheet_item_icon.setImageResourceOrBeGone(item.imageRes)
                bottom_sheet_item_icon.applyColorFilter(color)

                setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
    }
}

private class SimpleListItemDiffCallback : DiffUtil.ItemCallback<SimpleListItem>() {

    override fun areItemsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
        return SimpleListItem.areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: SimpleListItem, newItem: SimpleListItem): Boolean {
        return SimpleListItem.areContentsTheSame(oldItem, newItem)
    }

}
