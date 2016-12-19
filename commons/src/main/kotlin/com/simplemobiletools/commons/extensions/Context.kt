package com.simplemobiletools.commons.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.helpers.PREFS_KEY
import com.simplemobiletools.commons.views.MyAppCompatSpinner
import com.simplemobiletools.commons.views.MyAppCompatCheckbox
import com.simplemobiletools.commons.views.MyCompatRadioButton
import com.simplemobiletools.commons.views.MySwitchCompat
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

fun Context.updateTextColors(viewGroup: ViewGroup, color: Int = 0) {
    val baseConfig = BaseConfig.newInstance(this)
    val textColor = if (color == 0) baseConfig.textColor else color
    val cnt = viewGroup.childCount
    (0..cnt - 1)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                if (it is AppCompatEditText) {
                    it.background.mutate().setColorFilter(baseConfig.primaryColor, PorterDuff.Mode.SRC_ATOP)
                    it.setTextColor(textColor)
                } else if (it is AppCompatTextView) {
                    it.setTextColor(textColor)
                    it.setLinkTextColor(baseConfig.primaryColor)
                } else if (it is MyAppCompatSpinner) {
                    it.setColors(textColor, baseConfig.backgroundColor)
                } else if (it is MySwitchCompat) {
                    it.setTextColor(textColor)
                    it.setColor(baseConfig.primaryColor)
                } else if (it is MyCompatRadioButton) {
                    it.setTextColor(textColor)
                    it.setColor(baseConfig.primaryColor)
                } else if (it is MyAppCompatCheckbox) {
                    it.setTextColor(textColor)
                    it.setColor(baseConfig.primaryColor)
                } else if (it is ViewGroup) {
                    updateTextColors(it, textColor)
                }
            }
}

fun Context.setupDialogStuff(view: View, dialog: AlertDialog, titleId: Int = 0) {
    if (view is ViewGroup)
        updateTextColors(view)
    else if (view is AppCompatTextView) {
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
            textSize = resources.getDimension(R.dimen.smaller_text_size)
        }
    }

    dialog.apply {
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
