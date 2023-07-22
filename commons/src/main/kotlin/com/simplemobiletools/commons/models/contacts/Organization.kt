package com.simplemobiletools.commons.models.contacts

import kotlinx.serialization.Serializable

@Serializable
data class Organization(var company: String, var jobPosition: String) {
    fun isEmpty() = company.isEmpty() && jobPosition.isEmpty()

    fun isNotEmpty() = !isEmpty()
}
