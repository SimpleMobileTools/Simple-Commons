package com.simplemobiletools.commons.interfaces

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.os.postDelayed
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.countdown
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.helpers.DEFAULT_PASSWORD_COUNTDOWN
import com.simplemobiletools.commons.helpers.MAX_PASSWORD_RETRY_COUNT

abstract class BaseSecurityTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), SecurityTab {

    abstract val protectionType: Int
    abstract val defaultTextRes: Int
    abstract val wrongTextRes: Int
    abstract val titleTextView: TextView

    lateinit var computedHash: String
    lateinit var requiredHash: String
    lateinit var hashListener: HashListener

    private val config = context.baseConfig
    private val handler = Handler(Looper.getMainLooper())

    open fun onLockedOutChange(lockedOut: Boolean) {}

    fun isLockedOut() = requiredHash.isNotEmpty() && getCountdown() > 0

    fun onCorrectPassword() {
        resetCountdown()
        handler.postDelayed(delayInMillis = 300) {
            hashListener.receivedHash(computedHash, protectionType)
        }
    }

    fun onIncorrectPassword() {
        config.passwordRetryCount += 1
        if (requiredHash.isNotEmpty() && shouldStartCountdown()) {
            onLockedOutChange(lockedOut = true)
            startCountdown()
        } else {
            updateTitle(context.getString(wrongTextRes), context.getColor(R.color.md_red))
            handler.postDelayed(delayInMillis = 1000) {
                updateTitle(context.getString(defaultTextRes), context.getProperTextColor())
            }
        }
    }

    fun maybeShowCountdown() {
        if (shouldStartCountdown()) {
            startCountdown()
        } else {
            updateCountdownText(0)
        }
    }

    private fun shouldStartCountdown() = config.passwordRetryCount >= MAX_PASSWORD_RETRY_COUNT

    private fun resetCountdown() {
        config.passwordRetryCount = 0
        config.passwordCountdownStartMs = 0
    }

    private fun getCountdown(): Int {
        val retryCount = config.passwordRetryCount
        if (retryCount >= MAX_PASSWORD_RETRY_COUNT) {
            val currentTimeMs = System.currentTimeMillis()
            val countdownStartMs = config.passwordCountdownStartMs

            if (countdownStartMs == 0L) {
                config.passwordCountdownStartMs = currentTimeMs
                return DEFAULT_PASSWORD_COUNTDOWN
            }

            val countdownWaitMs = DEFAULT_PASSWORD_COUNTDOWN * 1000L
            val elapsedMs = currentTimeMs - countdownStartMs

            return if (elapsedMs < countdownWaitMs) {
                val remainingMs = countdownWaitMs - elapsedMs
                val remainingSeconds = remainingMs / 1000L
                remainingSeconds.toInt()
            } else {
                0
            }
        }

        return 0
    }

    private fun startCountdown() {
        getCountdown().countdown(intervalMillis = 1000L) { count ->
            updateCountdownText(count)
            if (count == 0) {
                resetCountdown()
                onLockedOutChange(lockedOut = false)
            }
        }
    }

    private fun updateCountdownText(count: Int) {
        removeCallbacks()
        if (count > 0) {
            updateTitle(context.getString(R.string.too_many_incorrect_attempts, count), context.getColor(R.color.md_red))
        } else {
            updateTitle(context.getString(defaultTextRes), context.getProperTextColor())
        }
    }

    private fun removeCallbacks() = handler.removeCallbacksAndMessages(null)

    private fun updateTitle(text: String, @ColorInt color: Int) {
        titleTextView.text = text
        titleTextView.setTextColor(color)
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}

