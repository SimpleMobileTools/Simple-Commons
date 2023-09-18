package com.simplemobiletools.commons.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import androidx.biometric.auth.AuthPromptHost
import androidx.core.os.postDelayed
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.TabPatternBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PROTECTION_PATTERN
import com.simplemobiletools.commons.interfaces.BaseSecurityTab
import com.simplemobiletools.commons.interfaces.HashListener

class PatternTab(context: Context, attrs: AttributeSet) : BaseSecurityTab(context, attrs) {
    private var scrollView: MyScrollView? = null

    private lateinit var binding: TabPatternBinding

    override val protectionType = PROTECTION_PATTERN
    override val defaultTextRes = R.string.insert_pattern
    override val wrongTextRes = R.string.wrong_pattern
    override val titleTextView: TextView
        get() = binding.patternLockTitle

    @SuppressLint("ClickableViewAccessibility")
    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = TabPatternBinding.bind(this)

        val textColor = context.getProperTextColor()
        context.updateTextColors(binding.patternLockHolder)

        binding.patternLockView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> scrollView?.isScrollable = false
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> scrollView?.isScrollable = true
            }
            false
        }

        binding.patternLockView.correctStateColor = context.getProperPrimaryColor()
        binding.patternLockView.normalStateColor = textColor
        binding.patternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                receivedHash(PatternLockUtils.patternToSha1(binding.patternLockView, pattern))
            }

            override fun onCleared() {}

            override fun onStarted() {}

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) {}
        })

        binding.patternLockIcon.applyColorFilter(textColor)
        maybeShowCountdown()
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.requiredHash = requiredHash
        this.scrollView = scrollView
        computedHash = requiredHash
        hashListener = listener
    }

    override fun onLockedOutChange(lockedOut: Boolean) {
        binding.patternLockView.isInputEnabled = !lockedOut
    }

    private fun receivedHash(newHash: String) {
        if (isLockedOut()) {
            performHapticFeedback()
            return
        }

        when {
            computedHash.isEmpty() -> {
                computedHash = newHash
                binding.patternLockView.clearPattern()
                binding.patternLockTitle.setText(R.string.repeat_pattern)
            }

            computedHash == newHash -> {
                binding.patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                onCorrectPassword()
            }

            else -> {
                onIncorrectPassword()
                binding.patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG)
                Handler().postDelayed(delayInMillis = 1000) {
                    binding.patternLockView.clearPattern()
                    if (requiredHash.isEmpty()) {
                        computedHash = ""
                    }
                }
            }
        }
    }
}
