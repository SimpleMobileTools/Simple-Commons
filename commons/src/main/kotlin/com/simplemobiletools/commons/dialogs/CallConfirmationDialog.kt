package com.simplemobiletools.commons.dialogs

import android.view.animation.AnimationUtils
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.databinding.DialogCallConfirmationBinding
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.setupDialogStuff

class CallConfirmationDialog(val activity: BaseSimpleActivity, val callee: String, private val callback: () -> Unit) {
    private var view = DialogCallConfirmationBinding.inflate(activity.layoutInflater, null, false)

    init {
        view.callConfirmPhone.applyColorFilter(activity.getProperTextColor())
        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel, null)
            .apply {
                val title = String.format(activity.getString(R.string.confirm_calling_person), callee)
                activity.setupDialogStuff(view.root, this, titleText = title) { alertDialog ->
                    view.callConfirmPhone.apply {
                        startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_pulse_animation))
                        setOnClickListener {
                            callback.invoke()
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
