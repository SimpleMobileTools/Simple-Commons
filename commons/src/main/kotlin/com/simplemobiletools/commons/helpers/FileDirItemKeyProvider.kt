package com.simplemobiletools.commons.helpers

import androidx.recyclerview.selection.ItemKeyProvider
import com.simplemobiletools.commons.models.FileDirItem

class FileDirItemKeyProvider(val items: List<FileDirItem>, scope: Int = ItemKeyProvider.SCOPE_CACHED) : ItemKeyProvider<String>(scope) {

    override fun getKey(position: Int) = items[position].path

    override fun getPosition(key: String) = items.indexOfFirst { it.path == key }
}
