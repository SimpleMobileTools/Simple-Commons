package com.simplemobiletools.commons.extensions

import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

fun AlertDialog.showKeyboard(editText: EditText) {
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    editText.apply {
        requestFocus()
        onGlobalLayout {
            setSelection(text.toString().length)
        }
    }
}

fun AlertDialog.hideKeyboard() {
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}
