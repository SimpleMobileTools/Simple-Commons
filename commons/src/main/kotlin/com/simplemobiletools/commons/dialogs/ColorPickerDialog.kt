package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.app.AlertDialog
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.RelativeLayout
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setBackgroundWithStroke
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.views.ColorPickerSquare
import kotlinx.android.synthetic.main.dialog_colorpicker.view.*

// forked from https://github.com/yukuku/ambilwarna
class ColorPickerDialog(val context: Context, color: Int, val callback: (color: Int) -> Unit) {
    lateinit var viewHue: View
    lateinit var viewSatVal: ColorPickerSquare
    lateinit var viewCursor: ImageView
    lateinit var viewNewColor: ImageView
    lateinit var viewTarget: ImageView
    lateinit var viewContainer: ViewGroup
    val currentColorHsv = FloatArray(3)

    init {
        Color.colorToHSV(color, currentColorHsv)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_colorpicker, null).apply {
            viewHue = color_picker_hue
            viewSatVal = color_picker_square
            viewCursor = color_picker_hue_cursor

            viewNewColor = color_picker_new_color
            viewTarget = color_picker_cursor
            viewContainer = color_picker_holder

            viewSatVal.setHue(getHue())
            viewNewColor.setBackgroundWithStroke(getColor())
            color_picker_old_color.setBackgroundWithStroke(color)
        }

        viewHue.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var y = event.y
                if (y < 0f) y = 0f
                if (y > viewHue.measuredHeight) {
                    y = viewHue.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                var hue = 360f - 360f / viewHue.measuredHeight * y
                if (hue == 360f) hue = 0f
                currentColorHsv[0] = hue
                viewSatVal.setHue(getHue())
                moveHuePicker()
                viewNewColor.setBackgroundWithStroke(getColor())
                return@OnTouchListener true
            }
            false
        })

        viewSatVal.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var x = event.x
                var y = event.y

                if (x < 0f)
                    x = 0f
                if (x > viewSatVal.measuredWidth)
                    x = viewSatVal.measuredWidth.toFloat()
                if (y < 0f)
                    y = 0f
                if (y > viewSatVal.measuredHeight)
                    y = viewSatVal.measuredHeight.toFloat()

                currentColorHsv[1] = 1f / viewSatVal.measuredWidth * x
                currentColorHsv[2] = 1f - 1f / viewSatVal.measuredHeight * y

                moveColorPicker()
                viewNewColor.setBackgroundWithStroke(getColor())
                return@OnTouchListener true
            }
            false
        })

        val textColor = BaseConfig.newInstance(context).textColor
        AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, { dialog, which -> callback.invoke(getColor()) })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            context.setupDialogStuff(view, this)
            view.color_picker_arrow.colorFilter = PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_IN)
            view.color_picker_hue_cursor.colorFilter = PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                moveHuePicker()
                moveColorPicker()
                view.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })
    }

    private fun moveHuePicker() {
        var y = viewHue.measuredHeight - getHue() * viewHue.measuredHeight / 360f
        if (y == viewHue.measuredHeight.toFloat())
            y = 0f

        val params = (viewCursor.layoutParams as RelativeLayout.LayoutParams)
        params.leftMargin = ((viewHue.left - Math.floor((viewCursor.measuredWidth / 2).toDouble()) - viewContainer.paddingLeft).toInt())
        params.topMargin = ((viewHue.top + y - Math.floor((viewCursor.measuredHeight / 2).toDouble()) - viewContainer.paddingTop).toInt())
        viewCursor.layoutParams = params
    }

    private fun moveColorPicker() {
        val x = getSat() * viewSatVal.measuredWidth
        val y = (1f - getVal()) * viewSatVal.measuredHeight
        val params = (viewTarget.layoutParams as RelativeLayout.LayoutParams)
        params.leftMargin = ((viewSatVal.left + x).toDouble() - Math.floor((viewTarget.measuredWidth / 2).toDouble()) - viewContainer.paddingLeft).toInt()
        params.topMargin = ((viewSatVal.top + y).toDouble() - Math.floor((viewTarget.measuredHeight / 2).toDouble()) - viewContainer.paddingTop).toInt()
        viewTarget.layoutParams = params
    }

    private fun getColor() = Color.HSVToColor(currentColorHsv)
    private fun getHue() = currentColorHsv[0]
    private fun getSat() = currentColorHsv[1]
    private fun getVal() = currentColorHsv[2]
}
