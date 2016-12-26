package com.simplemobiletools.commons.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.helpers.PREFS_KEY
import com.simplemobiletools.commons.views.*
import kotlinx.android.synthetic.main.dialog_title.view.*

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, id, length).show()

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, msg, length).show()

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

fun Context.getDialogBackgroundColor(backgroundColor: Int): Drawable {
    return ColorDrawable(if (backgroundColor.getContrastColor() == Color.WHITE) {
        resources.getColor(R.color.dark_dialog_background)
    } else {
        backgroundColor
    })
}

fun Context.updateTextColors(viewGroup: ViewGroup, tmpTextColor: Int = 0, tmpAccentColor: Int = 0) {
    val baseConfig = BaseConfig.newInstance(this)
    val textColor = if (tmpTextColor == 0) baseConfig.textColor else tmpTextColor
    val accentColor = if (tmpAccentColor == 0) baseConfig.primaryColor else tmpAccentColor
    val backgroundColor = baseConfig.backgroundColor
    val cnt = viewGroup.childCount
    (0..cnt - 1)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                if (it is MyTextView) {
                    it.setColors(textColor, accentColor, backgroundColor)
                } else if (it is MyAppCompatSpinner) {
                    it.setColors(textColor, accentColor, backgroundColor)
                } else if (it is MySwitchCompat) {
                    it.setColors(textColor, accentColor, backgroundColor)
                } else if (it is MyCompatRadioButton) {
                    it.setColors(textColor, accentColor, backgroundColor)
                } else if (it is MyAppCompatCheckbox) {
                    it.setColors(textColor, accentColor, backgroundColor)
                } else if (it is MyEditText) {
                    it.setColors(textColor, accentColor, backgroundColor)
                } else if (it is ViewGroup) {
                    updateTextColors(it, textColor, accentColor)
                }
            }
}

fun Context.setupDialogStuff(view: View, dialog: AlertDialog, titleId: Int = 0) {
    if (view is ViewGroup)
        updateTextColors(view)
    else if (view is MyTextView) {
        view.setTextColor(BaseConfig.newInstance(this).textColor)
    }

    val baseConfig = BaseConfig.newInstance(this)
    val primaryColor = baseConfig.primaryColor
    var title: TextView? = null
    if (titleId != 0) {
        title = LayoutInflater.from(this).inflate(R.layout.dialog_title, null) as TextView
        title.dialog_title_textview.apply {
            setText(titleId)
            setTextColor(baseConfig.textColor)
        }
    }

    dialog.apply {
        setView(view)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCustomTitle(title)
        setCanceledOnTouchOutside(true)
        show()
        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primaryColor)
        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(primaryColor)
        getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(primaryColor)
        window.setBackgroundDrawable(context.getDialogBackgroundColor(baseConfig.backgroundColor))
    }
}
