package com.simplemobiletools.commons.samples.activities

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.interfaces.ItemMoveCallback
import com.simplemobiletools.commons.interfaces.ItemTouchHelperContract
import com.simplemobiletools.commons.interfaces.StartReorderDragListener
import com.simplemobiletools.commons.samples.databinding.ListItemBinding
import com.simplemobiletools.commons.views.MyRecyclerView
import java.util.*

class StringsAdapter(
    activity: BaseSimpleActivity, var strings: MutableList<String>, recyclerView: MyRecyclerView, val swipeRefreshLayout: SwipeRefreshLayout,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick), ItemTouchHelperContract {

    private var isChangingOrder = false
    private var startReorderDragListener: StartReorderDragListener

    init {
        setupDragListener(true)

        val touchHelper = ItemTouchHelper(ItemMoveCallback(this, true))
        touchHelper.attachToRecyclerView(recyclerView)

        startReorderDragListener = object : StartReorderDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        }
    }

    override fun getActionMenuId() = com.simplemobiletools.commons.R.menu.cab_delete_only

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            com.simplemobiletools.commons.R.id.cab_delete -> changeOrder()
        }
    }

    override fun getSelectableItemCount() = strings.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = strings.getOrNull(position)?.hashCode()

    override fun getItemKeyPosition(key: Int) = strings.indexOfFirst { it.hashCode() == key }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).run { ViewHolder(root) }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {
        if (isChangingOrder) {
            notifyDataSetChanged()
        }

        isChangingOrder = false
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val item = strings[position]
        holder.bindView(item, true, true) { itemView, layoutPosition ->
            setupView(ListItemBinding.bind(itemView), item, holder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = strings.size

    private fun changeOrder() {
        isChangingOrder = true
        notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView(view: ListItemBinding, string: String, holder: ViewHolder) {
        val isSelected = selectedKeys.contains(string.hashCode())
        view.apply {
            itemFrame.isSelected = isSelected
            itemName.text = string

            dragHandle.beVisibleIf(isChangingOrder)

            if (isChangingOrder) {
                dragHandle.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startReorderDragListener.requestDrag(holder)
                    }
                    false
                }
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(strings, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(strings, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
        swipeRefreshLayout.isEnabled = false
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
        swipeRefreshLayout.isEnabled = true
    }
}
