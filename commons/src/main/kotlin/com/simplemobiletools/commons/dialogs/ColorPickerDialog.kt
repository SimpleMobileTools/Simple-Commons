package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.views.ColorPickerSquare
import kotlinx.android.synthetic.main.dialog_color_picker.view.*

// forked from https://github.com/yukuku/ambilwarna
class ColorPickerDialog(val activity: Activity, color: Int, val removeDimmedBackground: Boolean = false, showUseDefaultButton: Boolean = false,
                        val currentColorCallback: ((color: Int) -> Unit)? = null, val callback: (wasPositivePressed: Boolean, color: Int) -> Unit) {
    lateinit var viewHue: View
    lateinit var viewSatVal: ColorPickerSquare
    lateinit var viewCursor: ImageView
    lateinit var viewNewColor: ImageView
    lateinit var viewTarget: ImageView
    lateinit var newHexField: EditText
    lateinit var viewContainer: ViewGroup
    private val currentColorHsv = FloatArray(3)
    private val backgroundColor = activity.baseConfig.backgroundColor
    private val cornerRadius = activity.getCornerRadius()
    private var isHueBeingDragged = false
    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null

    init {
        Color.colorToHSV(color, currentColorHsv)

        val view = activity.layoutInflater.inflate(R.layout.dialog_color_picker, null).apply {
            viewHue = color_picker_hue
            viewSatVal = color_picker_square
            viewCursor = color_picker_hue_cursor

            viewNewColor = color_picker_new_color
            viewTarget = color_picker_cursor
            viewContainer = color_picker_holder
            newHexField = color_picker_new_hex

            viewSatVal.setHue(getHue())

            viewNewColor.setFillWithStroke(getColor(), backgroundColor, cornerRadius)
            color_picker_old_color.setFillWithStroke(color, backgroundColor, cornerRadius)

            val hexCode = getHexCode(color)
            color_picker_old_hex.text = "#$hexCode"
            color_picker_old_hex.setOnLongClickListener {
                activity.copyToClipboard(hexCode)
                true
            }
            newHexField.setText(hexCode)
        }

        viewHue.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isHueBeingDragged = true
            }

            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var y = event.y
                if (y < 0f)
                    y = 0f

                if (y > viewHue.measuredHeight) {
                    y = viewHue.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                var hue = 360f - 360f / viewHue.measuredHeight * y
                if (hue == 360f)
                    hue = 0f

                currentColorHsv[0] = hue
                updateHue()
                newHexField.setText(getHexCode(getColor()))

                if (event.action == MotionEvent.ACTION_UP) {
                    isHueBeingDragged = false
                }
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
                viewNewColor.setFillWithStroke(getColor(), backgroundColor, cornerRadius)
                newHexField.setText(getHexCode(getColor()))
                return@OnTouchListener true
            }
            false
        })

        newHexField.onTextChangeListener {
            if (it.length == 6 && !isHueBeingDragged) {
                try {
                    val newColor = Color.parseColor("#$it")
                    Color.colorToHSV(newColor, currentColorHsv)
                    updateHue()
                    moveColorPicker()
                } catch (ignored: Exception) {
                }
            }
        }

        val textColor = activity.baseConfig.textColor
        val builder = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok) { dialog, which -> confirmNewColor() }
                .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
                .setOnCancelListener { dialogDismissed() }

        if (showUseDefaultButton) {
            builder.setNeutralButton(R.string.use_default) { dialog, which -> useDefault() }
        }

        dialog = builder.create().apply {
            activity.setupDialogStuff(view, this) {
                view.color_picker_arrow.applyColorFilter(textColor)
                view.color_picker_hex_arrow.applyColorFilter(textColor)
                viewCursor.applyColorFilter(textColor)
            }
        }

        view.onGlobalLayout {
            moveHuePicker()
            moveColorPicker()
        }
    }

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun confirmNewColor() {
        val hexValue = newHexField.value
        if (hexValue.length == 6) {
            callback(true, Color.parseColor("#$hexValue"))
        } else {
            callback(true, getColor())
        }
    }

    private fun useDefault() {
        callback(true, activity.baseConfig.defaultNavigationBarColor)
    }

    private fun getHexCode(color: Int) = color.toHex().substring(1)

    private fun updateHue() {
        viewSatVal.setHue(getHue())
        moveHuePicker()
        viewNewColor.setFillWithStroke(getColor(), backgroundColor, cornerRadius)
        if (removeDimmedBackground && !wasDimmedBackgroundRemoved) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            wasDimmedBackgroundRemoved = true
        }

        currentColorCallback?.invoke(getColor())
    }

    private fun moveHuePicker() {
        var y = viewHue.measuredHeight - getHue() * viewHue.measuredHeight / 360f
        if (y == viewHue.measuredHeight.toFloat())
            y = 0f

        viewCursor.x = (viewHue.left - viewCursor.width).toFloat()
        viewCursor.y = viewHue.top + y - viewCursor.height / 2
    }

    private fun moveColorPicker() {
        val x = getSat() * viewSatVal.measuredWidth
        val y = (1f - getVal()) * viewSatVal.measuredHeight
        viewTarget.x = viewSatVal.left + x - viewTarget.width / 2
        viewTarget.y = viewSatVal.top + y - viewTarget.height / 2
    }

    private fun getColor() = Color.HSVToColor(currentColorHsv)
    private fun getHue() = currentColorHsv[0]
    private fun getSat() = currentColorHsv[1]
    private fun getVal() = currentColorHsv[2]
}
