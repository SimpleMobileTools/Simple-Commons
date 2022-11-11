package com.simplemobiletools.commons.dialogs

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.adapters.SimpleListItemAdapter
import com.simplemobiletools.commons.fragments.BaseBottomSheetDialogFragment
import com.simplemobiletools.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.layout_simple_recycler_view.*

class BottomSheetChooserDialog : BaseBottomSheetDialogFragment() {

    var onItemClick: ((SimpleListItem) -> Unit)? = null

    override fun setupContentView(parent: ViewGroup) {
        val child = layoutInflater.inflate(R.layout.layout_simple_recycler_view, parent, false)
        parent.addView(child)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        @Suppress("UNCHECKED_CAST")
        val listItems = arguments?.getParcelableArray(DATA) as Array<SimpleListItem>
        getRecyclerViewAdapter().submitList(listItems.toList())
    }

    private fun getRecyclerViewAdapter(): SimpleListItemAdapter {
        var adapter = recycler_view.adapter as? SimpleListItemAdapter
        if (adapter == null) {
            adapter = SimpleListItemAdapter(requireActivity()) {
                onItemClick?.invoke(it)
                dismissAllowingStateLoss()
            }
            recycler_view.adapter = adapter
        }
        return adapter
    }

    fun updateChooserItems(newItems: Array<SimpleListItem>) {
        if (isAdded) {
            getRecyclerViewAdapter().submitList(newItems.toList())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onItemClick = null
    }

    companion object {
        private const val TAG = "BottomSheetChooserDialog"
        private const val DATA = "data"

        fun createChooser(
            fragmentManager: FragmentManager,
            title: Int?,
            items: Array<SimpleListItem>,
            callback: (SimpleListItem) -> Unit
        ): BottomSheetChooserDialog {
            val extras = Bundle().apply {
                if (title != null) {
                    putInt(BOTTOM_SHEET_TITLE, title)
                }
                putParcelableArray(DATA, items)
            }
            return BottomSheetChooserDialog().apply {
                arguments = extras
                onItemClick = callback
                show(fragmentManager, TAG)
            }
        }
    }
}
