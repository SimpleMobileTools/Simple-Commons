package com.simplemobiletools.commons.views.bottomactionmenu

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.PopupWindow
import androidx.annotation.MenuRes
import androidx.core.widget.PopupWindowCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.simplemobiletools.commons.activities.BaseSimpleActivity

class BottomActionMenuPopup(private val activity: BaseSimpleActivity, items: List<BottomActionMenuItem>) {
    private val bottomActionMenuView = BottomActionMenuView(activity)
    private val popup = PopupWindow(activity, null, android.R.attr.popupMenuStyle)
    private var floatingActionButton: FloatingActionButton? = null
    private var callback: BottomActionMenuCallback? = null

    constructor(activity: BaseSimpleActivity, @MenuRes menuResId: Int) : this(activity, BottomActionMenuParser(activity).inflate(menuResId))

    init {
        popup.contentView = bottomActionMenuView
        popup.width = ViewGroup.LayoutParams.MATCH_PARENT
        popup.height = ViewGroup.LayoutParams.WRAP_CONTENT
        popup.isOutsideTouchable = false
        popup.setOnDismissListener {
            callback?.onViewDestroyed()
            floatingActionButton?.show()
        }
        PopupWindowCompat.setWindowLayoutType(popup, WindowManager.LayoutParams.TYPE_APPLICATION)
        bottomActionMenuView.setup(items)
    }

    fun show(callback: BottomActionMenuCallback?, hideFab: Boolean = true) {
        this.callback = callback
        callback?.onViewCreated(bottomActionMenuView)
        if (hideFab) {
            floatingActionButton?.hide() ?: findFABAndHide()
        }
        bottomActionMenuView.setCallback(callback)
        bottomActionMenuView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        popup.showAtLocation(bottomActionMenuView, Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        bottomActionMenuView.show()
    }

    fun dismiss() {
        popup.dismiss()
    }

    private fun findFABAndHide() {
        val parent = activity.findViewById<ViewGroup>(android.R.id.content)
        findFab(parent)
        floatingActionButton?.hide()
    }

    private fun findFab(parent: ViewGroup) {
        val count = parent.childCount
        for (i in 0 until count) {
            val child = parent.getChildAt(i)
            if (child is FloatingActionButton) {
                floatingActionButton = child
                break
            } else if (child is ViewGroup) {
                findFab(child)
            }
        }
    }

    fun invalidate() {
        callback?.onViewCreated(bottomActionMenuView)
    }
}
