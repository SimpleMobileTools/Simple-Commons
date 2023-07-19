package com.simplemobiletools.commons.models.contacts

import kotlinx.serialization.Serializable

@Serializable
data class Address(var value: String, var type: Int, var label: String)
