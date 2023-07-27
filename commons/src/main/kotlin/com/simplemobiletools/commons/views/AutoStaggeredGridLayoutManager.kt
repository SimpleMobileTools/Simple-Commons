package com.simplemobiletools.commons.views

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlin.math.max

/**
 * RecyclerView StaggeredGridLayoutManager but with automatic spanCount calculation.
 *
 * @param context The initiating view's context.
 * @param itemSize: Grid item size (width or height, depending on orientation) in pixels. Will be used to calculate span count.
 */
class AutoStaggeredGridLayoutManager(
    private var itemSize: Int,
    orientation: Int,
) : StaggeredGridLayoutManager(1, orientation) {

    init {
        require(itemSize >= 0) {
            "itemSize must be >= 0"
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        val width = width
        val height = height
        if (itemSize > 0 && width > 0 && height > 0) {
            val totalSpace = if (orientation == VERTICAL) {
                width - paddingRight - paddingLeft
            } else {
                height - paddingTop - paddingBottom
            }
            postOnAnimation {
                spanCount = max(1, totalSpace / itemSize)
            }
        }
        super.onLayoutChildren(recycler, state)
    }

    // fixes crash java.lang.IndexOutOfBoundsException: Inconsistency detected...
    // taken from https://stackoverflow.com/a/33985508/1967672
    override fun supportsPredictiveItemAnimations() = false
}
