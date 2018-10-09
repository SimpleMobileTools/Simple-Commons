package com.simplemobiletools.commons.helpers

import androidx.recyclerview.selection.ItemKeyProvider

class StringListKeyProvider(val items: List<String>, scope: Int = ItemKeyProvider.SCOPE_CACHED) : ItemKeyProvider<String>(scope) {

    override fun getKey(position: Int) = items[position]

    override fun getPosition(key: String) = items.indexOfFirst { it == key }
}
