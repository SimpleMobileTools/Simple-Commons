package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.onGlobalLayout

// based on https://blog.stylingandroid.com/recyclerview-fastscroll-part-1
class FastScroller : FrameLayout {
    var isHorizontal = false
    var measureItemIndex = 0

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
    private var tinyMargin = 0
    private var isScrollingEnabled = false      // a boolean indicating whether the actual recycler view content is higher than the screen
    private var fastScrollCallback: ((Int) -> Unit)? = null
    private var wasRecyclerViewContentSizeSet = false       // stop measuring and calculating content size as soon as it is manually set once

    private val HANDLE_HIDE_DELAY = 1000L
    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var bubbleHideHandler = Handler()
    private var handleHideHandler = Handler()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setViews(recyclerView: RecyclerView, swipeRefreshLayout: SwipeRefreshLayout? = null, callback: ((Int) -> Unit)? = null) {
        this.recyclerView = recyclerView
        this.swipeRefreshLayout = swipeRefreshLayout
        tinyMargin = context.resources.getDimension(R.dimen.tiny_margin).toInt()

        updatePrimaryColor()
        recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (isScrollingEnabled) {
                    if (!handle!!.isSelected) {
                        bubble?.alpha = 0f
                        bubble?.text = ""
                        bubbleHideHandler.removeCallbacksAndMessages(null)
                    }

                    currScrollX += dx
                    currScrollY += dy

                    currScrollX = getValueInRange(0, recyclerViewContentWidth, currScrollX.toFloat()).toInt()
                    currScrollY = getValueInRange(0, recyclerViewContentHeight, currScrollY.toFloat()).toInt()

                    updateHandlePosition()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!isScrollingEnabled) {
                    hideHandle()
                    return
                }

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showHandle()
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    hideHandle()
                }
            }
        })

        fastScrollCallback = callback
        measureRecyclerViewOnRedraw()
    }

    fun resetScrollPositions() {
        currScrollX = 0
        currScrollY = 0
    }

    fun measureRecyclerViewOnRedraw() {
        recyclerView?.onGlobalLayout {
            measureRecyclerView()
        }
    }

    fun measureRecyclerView() {
        if (recyclerView == null || recyclerView!!.adapter == null) {
            return
        }

        if (!wasRecyclerViewContentSizeSet) {
            val adapter = recyclerView!!.adapter
            val spanCount = ((recyclerView!!.layoutManager as? GridLayoutManager)?.spanCount ?: 1)
            val otherDimension = Math.floor((adapter!!.itemCount - 1) / spanCount.toDouble()) + 1
            val size = recyclerView!!.getChildAt(measureItemIndex)?.height ?: 0
            if (isHorizontal) {
                recyclerViewContentWidth = (otherDimension * size).toInt()
            } else {
                recyclerViewContentHeight = (otherDimension * size).toInt()
            }
        }

        isScrollingEnabled = if (isHorizontal) {
            recyclerViewContentWidth > recyclerViewWidth
        } else {
            recyclerViewContentHeight > recyclerViewHeight
        }

        if (!isScrollingEnabled) {
            bubbleHideHandler.removeCallbacksAndMessages(null)
            bubble?.animate()?.cancel()
            bubble?.alpha = 0f
            bubble?.text = ""

            handleHideHandler.removeCallbacksAndMessages(null)
            handle?.animate()?.cancel()
            handle?.alpha = 0f
        }
    }

    fun setContentWidth(width: Int) {
        recyclerViewContentWidth = width
        wasRecyclerViewContentSizeSet = true
        updateHandlePosition()
        isScrollingEnabled = recyclerViewContentWidth > recyclerViewWidth
    }

    fun setContentHeight(height: Int) {
        recyclerViewContentHeight = height
        wasRecyclerViewContentSizeSet = true
        updateHandlePosition()
        isScrollingEnabled = recyclerViewContentHeight > recyclerViewHeight
    }

    fun setScrollToX(x: Int) {
        measureRecyclerView()
        currScrollX = x
        updateHandlePosition()
        hideHandle()
    }

    fun setScrollToY(y: Int) {
        measureRecyclerView()
        currScrollY = y
        updateHandlePosition()
        hideHandle()
    }

    fun updatePrimaryColor(color: Int = context.getAdjustedPrimaryColor()) {
        handle!!.background.applyColorFilter(color)
        updateBubblePrimaryColor()
    }

    fun updateBubbleColors() {
        updateBubblePrimaryColor()
        updateBubbleTextColor()
        updateBubbleBackgroundColor()
    }

    fun updateBubblePrimaryColor(color: Int = context.getAdjustedPrimaryColor()) {
        getBubbleBackgroundDrawable()?.setStroke(resources.displayMetrics.density.toInt(), color)
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
    }

    private fun updateHandlePosition() {
        if (handle!!.isSelected || recyclerView == null)
            return

        if (isHorizontal) {
            val proportion = currScrollX.toFloat() / (recyclerViewContentWidth - recyclerViewWidth)
            val targetX = proportion * (recyclerViewWidth - handleWidth)
            handle!!.x = getValueInRange(0, recyclerViewWidth - handleWidth, targetX)
        } else {
            val proportion = currScrollY.toFloat() / (recyclerViewContentHeight - recyclerViewHeight)
            val targetY = proportion * (recyclerViewHeight - handleHeight)
            handle!!.y = getValueInRange(0, recyclerViewHeight - handleHeight, targetY)
        }
        showHandle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // allow dragging only the handle itself
        if (!isScrollingEnabled) {
            return super.onTouchEvent(event)
        }

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
                if (isScrollingEnabled) {
                    startScrolling()
                }
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isScrollingEnabled) {
                    try {
                        if (isHorizontal) {
                            setPosition(event.x)
                            setRecyclerViewPosition(event.x)
                        } else {
                            setPosition(event.y)
                            setRecyclerViewPosition(event.y)
                        }
                    } catch (ignored: Exception) {
                    }
                }
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handleYOffset = 0
                handle!!.isSelected = false
                if (context.baseConfig.enablePullToRefresh) {
                    swipeRefreshLayout?.isEnabled = true
                }
                hideHandle()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun startScrolling() {
        handle!!.isSelected = true
        swipeRefreshLayout?.isEnabled = false
        showHandle()
    }

    private fun setRecyclerViewPosition(pos: Float) {
        if (recyclerView != null) {
            val targetProportion: Float
            if (isHorizontal) {
                targetProportion = currScrollX / recyclerViewContentWidth.toFloat()
                val diffInMove = pos - handleXOffset
                val movePercent = diffInMove / (recyclerViewWidth.toFloat() - handleWidth)
                val target = (recyclerViewContentWidth - recyclerViewWidth) * movePercent
                val diff = target.toInt() - currScrollX
                recyclerView!!.scrollBy(diff, 0)
            } else {
                targetProportion = currScrollY / recyclerViewContentHeight.toFloat()
                val diffInMove = pos - handleYOffset
                val movePercent = diffInMove / (recyclerViewHeight.toFloat() - handleHeight)
                val target = (recyclerViewContentHeight - recyclerViewHeight) * movePercent
                val diff = target.toInt() - currScrollY
                recyclerView!!.scrollBy(0, diff)
            }

            val itemCount = recyclerView!!.adapter!!.itemCount
            val targetPos = getValueInRange(0, itemCount - 1, targetProportion * itemCount).toInt()
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
            hideHandle()
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
        if (!isScrollingEnabled) {
            return
        }

        handleHideHandler.removeCallbacksAndMessages(null)
        handle!!.animate().cancel()
        handle!!.alpha = 1f
        if (handleWidth == 0 && handleHeight == 0) {
            handleWidth = handle!!.width
            handleHeight = handle!!.height
        }
    }

    private fun hideHandle() {
        if (!handle!!.isSelected) {
            handleHideHandler.removeCallbacksAndMessages(null)
            handleHideHandler.postDelayed({
                handle!!.animate().alpha(0f).start()
            }, HANDLE_HIDE_DELAY)

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
            handle!!.x = getValueInRange(0, recyclerViewWidth - handleWidth, pos - handleXOffset)
            if (bubble != null && handle!!.isSelected) {
                val bubbleWidth = bubble!!.width
                bubble!!.x = getValueInRange(tinyMargin, recyclerViewWidth - bubbleWidth, handle!!.x - bubbleWidth)
                bubbleHideHandler.removeCallbacksAndMessages(null)
                bubble?.alpha = 1f
            }
        } else {
            handle!!.y = getValueInRange(0, recyclerViewHeight - handleHeight, pos - handleYOffset)
            if (bubble != null && handle!!.isSelected) {
                bubble!!.y = getValueInRange(tinyMargin, recyclerViewHeight - bubbleHeight, handle!!.y - bubbleHeight)
                bubbleHideHandler.removeCallbacksAndMessages(null)
                bubble?.alpha = 1f
            }
        }
        hideHandle()
    }

    private fun getValueInRange(min: Int, max: Int, value: Float) = Math.min(Math.max(min.toFloat(), value), max.toFloat())
}
