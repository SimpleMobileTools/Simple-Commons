package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.android.synthetic.main.breadcrumb_item.view.*

class Breadcrumbs(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnClickListener {
    private var availableWidth = 0
    private var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var textColor = context.baseConfig.textColor
    private var fontSize = resources.getDimension(R.dimen.bigger_text_size)
    private var lastPath = ""

    var listener: BreadcrumbsListener? = null

    init {
        onGlobalLayout {
            availableWidth = width
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childRight = measuredWidth - paddingRight
        val childBottom = measuredHeight - paddingBottom
        val childHeight = childBottom - paddingTop

        val usableWidth = availableWidth - paddingLeft - paddingRight
        var maxHeight = 0
        var curWidth: Int
        var curHeight: Int
        var curLeft = paddingLeft
        var curTop = paddingTop

        val cnt = childCount
        for (i in 0 until cnt) {
            val child = getChildAt(i)

            child.measure(MeasureSpec.makeMeasureSpec(usableWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST))
            curWidth = child.measuredWidth
            curHeight = child.measuredHeight

            if (curLeft + curWidth >= childRight) {
                curLeft = paddingLeft
                curTop += maxHeight
                maxHeight = 0
            }

            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight)
            if (maxHeight < curHeight)
                maxHeight = curHeight

            curLeft += curWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val usableWidth = availableWidth - paddingLeft - paddingRight
        var width = 0
        var rowHeight = 0
        var lines = 1

        val cnt = childCount
        for (i in 0 until cnt) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            width += child.measuredWidth
            rowHeight = child.measuredHeight

            if (width / usableWidth > 0) {
                lines++
                width = child.measuredWidth
            }
        }

        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val calculatedHeight = paddingTop + paddingBottom + rowHeight * lines
        setMeasuredDimension(parentWidth, calculatedHeight)
    }

    fun setBreadcrumb(fullPath: String) {
        lastPath = fullPath
        val basePath = fullPath.getBasePath(context)
        var currPath = basePath
        val tempPath = context.humanizePath(fullPath)

        removeAllViewsInLayout()
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
            addBreadcrumb(item, i > 0)
        }
    }

    private fun addBreadcrumb(item: FileDirItem, addPrefix: Boolean) {
        inflater.inflate(R.layout.breadcrumb_item, null, false).apply {
            var textToAdd = item.name
            if (addPrefix) {
                textToAdd = "/ $textToAdd"
            }

            if (childCount == 0) {
                resources.apply {
                    background = getDrawable(R.drawable.button_background)
                    background.applyColorFilter(textColor)
                    val medium = getDimension(R.dimen.medium_margin).toInt()
                    setPadding(medium, medium, medium, medium)
                }
            }

            breadcrumb_text.text = textToAdd
            breadcrumb_text.setTextColor(textColor)
            breadcrumb_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            addView(this)
            setOnClickListener(this@Breadcrumbs)

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
        removeView(getChildAt(childCount - 1))
    }

    fun getLastItem() = getChildAt(childCount - 1).tag as FileDirItem

    override fun onClick(v: View) {
        val cnt = childCount
        for (i in 0 until cnt) {
            if (getChildAt(i) != null && getChildAt(i) == v) {
                listener?.breadcrumbClicked(i)
            }
        }
    }

    interface BreadcrumbsListener {
        fun breadcrumbClicked(id: Int)
    }
}
