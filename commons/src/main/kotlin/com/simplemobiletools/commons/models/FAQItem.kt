package com.simplemobiletools.commons.models

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class FAQItem(val title: Any, val text: Any) : Serializable {
    companion object {
        private const val serialVersionUID = -6553345863512345L
    }
}
