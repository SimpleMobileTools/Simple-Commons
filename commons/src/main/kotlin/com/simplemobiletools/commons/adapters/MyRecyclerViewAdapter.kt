package com.simplemobiletools.commons.adapters

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.helpers.StringItemDetails
import com.simplemobiletools.commons.helpers.StringItemDetailsLookup
import com.simplemobiletools.commons.interfaces.MyActionModeCallback
import com.simplemobiletools.commons.interfaces.MyAdapterListener
import com.simplemobiletools.commons.views.FastScroller
import com.simplemobiletools.commons.views.MyRecyclerView
import java.util.*

abstract class MyRecyclerViewAdapter(val activity: BaseSimpleActivity, val recyclerView: MyRecyclerView, val fastScroller: FastScroller? = null,
                                     val itemClick: (Any) -> Unit) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
    protected val baseConfig = activity.baseConfig
    protected val resources = activity.resources!!
    protected val layoutInflater = activity.layoutInflater
    protected var primaryColor = baseConfig.primaryColor
    protected var textColor = baseConfig.textColor
    protected var backgroundColor = baseConfig.backgroundColor
    protected var mSelectionTracker: SelectionTracker<String>? = null
    protected var actModeCallback: MyActionModeCallback?
    protected val adapterListener: MyAdapterListener
    protected var positionOffset = 0

    private var actMode: ActionMode? = null
    private var actBarTextView: TextView? = null
    private var lastLongPressedItem = -1
    private var isALongPressSelection = false

    abstract fun getActionMenuId(): Int

    abstract fun prepareActionMode(menu: Menu)

    abstract fun actionItemPressed(id: Int)

    abstract fun getSelectableItemCount(): Int

    abstract fun getIsItemSelectable(position: Int): Boolean

    abstract fun getItemSelectionKey(position: Int): String

    abstract fun getItemSelectionKeyProvider(): ItemKeyProvider<String>

    protected fun isOneItemSelected() = mSelectionTracker?.selection?.size() == 1

    init {
        fastScroller?.resetScrollPositions()

        actModeCallback = object : MyActionModeCallback() {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                actionItemPressed(item.itemId)
                return true
            }

            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
                isSelectable = true
                actMode = actionMode
                actBarTextView = layoutInflater.inflate(R.layout.actionbar_title, null) as TextView
                actMode!!.customView = actBarTextView
                actBarTextView!!.setOnClickListener {
                    if (getSelectableItemCount() == mSelectionTracker?.selection?.size()) {
                        finishActMode()
                    } else {
                        selectAll()
                    }
                }
                activity.menuInflater.inflate(getActionMenuId(), menu)
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                prepareActionMode(menu)
                return true
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {
                isSelectable = false
                mSelectionTracker?.clearSelection()
                actBarTextView?.text = ""
                actMode = null
                lastLongPressedItem = -1
                isALongPressSelection = false
            }
        }

        adapterListener = object : MyAdapterListener {
            override fun itemLongClicked(position: Int) {
                isALongPressSelection = true
                lastLongPressedItem = if (lastLongPressedItem == -1) {
                    position
                } else {
                    val min = Math.min(lastLongPressedItem, position)
                    val max = Math.max(lastLongPressedItem, position)
                    for (i in min..max) {
                        if (getIsItemSelectable(i)) {
                            val key = getItemSelectionKey(i)
                            mSelectionTracker?.select(key)
                        }
                        isALongPressSelection = true
                    }
                    position
                }
            }

            override fun getItemKey(position: Int) = getItemSelectionKey(position)
        }
    }

    protected fun getSelectedKeys() = mSelectionTracker!!.selection

    protected fun isKeySelected(key: String) = mSelectionTracker?.isSelected(key) == true

    fun initSelectionTracker() {
        mSelectionTracker = SelectionTracker.Builder<String>(
                "string-items-selection",
                recyclerView,
                getItemSelectionKeyProvider(),
                StringItemDetailsLookup(recyclerView),
                StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

        mSelectionTracker!!.addObserver(object : SelectionTracker.SelectionObserver<String>() {
            override fun onItemStateChanged(key: String, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                val selectionCnt = getSelectedKeys().size()
                if (!isALongPressSelection) {
                    lastLongPressedItem = -1
                }

                isALongPressSelection = false
                if (selectionCnt > 0) {
                    if (actMode == null) {
                        activity.startSupportActionMode(actModeCallback!!)
                    }
                    val selectionText = String.format(activity.getString(R.string.progress), selectionCnt, getSelectableItemCount())
                    actBarTextView?.text = selectionText
                } else {
                    actMode?.finish()
                }
            }
        })
    }

    protected fun selectAll() {
        val cnt = itemCount - positionOffset
        for (i in 0 until cnt) {
            if (getIsItemSelectable(i)) {
                mSelectionTracker?.select(getItemSelectionKey(i))
            }
        }
        lastLongPressedItem = -1
    }

    fun setupZoomListener(zoomListener: MyRecyclerView.MyZoomListener?) {
        recyclerView.setupZoomListener(zoomListener)
    }

    fun addVerticalDividers(add: Boolean) {
        if (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }

        if (add) {
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL).apply {
                setDrawable(resources.getDrawable(R.drawable.divider))
                recyclerView.addItemDecoration(this)
            }
        }
    }

    fun finishActMode() {
        mSelectionTracker?.clearSelection()
    }

    fun updateTextColor(textColor: Int) {
        this.textColor = textColor
        notifyDataSetChanged()
    }

    fun updatePrimaryColor(primaryColor: Int) {
        this.primaryColor = primaryColor
    }

    fun updateBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    protected fun createViewHolder(layoutType: Int, parent: ViewGroup?): ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return ViewHolder(view, adapterListener, activity, actModeCallback!!, positionOffset, itemClick)
    }

    protected fun bindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder) {
        holder.itemView.tag = holder
    }

    protected fun removeSelectedItems(positions: ArrayList<Int>) {
        positions.forEach {
            notifyItemRemoved(it)
        }
        finishActMode()
        fastScroller?.measureRecyclerView()
    }

    open class ViewHolder(view: View, val adapterListener: MyAdapterListener, val activity: BaseSimpleActivity? = null, val actModeCallback: MyActionModeCallback,
                          val positionOffset: Int = 0, val itemClick: ((Any) -> (Unit))? = null) : RecyclerView.ViewHolder(view) {
        fun bindView(any: Any, allowSingleClick: Boolean, allowLongClick: Boolean, callback: (itemView: View, adapterPosition: Int) -> Unit): View {
            return itemView.apply {
                callback(this, adapterPosition)

                if (allowSingleClick) {
                    setOnClickListener { viewClicked(any) }
                    setOnLongClickListener { if (allowLongClick) viewLongClicked() else viewClicked(any); true }
                } else {
                    setOnClickListener(null)
                    setOnLongClickListener(null)
                }
            }
        }

        private fun viewClicked(any: Any) {
            if (!actModeCallback.isSelectable) {
                itemClick?.invoke(any)
            }
        }

        private fun viewLongClicked() {
            adapterListener.itemLongClicked(adapterPosition - positionOffset)
        }

        fun getItemDetails() = StringItemDetails(adapterPosition, adapterListener.getItemKey(adapterPosition))
    }
}
