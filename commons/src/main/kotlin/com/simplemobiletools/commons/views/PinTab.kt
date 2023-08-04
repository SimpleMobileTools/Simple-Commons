package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.biometric.auth.AuthPromptHost
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.TabPinBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PROTECTION_PIN
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.interfaces.SecurityTab
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class PinTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), SecurityTab {
    private var hash = ""
    private var requiredHash = ""
    private var pin = ""
    lateinit var hashListener: HashListener

    private lateinit var binding: TabPinBinding

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = TabPinBinding.bind(this)

        context.updateTextColors(binding.pinLockHolder)

        binding.pin0.setOnClickListener { addNumber("0") }
        binding.pin1.setOnClickListener { addNumber("1") }
        binding.pin2.setOnClickListener { addNumber("2") }
        binding.pin3.setOnClickListener { addNumber("3") }
        binding.pin4.setOnClickListener { addNumber("4") }
        binding.pin5.setOnClickListener { addNumber("5") }
        binding.pin6.setOnClickListener { addNumber("6") }
        binding.pin7.setOnClickListener { addNumber("7") }
        binding.pin8.setOnClickListener { addNumber("8") }
        binding.pin9.setOnClickListener { addNumber("9") }
        binding.pinC.setOnClickListener { clear() }
        binding.pinOk.setOnClickListener { confirmPIN() }
        binding.pinOk.applyColorFilter(context.getProperTextColor())
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.requiredHash = requiredHash
        hash = requiredHash
        hashListener = listener
    }

    private fun addNumber(number: String) {
        if (pin.length < 10) {
            pin += number
            updatePinCode()
        }
        performHapticFeedback()
    }

    private fun clear() {
        if (pin.isNotEmpty()) {
            pin = pin.substring(0, pin.length - 1)
            updatePinCode()
        }
        performHapticFeedback()
    }

    private fun confirmPIN() {
        val newHash = getHashedPin()
        if (pin.isEmpty()) {
            context.toast(R.string.please_enter_pin)
        } else if (hash.isEmpty()) {
            hash = newHash
            resetPin()
            binding.pinLockTitle.setText(R.string.repeat_pin)
        } else if (hash == newHash) {
            hashListener.receivedHash(hash, PROTECTION_PIN)
        } else {
            resetPin()
            context.toast(R.string.wrong_pin)
            if (requiredHash.isEmpty()) {
                hash = ""
                binding.pinLockTitle.setText(R.string.enter_pin)
            }
        }
        performHapticFeedback()
    }

    private fun resetPin() {
        pin = ""
        binding.pinLockCurrentPin.text = ""
    }

    private fun updatePinCode() {
        binding.pinLockCurrentPin.text = "*".repeat(pin.length)
        if (hash.isNotEmpty() && hash == getHashedPin()) {
            hashListener.receivedHash(hash, PROTECTION_PIN)
        }
    }

    private fun getHashedPin(): String {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(pin.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        val bigInteger = BigInteger(1, digest)
        return String.format(Locale.getDefault(), "%0${digest.size * 2}x", bigInteger).lowercase(Locale.getDefault())
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
