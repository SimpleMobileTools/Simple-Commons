package com.simplemobiletools.commons.compose.extensions

import androidx.compose.ui.Modifier

inline fun Modifier.ifTrue(predicate: Boolean, builder: () -> Modifier) =
    then(if (predicate) builder() else Modifier)

inline fun Modifier.ifFalse(predicate: Boolean, builder: () -> Modifier) =
    then(if (!predicate) builder() else Modifier)

inline infix fun (() -> Unit).andThen(crossinline function: () -> Unit): () -> Unit = {
    this()
    function()
}

inline fun (() -> Unit).andThen(
    crossinline function: () -> Unit,
    crossinline function2: () -> Unit
): () -> Unit = {
    this()
    function()
    function2()
}

inline fun (() -> Unit).andThen(
    crossinline function: () -> Unit,
    crossinline function2: () -> Unit,
    crossinline function3: () -> Unit,
): () -> Unit = {
    this()
    function()
    function2()
    function3()
}
