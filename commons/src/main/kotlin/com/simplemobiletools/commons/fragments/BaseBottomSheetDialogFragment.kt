package com.simplemobiletools.commons.fragments

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.DialogBottomSheetBinding
import com.simplemobiletools.commons.extensions.*

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DialogBottomSheetBinding.inflate(inflater, container, false)
        val context = requireContext()
        val config = context.baseConfig

        if (requireContext().isBlackAndWhiteTheme()) {
            view.root.background = ResourcesCompat.getDrawable(context.resources, R.drawable.bottom_sheet_bg_black, context.theme)
        } else if (!config.isUsingSystemTheme) {
            view.root.background = ResourcesCompat.getDrawable(context.resources, R.drawable.bottom_sheet_bg, context.theme).apply {
                (this as LayerDrawable).findDrawableByLayerId(R.id.bottom_sheet_background).applyColorFilter(context.getProperBackgroundColor())
            }
        }
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getInt(BOTTOM_SHEET_TITLE).takeIf { it != 0 }
        DialogBottomSheetBinding.bind(view).apply {
            bottomSheetTitle.setTextColor(view.context.getProperTextColor())
            bottomSheetTitle.setTextOrBeGone(title)
            setupContentView(bottomSheetContentHolder)
        }
    }

    abstract fun setupContentView(parent: ViewGroup)

    companion object {
        const val BOTTOM_SHEET_TITLE = "title_string"
    }
}
