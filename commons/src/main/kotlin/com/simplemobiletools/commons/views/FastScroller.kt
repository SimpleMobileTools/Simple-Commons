package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.simplemobiletools.commons.extensions.baseConfig

// based on https://blog.stylingandroid.com/recyclerview-fastscroll-part-1
class FastScroller : FrameLayout {
    var isHorizontal = false
    private var handle: View? = null
    private var currHeight = 0
    private var currWidth = 0

    private val HANDLE_HIDE_DELAY = 1000L
    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setViews(recyclerView: RecyclerView, swipeRefreshLayout: SwipeRefreshLayout? = null) {
        this.recyclerView = recyclerView
        this.swipeRefreshLayout = swipeRefreshLayout
        updateHandleColor()

        recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                updateHandlePosition()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showHandle()
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    hideHandle()
                }
            }
        })
    }

    fun updateHandleColor() {
        handle!!.background.setColorFilter(context.baseConfig.primaryColor, PorterDuff.Mode.SRC_IN)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        currHeight = height
        currWidth = width
        updateHandlePosition()
    }

    private fun updateHandlePosition() {
        if (handle!!.isSelected || recyclerView == null)
            return

        if (isHorizontal) {
            val horizontalScrollOffset = recyclerView!!.computeHorizontalScrollOffset()
            val horizontalScrollRange = recyclerView!!.computeHorizontalScrollRange()
            val proportion = horizontalScrollOffset.toFloat() / (horizontalScrollRange.toFloat() - currWidth)
            setPosition(currWidth * proportion)
        } else {
            val verticalScrollOffset = recyclerView!!.computeVerticalScrollOffset()
            val verticalScrollRange = recyclerView!!.computeVerticalScrollRange()
            val proportion = verticalScrollOffset.toFloat() / (verticalScrollRange.toFloat() - currHeight)
            setPosition(currHeight * proportion)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // allow dragging only the handle itself
        if (!handle!!.isSelected) {
            if (isHorizontal) {
                val min = handle!!.x
                val max = min + handle!!.width
                if (event.x < min || event.x > max) {
                    return super.onTouchEvent(event)
                }
            } else {
                val min = handle!!.y
                val max = min + handle!!.height
                if (event.y < min || event.y > max) {
                    return super.onTouchEvent(event)
                }
            }
        }

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                showHandle()
                handle!!.isSelected = true
                swipeRefreshLayout?.isEnabled = false
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontal) {
                    setPosition(event.x)
                    setRecyclerViewPosition(event.x)
                } else {
                    setPosition(event.y)
                    setRecyclerViewPosition(event.y)
                }
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                hideHandle()
                handle!!.isSelected = false
                swipeRefreshLayout?.isEnabled = true
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun setRecyclerViewPosition(pos: Float) {
        if (recyclerView != null) {
            val itemCount = recyclerView!!.adapter.itemCount
            val proportion = if (isHorizontal) {
                pos / currWidth
            } else {
                pos / currHeight
            }

            val targetPos = getValueInRange(0f, (itemCount - 1).toFloat(), proportion * itemCount).toInt()
            (recyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(targetPos, 0)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        handle = getChildAt(0)
    }

    private fun showHandle() {
        handle!!.animate().alpha(1f).start()  // override the fade animation
        handle!!.alpha = 1f
    }

    private fun hideHandle() {
        handle!!.animate().alpha(0f).startDelay = HANDLE_HIDE_DELAY
    }

    private fun setPosition(pos: Float) {
        if (isHorizontal) {
            val position = pos / currWidth
            val handleWidth = handle!!.width
            handle!!.x = getValueInRange(0f, (currWidth - handleWidth).toFloat(), (currWidth - handleWidth) * position)
        } else {
            val position = pos / currHeight
            val handleHeight = handle!!.height
            handle!!.y = getValueInRange(0f, (currHeight - handleHeight).toFloat(), (currHeight - handleHeight) * position)
        }
    }

    private fun getValueInRange(min: Float, max: Float, value: Float) = Math.min(Math.max(min, value), max)
}
