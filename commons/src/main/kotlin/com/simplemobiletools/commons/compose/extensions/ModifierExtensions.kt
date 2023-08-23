package com.simplemobiletools.commons.compose.extensions

import androidx.compose.ui.Modifier

inline fun Modifier.ifTrue(predicate: Boolean, builder: () -> Modifier) =
    then(if (predicate) builder() else Modifier)
