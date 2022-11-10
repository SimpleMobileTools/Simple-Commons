package com.simplemobiletools.commons.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.setImageResourceOrBeGone
import com.simplemobiletools.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.item_simple_list.view.*

open class SimpleListItemAdapter(val activity: Activity, val onItemClicked: (SimpleListItem) -> Unit) :
    RecyclerView.Adapter<SimpleListItemAdapter.SimpleItemViewHolder>() {

    private val listItems = arrayListOf<SimpleListItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(items: Array<SimpleListItem>) {
        listItems.clear()
        listItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleItemViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.item_simple_list, parent, false)
        return SimpleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleItemViewHolder, position: Int) {
        val route = listItems[position]
        holder.bindView(route)
    }

    override fun getItemCount() = listItems.size

    open inner class SimpleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(item: SimpleListItem) {
            itemView.apply {
                val textColor = context.getProperTextColor()
                bottom_sheet_item_title.setText(item.textRes)
                bottom_sheet_item_title.setTextColor(textColor)

                bottom_sheet_item_icon.setImageResourceOrBeGone(item.imageRes)
                bottom_sheet_item_icon.applyColorFilter(textColor)

                setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
    }
}
