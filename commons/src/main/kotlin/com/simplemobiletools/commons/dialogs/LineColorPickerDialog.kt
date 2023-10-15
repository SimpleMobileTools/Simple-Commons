package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.MaterialToolbar
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.DialogSurface
import com.simplemobiletools.commons.compose.alert_dialog.dialogTextColor
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.databinding.DialogLineColorPickerBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.interfaces.LineColorPickerListener

class LineColorPickerDialog(
    val activity: BaseSimpleActivity, val color: Int, val isPrimaryColorPicker: Boolean, val primaryColors: Int = R.array.md_primary_colors,
    val appIconIDs: ArrayList<Int>? = null, val toolbar: MaterialToolbar? = null, val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    private val PRIMARY_COLORS_COUNT = 19
    private val DEFAULT_PRIMARY_COLOR_INDEX = 14
    private val DEFAULT_SECONDARY_COLOR_INDEX = 6
    private val DEFAULT_COLOR_VALUE = activity.resources.getColor(R.color.color_primary)

    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null
    private var view = DialogLineColorPickerBinding.inflate(activity.layoutInflater, null, false)

    init {
        view.apply {
            hexCode.text = color.toHex()
            hexCode.setOnLongClickListener {
                activity.copyToClipboard(hexCode.value.substring(1))
                true
            }

            lineColorPickerIcon.beGoneIf(isPrimaryColorPicker)
            val indexes = getColorIndexes(color)

            val primaryColorIndex = indexes.first
            primaryColorChanged(primaryColorIndex)
            primaryLineColorPicker.updateColors(getColors(primaryColors), primaryColorIndex)
            primaryLineColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor = if (isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }

            secondaryLineColorPicker.beVisibleIf(isPrimaryColorPicker)
            secondaryLineColorPicker.updateColors(getColorsForIndex(primaryColorIndex), indexes.second)
            secondaryLineColorPicker.listener = LineColorPickerListener { _, color -> colorUpdated(color) }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel) { dialog, which -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .apply {
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    fun getSpecificColor() = view.secondaryLineColorPicker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        view.hexCode.text = color.toHex()
        if (isPrimaryColorPicker) {

            if (toolbar != null) {
                activity.updateTopBarColors(toolbar, color)
            }

            if (!wasDimmedBackgroundRemoved) {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                wasDimmedBackgroundRemoved = true
            }
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        if (color == DEFAULT_COLOR_VALUE) {
            return getDefaultColorPair()
        }

        for (i in 0 until PRIMARY_COLORS_COUNT) {
            getColorsForIndex(i).indexOfFirst { color == it }.apply {
                if (this != -1) {
                    return Pair(i, this)
                }
            }
        }

        return getDefaultColorPair()
    }

    private fun primaryColorChanged(index: Int) {
        view.lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
    }

    private fun getDefaultColorPair() = Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun dialogConfirmed() {
        val targetView = if (isPrimaryColorPicker) view.secondaryLineColorPicker else view.primaryLineColorPicker
        val color = targetView.getCurrentColor()
        callback(true, color)
    }

    private fun getColorsForIndex(index: Int) = when (index) {
        0 -> getColors(R.array.md_reds)
        1 -> getColors(R.array.md_pinks)
        2 -> getColors(R.array.md_purples)
        3 -> getColors(R.array.md_deep_purples)
        4 -> getColors(R.array.md_indigos)
        5 -> getColors(R.array.md_blues)
        6 -> getColors(R.array.md_light_blues)
        7 -> getColors(R.array.md_cyans)
        8 -> getColors(R.array.md_teals)
        9 -> getColors(R.array.md_greens)
        10 -> getColors(R.array.md_light_greens)
        11 -> getColors(R.array.md_limes)
        12 -> getColors(R.array.md_yellows)
        13 -> getColors(R.array.md_ambers)
        14 -> getColors(R.array.md_oranges)
        15 -> getColors(R.array.md_deep_oranges)
        16 -> getColors(R.array.md_browns)
        17 -> getColors(R.array.md_blue_greys)
        18 -> getColors(R.array.md_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors(id: Int) = activity.resources.getIntArray(id).toCollection(ArrayList())
}

@Composable
fun LineColorPickerAlertDialog(
    alertDialogState: AlertDialogState,
    @ColorInt color: Int,
    isPrimaryColorPicker: Boolean,
    modifier: Modifier = Modifier,
    primaryColors: Int = R.array.md_primary_colors,
    appIconIDs: ArrayList<Int>? = null,
    onActiveColorChange: (color: Int) -> Unit,
    onButtonPressed: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    var wasDimmedBackgroundRemoved by remember { mutableStateOf(false) }

    val defaultColor = remember {
        ContextCompat.getColor(context, R.color.color_primary)
    }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = alertDialogState::hide,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DialogSurface {
            Column(
                Modifier
                    .fillMaxWidth(0.95f)
                    .padding(SimpleTheme.dimens.padding.extraLarge)
            ) {
                val dialogTextColor = dialogTextColor
                var dialogLineColorPickerBinding by remember { mutableStateOf<DialogLineColorPickerBinding?>(null) }
                AndroidViewBinding(
                    DialogLineColorPickerBinding::inflate, onRelease = {
                        dialogLineColorPickerBinding = null
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    root.updateLayoutParams<FrameLayout.LayoutParams> {
                        height = FrameLayout.LayoutParams.WRAP_CONTENT
                    }
                    dialogLineColorPickerBinding = this
                    fun colorUpdated(color: Int) {
                        hexCode.text = color.toHex()
                        onActiveColorChange(color)
                        if (isPrimaryColorPicker) {
                            if (!wasDimmedBackgroundRemoved) {
                                (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
                                wasDimmedBackgroundRemoved = true
                            }
                        }
                    }

                    hexCode.setTextColor(dialogTextColor.toArgb())
                    hexCode.text = color.toHex()
                    hexCode.setOnLongClickListener {
                        context.copyToClipboard(hexCode.value.substring(1))
                        true
                    }

                    lineColorPickerIcon.beGoneIf(isPrimaryColorPicker)
                    val indexes = context.getColorIndexes(color, defaultColor)

                    val primaryColorIndex = indexes.first
                    lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(primaryColorIndex) ?: 0)
                    primaryLineColorPicker.updateColors(context.getColors(primaryColors), primaryColorIndex)
                    primaryLineColorPicker.listener = LineColorPickerListener { index, color ->
                        val secondaryColors = context.getColorsForIndex(index)
                        secondaryLineColorPicker.updateColors(secondaryColors)

                        val newColor = if (isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                        colorUpdated(newColor)

                        if (!isPrimaryColorPicker) {
                            lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
                        }
                    }

                    secondaryLineColorPicker.beVisibleIf(isPrimaryColorPicker)
                    secondaryLineColorPicker.updateColors(context.getColorsForIndex(primaryColorIndex), indexes.second)
                    secondaryLineColorPicker.listener = LineColorPickerListener { _, color -> colorUpdated(color) }
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
                    TextButton(onClick = {
                        if (dialogLineColorPickerBinding != null) {
                            val targetView =
                                if (isPrimaryColorPicker) dialogLineColorPickerBinding!!.secondaryLineColorPicker else dialogLineColorPickerBinding!!.primaryLineColorPicker
                            onButtonPressed(true, targetView.getCurrentColor())
                        }
                        alertDialogState.hide()

                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

private const val PRIMARY_COLORS_COUNT = 19
private const val DEFAULT_PRIMARY_COLOR_INDEX = 14
private const val DEFAULT_SECONDARY_COLOR_INDEX = 6

private fun Context.getColorIndexes(color: Int, defaultColor: Int): Pair<Int, Int> {
    if (color == defaultColor) {
        return getDefaultColorPair()
    }

    for (i in 0 until PRIMARY_COLORS_COUNT) {
        getColorsForIndex(i).indexOfFirst { color == it }.apply {
            if (this != -1) {
                return Pair(i, this)
            }
        }
    }

    return getDefaultColorPair()
}

private fun getDefaultColorPair() = Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)


private fun Context.getColorsForIndex(index: Int) = when (index) {
    0 -> getColors(R.array.md_reds)
    1 -> getColors(R.array.md_pinks)
    2 -> getColors(R.array.md_purples)
    3 -> getColors(R.array.md_deep_purples)
    4 -> getColors(R.array.md_indigos)
    5 -> getColors(R.array.md_blues)
    6 -> getColors(R.array.md_light_blues)
    7 -> getColors(R.array.md_cyans)
    8 -> getColors(R.array.md_teals)
    9 -> getColors(R.array.md_greens)
    10 -> getColors(R.array.md_light_greens)
    11 -> getColors(R.array.md_limes)
    12 -> getColors(R.array.md_yellows)
    13 -> getColors(R.array.md_ambers)
    14 -> getColors(R.array.md_oranges)
    15 -> getColors(R.array.md_deep_oranges)
    16 -> getColors(R.array.md_browns)
    17 -> getColors(R.array.md_blue_greys)
    18 -> getColors(R.array.md_greys)
    else -> throw RuntimeException("Invalid color id $index")
}

private fun Context.getColors(id: Int) = resources.getIntArray(id).toCollection(ArrayList())

@Composable
@MyDevices
private fun LineColorPickerAlertDialogPreview() {
    AppThemeSurface {
        LineColorPickerAlertDialog(alertDialogState = rememberAlertDialogState(),
            color = R.color.color_primary,
            isPrimaryColorPicker = true,
            onActiveColorChange = {}
        ) { _, _ -> }
    }
}
