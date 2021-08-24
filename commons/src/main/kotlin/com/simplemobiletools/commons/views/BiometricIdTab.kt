package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_biometric_id.view.*

class BiometricIdTab(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), SecurityTab {
    private lateinit var hashListener: HashListener
    private lateinit var biometricPromptHost: AuthPromptHost

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(biometric_lock_holder)

        open_biometric_dialog.setOnClickListener {
            showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {
        Class2BiometricAuthPrompt.Builder(context.getText(R.string.authenticate), context.getText(R.string.cancel))
            .build()
            .startAuthentication(
                biometricPromptHost,
                object : AuthPromptCallback() {
                    override fun onAuthenticationSucceeded(activity: FragmentActivity?, result: BiometricPrompt.AuthenticationResult) {
                        hashListener.receivedHash("", PROTECTION_FINGERPRINT)
                    }

                    override fun onAuthenticationError(activity: FragmentActivity?, errorCode: Int, errString: CharSequence) {
                        val isCanceledByUser = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        if (!isCanceledByUser) {
                            context.toast(errString.toString())
                        }
                    }

                    override fun onAuthenticationFailed(activity: FragmentActivity?) {
                        context.toast(R.string.authentication_failed)
                    }
                }
            )
    }

    override fun initTab(requiredHash: String, listener: HashListener, scrollView: MyScrollView, biometricPromptHost: AuthPromptHost) {
        this.biometricPromptHost = biometricPromptHost
        hashListener = listener
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
