package com.simplemobiletools.commons.models

import android.telephony.PhoneNumberUtils
import com.simplemobiletools.commons.extensions.normalizePhoneNumber
import com.simplemobiletools.commons.extensions.normalizeString

data class SimpleContact(val rawId: Int, val contactId: Int, var name: String, var photoUri: String, var phoneNumbers: ArrayList<String>) : Comparable<SimpleContact> {
    override fun compareTo(other: SimpleContact): Int {
        val firstString = name.normalizeString()
        val secondString = other.name.normalizeString()

        return if (firstString.firstOrNull()?.isLetter() == true && secondString.firstOrNull()?.isLetter() == false) {
            -1
        } else if (firstString.firstOrNull()?.isLetter() == false && secondString.firstOrNull()?.isLetter() == true) {
            1
        } else {
            if (firstString.isEmpty() && secondString.isNotEmpty()) {
                1
            } else if (firstString.isNotEmpty() && secondString.isEmpty()) {
                -1
            } else {
                firstString.compareTo(secondString, true)
            }
        }
    }

    fun doesContainPhoneNumber(text: String): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.any { phoneNumber ->
                    phoneNumber.contains(text)
                }
            } else {
                phoneNumbers.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                            phoneNumber.contains(text) ||
                            phoneNumber.normalizePhoneNumber().contains(normalizedText) ||
                            phoneNumber.contains(normalizedText)
                }
            }
        } else {
            false
        }
    }
}
