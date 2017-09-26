package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_fingerprint.view.*

class FingerprintTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), SecurityTab {
    private var hash = ""
    private var requiredHash = ""
    lateinit var hashListener: HashListener

    override fun onFinishInflate() {
        super.onFinishInflate()
        val textColor = context.baseConfig.textColor
        context.updateTextColors(fingerprint_lock_holder)
        finterprint_image.colorFilter = PorterDuffColorFilter(textColor, PorterDuff.Mode.SRC_IN)

        Reprint.authenticate(object : AuthenticationListener {
            override fun onSuccess(moduleTag: Int) {

            }

            override fun onFailure(failureReason: AuthenticationFailureReason, fatal: Boolean, errorMessage: CharSequence?, moduleTag: Int, errorCode: Int) {

            }
        })
    }

    override fun initTab(requiredHash: String, listener: HashListener) {
        this.requiredHash = requiredHash
        hash = requiredHash
        hashListener = listener
    }
}
