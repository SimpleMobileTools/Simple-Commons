package com.simplemobiletools.commons.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.toColorInt
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.databinding.DialogColorPickerBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isQPlus
import com.simplemobiletools.commons.views.ColorPickerSquare
import java.util.LinkedList
import kotlin.math.cos
import kotlin.math.sin

private const val RECENT_COLORS_NUMBER = 5
private val PICKER_SIZE = 240.dp
private val COLOR_BOX_SIZE = 30.dp

// forked from https://github.com/yukuku/ambilwarna
@SuppressLint("ClickableViewAccessibility")
class ColorPickerDialog(
    val activity: Activity,
    color: Int,
    val removeDimmedBackground: Boolean = false,
    val addDefaultColorButton: Boolean = false,
    val currentColorCallback: ((color: Int) -> Unit)? = null,
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    var viewHue: View
    var viewSatVal: ColorPickerSquare
    var viewCursor: ImageView
    var viewNewColor: ImageView
    var viewTarget: ImageView
    var newHexField: EditText
    var viewContainer: ViewGroup
    private val baseConfig = activity.baseConfig
    private val currentColorHsv = FloatArray(3)
    private val backgroundColor = baseConfig.backgroundColor
    private var isHueBeingDragged = false
    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null

    init {
        android.graphics.Color.colorToHSV(color, currentColorHsv)

        val view = DialogColorPickerBinding.inflate(activity.layoutInflater, null, false).apply {
            if (isQPlus()) {
                root.isForceDarkAllowed = false
            }

            viewHue = colorPickerHue
            viewSatVal = colorPickerSquare
            viewCursor = colorPickerHueCursor

            viewNewColor = colorPickerNewColor
            viewTarget = colorPickerCursor
            viewContainer = colorPickerHolder
            newHexField = colorPickerNewHex

            viewSatVal.setHue(getHue())

            viewNewColor.setFillWithStroke(getColor(), backgroundColor)
            colorPickerOldColor.setFillWithStroke(color, backgroundColor)

            val hexCode = getHexCode(color)
            colorPickerOldHex.text = "#$hexCode"
            colorPickerOldHex.setOnLongClickListener {
                activity.copyToClipboard(hexCode)
                true
            }
            newHexField.setText(hexCode)
            setupRecentColors()
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
                viewNewColor.setFillWithStroke(getColor(), backgroundColor)
                newHexField.setText(getHexCode(getColor()))
                return@OnTouchListener true
            }
            false
        })

        newHexField.onTextChangeListener {
            if (it.length == 6 && !isHueBeingDragged) {
                try {
                    val newColor = android.graphics.Color.parseColor("#$it")
                    android.graphics.Color.colorToHSV(newColor, currentColorHsv)
                    updateHue()
                    moveColorPicker()
                } catch (ignored: Exception) {
                }
            }
        }

        val textColor = activity.getProperTextColor()
        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> confirmNewColor() }
            .setNegativeButton(R.string.cancel) { _, _ -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .apply {
                if (addDefaultColorButton) {
                    setNeutralButton(R.string.default_color) { _, _ -> confirmDefaultColor() }
                }
            }

        builder.apply {
            activity.setupDialogStuff(view.root, this) { alertDialog ->
                dialog = alertDialog
                view.colorPickerArrow.applyColorFilter(textColor)
                view.colorPickerHexArrow.applyColorFilter(textColor)
                viewCursor.applyColorFilter(textColor)
            }
        }

        view.root.onGlobalLayout {
            moveHuePicker()
            moveColorPicker()
        }
    }

    private fun DialogColorPickerBinding.setupRecentColors() {
        val colorPickerRecentColors = baseConfig.colorPickerRecentColors
        if (colorPickerRecentColors.isNotEmpty()) {
            recentColors.beVisible()
            val squareSize = root.context.resources.getDimensionPixelSize(R.dimen.colorpicker_hue_width)
            colorPickerRecentColors.take(RECENT_COLORS_NUMBER).forEach { recentColor ->
                val recentColorView = ImageView(root.context)
                recentColorView.id = View.generateViewId()
                recentColorView.layoutParams = ViewGroup.LayoutParams(squareSize, squareSize)
                recentColorView.setFillWithStroke(recentColor, backgroundColor)
                recentColorView.setOnClickListener { newHexField.setText(getHexCode(recentColor)) }
                recentColors.addView(recentColorView)
                recentColorsFlow.addView(recentColorView)
            }
        }
    }

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun confirmDefaultColor() {
        callback(true, 0)
    }

    private fun confirmNewColor() {
        val hexValue = newHexField.value
        val newColor = if (hexValue.length == 6) {
            android.graphics.Color.parseColor("#$hexValue")
        } else {
            getColor()
        }

        activity.addRecentColor(newColor)
        callback(true, newColor)
    }


    private fun getHexCode(color: Int) = color.toHex().substring(1)

    private fun updateHue() {
        viewSatVal.setHue(getHue())
        moveHuePicker()
        viewNewColor.setFillWithStroke(getColor(), backgroundColor)
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

    private fun getColor() = android.graphics.Color.HSVToColor(currentColorHsv)
    private fun getHue() = currentColorHsv[0]
    private fun getSat() = currentColorHsv[1]
    private fun getVal() = currentColorHsv[2]
}

@Composable
fun ColorPickerAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    @ColorInt
    color: Int,
    recentColors: List<Int>,
    removeDimmedBackground: Boolean = false,
    addDefaultColorButton: Boolean = false,
    onActiveColorChange: ((color: Int) -> Unit)? = null,
    onButtonPressed: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    var wasDimmedBackgroundRemoved by remember { mutableStateOf(false) }

    AlertDialog(
        modifier = modifier
            .dialogBorder,
        onDismissRequest = alertDialogState::hide,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .dialogBackgroundAndShape
                .padding(16.dp)
        ) {
            val pickerController = rememberColorPickerController()
            var currentColorHex by remember { mutableStateOf(color.toHex().substring(1)) }

            if (recentColors.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    recentColors.take(RECENT_COLORS_NUMBER).forEach {
                        with(LocalDensity.current) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(it), CircleShape)
                                    .clickable {
                                        val (brightness, colorPoint) = colorToBrightnessAndCoordinates(
                                            pickerRadius = PICKER_SIZE.toPx() * 0.5f,
                                            color = it
                                        )
                                        pickerController.setBrightness(brightness, true)
                                        pickerController.selectByCoordinate(x = colorPoint.x, y = colorPoint.y, fromUser = true)
                                    },
                            )
                        }
                    }
                }
            }

            HsvColorPicker(
                modifier = Modifier
                    .padding(16.dp)
                    .size(PICKER_SIZE)
                    .align(Alignment.CenterHorizontally),
                controller = pickerController,
                initialColor = Color(color),
                onColorChanged = {
                    currentColorHex = it.hexCode.substring(2)
                    onActiveColorChange?.invoke(it.color.toArgb())
                    if (removeDimmedBackground && !wasDimmedBackgroundRemoved) {
                        (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
                        wasDimmedBackgroundRemoved = true
                    }
                }
            )

            BrightnessSlider(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                controller = pickerController,
                initialColor = Color(color)
            )

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = color.toHex(),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier.weight(1f, false),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_arrow_right_vector),
                        contentDescription = null
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    with(LocalDensity.current) {
                        BasicTextField(
                            value = currentColorHex,
                            onValueChange = {
                                currentColorHex = it.filter { it.isHexit() }.take(6).uppercase()
                                if (currentColorHex.length == 6) {
                                    val (brightness, colorPoint) = colorToBrightnessAndCoordinates(
                                        pickerRadius = PICKER_SIZE.toPx() * 0.5f,
                                        color = "#$currentColorHex".toColorInt()
                                    )
                                    pickerController.setBrightness(brightness, true)
                                    pickerController.selectByCoordinate(x = colorPoint.x, y = colorPoint.y, fromUser = true)
                                }
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.background(Color.Gray.copy(alpha = 0.2f)),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "#",
                                    fontSize = 14.sp
                                )
                                it()
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(COLOR_BOX_SIZE)
                        .background(Color(color), RectangleShape)
                )

                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_arrow_right_vector),
                    contentDescription = null
                )

                Box(
                    modifier = Modifier
                        .size(COLOR_BOX_SIZE)
                        .background(pickerController.selectedColor.value, RectangleShape)
                )
            }

            Row(
                Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    alertDialogState.hide()
                    onButtonPressed(false, 0)
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }

                if (addDefaultColorButton) {
                    TextButton(onClick = {
                        alertDialogState.hide()
                        onButtonPressed(true, 0)
                    }) {
                        Text(text = stringResource(id = R.string.default_color))
                    }
                }

                TextButton(onClick = {
                    alertDialogState.hide()
                    val pickedColor = pickerController.selectedColor.value.toArgb()
                    context.addRecentColor(pickedColor)
                    onButtonPressed(false, pickedColor)
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            }
        }
    }
}

private fun Context.addRecentColor(color: Int) {
    var recentColors = baseConfig.colorPickerRecentColors

    recentColors.remove(color)
    if (recentColors.size >= RECENT_COLORS_NUMBER) {
        val numberOfColorsToDrop = recentColors.size - RECENT_COLORS_NUMBER + 1
        recentColors = LinkedList(recentColors.dropLast(numberOfColorsToDrop))
    }
    recentColors.addFirst(color)

    baseConfig.colorPickerRecentColors = recentColors
}

private fun Char.isHexit() =
    this.isDigit() || listOf('a', 'b', 'c', 'd', 'e', 'f').contains(this.lowercaseChar())

private fun colorToBrightnessAndCoordinates(
    pickerRadius: Float,
    @ColorInt color: Int
): Pair<Float, PointF> {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color, hsv)
    val angle = (Math.PI / 180f) * hsv[0] * -1
    val saturationVector = pickerRadius * hsv[1]
    val center = PointF(pickerRadius, pickerRadius)
    val x = saturationVector * cos(angle) + center.x
    val y = saturationVector * sin(angle) + center.y
    return Pair(hsv[2], PointF(x.toFloat(), y.toFloat()))
}

@Composable
@MyDevices
private fun ColorPickerAlertDialogPreview() {
    AppThemeSurface {
        ColorPickerAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            modifier = Modifier,
            recentColors = listOf(android.graphics.Color.BLACK, android.graphics.Color.BLUE, android.graphics.Color.RED),
            color = android.graphics.Color.RED,
            onButtonPressed = { _, _ -> }
        )
    }
}
