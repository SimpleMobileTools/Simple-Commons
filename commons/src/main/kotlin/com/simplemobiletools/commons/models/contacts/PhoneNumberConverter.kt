package com.simplemobiletools.commons.models.contacts

import androidx.annotation.Keep

// need for hacky parsing of no longer minified PhoneNumber model in Converters.kt
@Keep
data class PhoneNumberConverter(var a: String, var b: Int, var c: String, var d: String, var e: Boolean = false)
