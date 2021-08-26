package com.simplemobiletools.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.android.synthetic.main.breadcrumb_item.view.*

class Breadcrumbs(context: Context, attrs: AttributeSet) : HorizontalScrollView(context, attrs) {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val itemsLayout: LinearLayout
    private var textColor = context.baseConfig.textColor
    private var fontSize = resources.getDimension(R.dimen.bigger_text_size)
    private var lastPath = ""
    private var isLayoutDirty = true
    private var isScrollToSelectedItemPending = false
    private var isFirstScroll = true

    private val textColorStateList: ColorStateList
        get() = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf()),
            intArrayOf(
                textColor,
                textColor.adjustAlpha(0.6f)
            )
        )

    val itemsCount: Int
        get() = itemsLayout.childCount
    var listener: BreadcrumbsListener? = null

    init {
        isHorizontalScrollBarEnabled = false
        itemsLayout = LinearLayout(context)
        itemsLayout.orientation = LinearLayout.HORIZONTAL
        itemsLayout.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
        setPaddingRelative(0, 0, 0, 0)
        addView(itemsLayout, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        isLayoutDirty = false
        if (isScrollToSelectedItemPending) {
            scrollToSelectedItem()
            isScrollToSelectedItemPending = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightMeasureSpec = heightMeasureSpec
        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            var height = context.resources.getDimensionPixelSize(R.dimen.breadcrumbs_layout_height)
            if (heightMode == MeasureSpec.AT_MOST) {
                height = height.coerceAtMost(MeasureSpec.getSize(heightMeasureSpec))
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun scrollToSelectedItem() {
        if (isLayoutDirty) {
            isScrollToSelectedItemPending = true
            return
        }

        var selectedIndex = itemsLayout.childCount - 1
        val cnt = itemsLayout.childCount
        for (i in 0 until cnt) {
            val child = itemsLayout.getChildAt(i)
            if ((child.tag as? FileDirItem)?.path == "$lastPath/") {
                selectedIndex = i
                break
            }
        }

        val selectedItemView = itemsLayout.getChildAt(selectedIndex)
        val scrollX = if (layoutDirection == View.LAYOUT_DIRECTION_LTR) {
            selectedItemView.left - itemsLayout.paddingStart
        } else {
            selectedItemView.right - width + itemsLayout.paddingStart
        }
        if (!isFirstScroll && isShown) {
            smoothScrollTo(scrollX, 0)
        } else {
            scrollTo(scrollX, 0)
        }
        isFirstScroll = false
    }

    override fun requestLayout() {
        isLayoutDirty = true

        super.requestLayout()
    }

    fun setBreadcrumb(fullPath: String) {
        lastPath = fullPath
        val basePath = fullPath.getBasePath(context)
        var currPath = basePath
        val tempPath = context.humanizePath(fullPath)

        itemsLayout.removeAllViews()
        val dirs = tempPath.split("/").dropLastWhile(String::isEmpty)
        for (i in dirs.indices) {
            val dir = dirs[i]
            if (i > 0) {
                currPath += dir + "/"
            }

            if (dir.isEmpty()) {
                continue
            }

            currPath = "${currPath.trimEnd('/')}/"
            val item = FileDirItem(currPath, dir, true, 0, 0, 0)
            addBreadcrumb(item, i, i > 0)
            scrollToSelectedItem()
        }
    }

    private fun addBreadcrumb(item: FileDirItem, index: Int, addPrefix: Boolean) {
        inflater.inflate(R.layout.breadcrumb_item, itemsLayout, false).apply {
            var textToAdd = item.name
            if (addPrefix) {
                textToAdd = "> $textToAdd"
            }

            if (itemsLayout.childCount == 0) {
                resources.apply {
                    background = ContextCompat.getDrawable(context, R.drawable.button_background)
                    background.applyColorFilter(textColor)
                    val medium = getDimension(R.dimen.medium_margin).toInt()
                    setPadding(medium, medium, medium, medium)
                }
            }

            isActivated = item.path == "$lastPath/"
            breadcrumb_text.text = textToAdd
            breadcrumb_text.setTextColor(textColorStateList)
            breadcrumb_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            itemsLayout.addView(this)

            setOnClickListener { v ->
                if (itemsLayout.getChildAt(index) != null && itemsLayout.getChildAt(index) == v) {
                    if ((v.tag as? FileDirItem)?.path == lastPath) {
                        scrollToSelectedItem()
                    } else {
                        listener?.breadcrumbClicked(index)
                    }
                }
            }

            tag = item
        }
    }

    fun updateColor(color: Int) {
        textColor = color
        setBreadcrumb(lastPath)
    }

    fun updateFontSize(size: Float) {
        fontSize = size
        setBreadcrumb(lastPath)
    }

    fun removeBreadcrumb() {
        itemsLayout.removeView(itemsLayout.getChildAt(itemsLayout.childCount - 1))
    }

    fun getItem(index: Int) = itemsLayout.getChildAt(index).tag as FileDirItem

    fun getLastItem() = itemsLayout.getChildAt(itemsLayout.childCount - 1).tag as FileDirItem


    interface BreadcrumbsListener {
        fun breadcrumbClicked(id: Int)
    }
}
