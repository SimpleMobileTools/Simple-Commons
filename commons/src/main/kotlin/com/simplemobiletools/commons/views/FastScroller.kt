package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.onGlobalLayout

// based on https://blog.stylingandroid.com/recyclerview-fastscroll-part-1
class FastScroller : FrameLayout {
    var isHorizontal = false
    var allowBubbleDisplay = false

    private var handle: View? = null
    private var bubble: TextView? = null
    private var recyclerViewWidth = 0
    private var recyclerViewHeight = 0
    private var currScrollX = 0
    private var currScrollY = 0
    private var handleWidth = 0
    private var handleHeight = 0
    private var bubbleHeight = 0
    private var handleXOffset = 0
    private var handleYOffset = 0
    private var recyclerViewContentWidth = 1
    private var recyclerViewContentHeight = 1
    private var tinyMargin = 0f
    private var fastScrollCallback: ((Int) -> Unit)? = null

    private val HANDLE_HIDE_DELAY = 1000L
    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var bubbleHideHandler = Handler()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setViews(recyclerView: RecyclerView, swipeRefreshLayout: SwipeRefreshLayout? = null, callback: ((Int) -> Unit)? = null) {
        this.recyclerView = recyclerView
        this.swipeRefreshLayout = swipeRefreshLayout
        tinyMargin = context.resources.getDimension(R.dimen.tiny_margin)

        updatePrimaryColor()
        recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!handle!!.isSelected) {
                    bubble?.alpha = 0f
                    bubbleHideHandler.removeCallbacksAndMessages(null)
                }
                currScrollX += dx
                currScrollY += dy
                updateHandlePosition()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showHandle()
                }
            }
        })

        fastScrollCallback = callback
        measureRecyclerViewOnRedraw()
    }

    fun measureRecyclerViewOnRedraw() {
        recyclerView?.onGlobalLayout {
            measureRecyclerView()
        }
    }

    fun measureRecyclerView() {
        val adapter = recyclerView!!.adapter
        val spanCount = ((recyclerView!!.layoutManager as? GridLayoutManager)?.spanCount ?: 1)
        val otherDimension = Math.floor((adapter.itemCount - 1) / spanCount.toDouble()) + 1
        if (isHorizontal) {
            val size = recyclerView!!.height / spanCount
            recyclerViewContentWidth = (otherDimension * size).toInt()
        } else {
            val size = recyclerView!!.width / spanCount
            recyclerViewContentHeight = (otherDimension * size).toInt()
        }
    }

    fun updatePrimaryColor() {
        handle!!.background.applyColorFilter(context.getAdjustedPrimaryColor())
        updateBubblePrimaryColor()
    }

    fun updateBubbleColors() {
        updateBubblePrimaryColor()
        updateBubbleTextColor()
        updateBubbleBackgroundColor()
    }

    fun updateBubblePrimaryColor() {
        getBubbleBackgroundDrawable()?.setStroke(resources.displayMetrics.density.toInt(), context.getAdjustedPrimaryColor())
    }

    fun updateBubbleTextColor() {
        bubble?.setTextColor(context.baseConfig.textColor)
    }

    fun updateBubbleBackgroundColor() {
        getBubbleBackgroundDrawable()?.setColor(context.baseConfig.backgroundColor)
    }

    fun updateBubbleText(text: String) {
        bubble?.text = text
    }

    private fun getBubbleBackgroundDrawable() = bubble?.background as? GradientDrawable

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        recyclerViewWidth = width
        recyclerViewHeight = height
        updateHandlePosition()
    }

    private fun updateHandlePosition() {
        if (handle!!.isSelected || recyclerView == null)
            return

        if (isHorizontal) {
            val proportion = currScrollX.toFloat() / (recyclerViewContentWidth - recyclerViewWidth)
            val targetX = proportion * (recyclerViewWidth - handleWidth)
            handle!!.x = getValueInRange(0f, recyclerViewWidth - handleWidth.toFloat(), targetX)
        } else {
            val proportion = currScrollY.toFloat() / (recyclerViewContentHeight - recyclerViewHeight)
            val targetY = proportion * (recyclerViewHeight - handleHeight)
            handle!!.y = getValueInRange(0f, recyclerViewHeight - handleHeight.toFloat(), targetY)
        }
        hideHandle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // allow dragging only the handle itself
        if (!handle!!.isSelected) {
            if (isHorizontal) {
                val min = handle!!.x
                val max = min + handleWidth
                if (event.x < min || event.x > max) {
                    return super.onTouchEvent(event)
                }
            } else {
                val min = handle!!.y
                val max = min + handleHeight
                if (event.y < min || event.y > max) {
                    return super.onTouchEvent(event)
                }
            }
        }

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isHorizontal) {
                    handleXOffset = (event.x - handle!!.x).toInt()
                } else {
                    handleYOffset = (event.y - handle!!.y).toInt()
                }
                handle!!.isSelected = true
                swipeRefreshLayout?.isEnabled = false
                showHandle()
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
                handleYOffset = 0
                handle!!.isSelected = false
                swipeRefreshLayout?.isEnabled = true
                hideHandle()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun setRecyclerViewPosition(pos: Float) {
        if (recyclerView != null) {
            val proportion = if (isHorizontal) {
                pos / (recyclerViewWidth - (handleWidth - handleXOffset))
            } else {
                pos / (recyclerViewHeight - (handleHeight - handleYOffset))
            }

            if (isHorizontal) {
                val diffInMove = pos - handleXOffset
                val movePercent = diffInMove / (recyclerViewWidth.toFloat() - handleWidth)
                val target = (recyclerViewContentWidth - recyclerViewWidth) * movePercent
                val diff = target.toInt() - currScrollX
                recyclerView!!.scrollBy(diff, 0)
            } else {
                val diffInMove = pos - handleYOffset
                val movePercent = diffInMove / (recyclerViewHeight.toFloat() - handleHeight)
                val target = (recyclerViewContentHeight - recyclerViewHeight) * movePercent
                val diff = target.toInt() - currScrollY
                recyclerView!!.scrollBy(0, diff)
            }

            val itemCount = recyclerView!!.adapter.itemCount
            val targetPos = getValueInRange(0f, (itemCount - 1).toFloat(), proportion * itemCount).toInt()
            fastScrollCallback?.invoke(targetPos)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        handle = getChildAt(0)
        handle!!.onGlobalLayout {
            handleWidth = handle!!.width
            handleHeight = handle!!.height
            showHandle()
        }

        bubble = getChildAt(1) as? TextView
        bubble?.onGlobalLayout {
            if (bubbleHeight == 0) {
                bubbleHeight = bubble!!.height
            }
            updateBubbleColors()
        }
    }

    private fun showHandle() {
        handle!!.animate().alpha(1f).start()  // override the fade animation
        handle!!.alpha = 1f

        if (handle!!.isSelected && allowBubbleDisplay) {
            bubbleHideHandler.removeCallbacksAndMessages(null)
            bubble?.alpha = 1f
            bubble?.animate()?.alpha(1f)?.start()
        }
    }

    private fun hideHandle() {
        if (!handle!!.isSelected) {
            handle!!.animate().alpha(0f).startDelay = HANDLE_HIDE_DELAY

            if (bubble != null) {
                bubbleHideHandler.removeCallbacksAndMessages(null)
                bubbleHideHandler.postDelayed({
                    bubble?.animate()?.alpha(0f)?.withEndAction {
                        if (bubble?.alpha == 0f) {
                            bubble?.text = ""
                        }
                    }
                }, HANDLE_HIDE_DELAY)
            }
        }
    }

    private fun setPosition(pos: Float) {
        if (isHorizontal) {
            handle!!.x = getValueInRange(0f, (recyclerViewWidth - handleWidth).toFloat(), pos - handleXOffset)
            if (bubble != null) {
                val bubbleWidth = bubble!!.width
                bubble!!.x = getValueInRange(tinyMargin, (recyclerViewWidth - bubbleWidth.toFloat()), (handle!!.x - bubbleWidth))
            }
        } else {
            handle!!.y = getValueInRange(0f, (recyclerViewHeight - handleHeight).toFloat(), pos - handleYOffset)
            if (bubble != null) {
                bubble!!.y = getValueInRange(tinyMargin, (recyclerViewHeight - bubbleHeight.toFloat()), (handle!!.y - bubbleHeight))
            }
        }
        hideHandle()
    }

    private fun getValueInRange(min: Float, max: Float, value: Float) = Math.min(Math.max(min, value), max)
}
