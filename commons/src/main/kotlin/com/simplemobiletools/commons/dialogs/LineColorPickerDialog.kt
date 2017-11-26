package com.simplemobiletools.commons.dialogs

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.WindowManager
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.interfaces.LineColorPickerListener
import kotlinx.android.synthetic.main.dialog_line_color_picker.view.*
import java.util.*

class LineColorPickerDialog(val context: Context, val callback: (color: Int) -> Unit) {
    private val PRIMARY_COLORS_COUNT = 19
    private val DEFAULT_PRIMARY_COLOR_INDEX = 14
    private val DEFAULT_SECONDARY_COLOR_INDEX = 6

    private var dialog: AlertDialog? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_line_color_picker, null).apply {
            val indexes = getColorIndexes(context.baseConfig.primaryColor)

            primary_line_color_picker.updateColors(getColors(R.array.md_primary_colors), indexes.first)
            primary_line_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)
                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
            }

            secondary_line_color_picker.updateColors(getColorsForIndex(indexes.first), indexes.second)
            secondary_line_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int) {
                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
            }
        }

        dialog = AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            context.setupDialogStuff(view, this)
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        for (i in 0 until PRIMARY_COLORS_COUNT) {
            val colors = getColorsForIndex(i)
            val size = colors.size
            (0 until size).filter { color == colors[it] }
                    .forEach { return Pair(i, it) }
        }
        return Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)
    }

    private fun dialogConfirmed() {

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
        17 -> getColors(R.array.md_greys)
        18 -> getColors(R.array.md_blue_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors(id: Int) = context.resources.getIntArray(id).toCollection(ArrayList())
}
