package com.simplemobiletools.commons.dialogs

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.adapters.SimpleListItemAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.SimpleListItem
import kotlinx.android.synthetic.main.dialog_bottom_sheet_chooser.*

class SimpleBottomSheetChooserDialog : BottomSheetDialogFragment() {

    var onItemClick: ((SimpleListItem) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_bottom_sheet_chooser, container, false)
        val context = requireContext()
        val config = context.baseConfig
        if (!config.isUsingSystemTheme) {
            val background = ResourcesCompat.getDrawable(context.resources, R.drawable.bottom_sheet_bg, context.theme)
            val backgroundColor = context.getProperBackgroundColor()
            (background as LayerDrawable).findDrawableByLayerId(R.id.bottom_sheet_background).applyColorFilter(backgroundColor)
            view.background = background
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getInt(TITLE).takeIf { it != 0 }
        val subtitle = arguments?.getInt(SUBTITLE).takeIf { it != 0 }
        view.apply {
            bottom_sheet_title.setTextColor(context.getProperTextColor())
            bottom_sheet_title.setTextOrBeGone(title)
            bottom_sheet_subtitle.setTextColor(context.getProperTextColor())
            bottom_sheet_subtitle.setTextOrBeGone(subtitle)
            setupRecyclerView()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setupRecyclerView() {
        val listItems = arguments?.getParcelableArray(DATA) as Array<SimpleListItem>
        getAudioRouteAdapter().submitList(listItems.toList())
    }

    private fun getAudioRouteAdapter(): SimpleListItemAdapter {
        var adapter = bottom_sheet_recycler_view.adapter as? SimpleListItemAdapter
        if (adapter == null) {
            adapter = SimpleListItemAdapter(requireActivity()) {
                onItemClick?.invoke(it)
                dismissAllowingStateLoss()
            }
            bottom_sheet_recycler_view.adapter = adapter
        }
        return adapter
    }

    fun updateChooserItems(newItems: Array<SimpleListItem>) {
        if (isAdded) {
            getAudioRouteAdapter().submitList(newItems.toList())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onItemClick = null
    }

    companion object {
        private const val TAG = "BottomSheetChooser"
        private const val TITLE = "title_string"
        private const val SUBTITLE = "subtitle_string"
        private const val DATA = "data"

        fun createChooser(
            fragmentManager: FragmentManager,
            title: Int?,
            subtitle: Int?,
            data: Array<SimpleListItem>,
            callback: (SimpleListItem) -> Unit
        ): SimpleBottomSheetChooserDialog {
            val extras = Bundle().apply {
                if (title != null) {
                    putInt(TITLE, title)
                }
                if (subtitle != null) {
                    putInt(SUBTITLE, subtitle)
                }

                putParcelableArray(DATA, data)
            }
            return SimpleBottomSheetChooserDialog().apply {
                arguments = extras
                onItemClick = callback
                show(fragmentManager, TAG)
            }
        }
    }
}
