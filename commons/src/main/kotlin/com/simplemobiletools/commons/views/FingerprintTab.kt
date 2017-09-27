package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_fingerprint.view.*

class FingerprintTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), SecurityTab {
    lateinit var hashListener: HashListener

    override fun onFinishInflate() {
        super.onFinishInflate()
        val textColor = context.baseConfig.textColor
        context.updateTextColors(fingerprint_lock_holder)
        fingerprint_image.colorFilter = PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_IN)
    }

    override fun initTab(requiredHash: String, listener: HashListener) {
        hashListener = listener
    }

    override fun visibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            Reprint.authenticate(object : AuthenticationListener {
                override fun onSuccess(moduleTag: Int) {
                    hashListener.receivedHash("", PROTECTION_FINGERPRINT)
                }

                override fun onFailure(failureReason: AuthenticationFailureReason, fatal: Boolean, errorMessage: CharSequence?, moduleTag: Int, errorCode: Int) {
                    if (failureReason == AuthenticationFailureReason.AUTHENTICATION_FAILED) {
                        context.toast(R.string.authentication_failed)
                    }
                }
            })
        } else {
            Reprint.cancelAuthentication()
        }
    }
}
