package com.simplemobiletools.commons.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.formatSize
import com.simplemobiletools.commons.extensions.isGif
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.android.synthetic.main.filepicker_list_item.view.*
import java.io.File

class FilepickerItemsAdapter(val context: Context, private val mItems: List<FileDirItem>, val itemClick: (FileDirItem) -> Unit) :
        RecyclerView.Adapter<FilepickerItemsAdapter.ViewHolder>() {
    var textColor = 0

    init {
        textColor = BaseConfig.newInstance(context).textColor
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.filepicker_list_item, parent, false)
        return ViewHolder(context, textColor, view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(mItems[position])
    }

    override fun getItemCount() = mItems.size

    class ViewHolder(val context: Context, val textColor: Int, view: View, val itemClick: (FileDirItem) -> (Unit)) : RecyclerView.ViewHolder(view) {
        fun bindView(fileDirItem: FileDirItem) {
            itemView.list_item_name.text = fileDirItem.name
            itemView.list_item_name.setTextColor(textColor)

            if (fileDirItem.isDirectory) {
                Glide.with(context).load(R.drawable.ic_directory).diskCacheStrategy(getCacheStrategy(fileDirItem)).centerCrop().crossFade().into(itemView.list_item_icon)
                itemView.list_item_details.text = getChildrenCnt(fileDirItem)
            } else {
                Glide.with(context).load(fileDirItem.path).diskCacheStrategy(getCacheStrategy(fileDirItem)).error(R.drawable.ic_file).centerCrop().crossFade().into(itemView.list_item_icon)
                itemView.list_item_details.text = fileDirItem.size.formatSize()
            }

            itemView.list_item_details.setTextColor(textColor)
            itemView.setOnClickListener { itemClick(fileDirItem) }
        }

        private fun getCacheStrategy(item: FileDirItem) = if (File(item.path).isGif()) DiskCacheStrategy.NONE else DiskCacheStrategy.RESULT

        private fun getChildrenCnt(item: FileDirItem): String {
            val children = item.children
            return context.resources.getQuantityString(R.plurals.items, children, children)
        }
    }
}
