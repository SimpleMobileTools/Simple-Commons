package com.simplemobiletools.commons.helpers

import androidx.recyclerview.selection.ItemDetailsLookup

class StringItemDetails(val itemPosition: Int, val string: String) : ItemDetailsLookup.ItemDetails<String>() {
    override fun getSelectionKey() = string

    override fun getPosition() = itemPosition
}
