package com.simplemobiletools.commons.helpers

public inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long) = this.map { selector(it) }.sum()
