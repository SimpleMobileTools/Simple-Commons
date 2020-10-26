package com.simplemobiletools.commons.extensions

import com.simplemobiletools.commons.helpers.CHOPPED_LIST_DEFAULT_SIZE

// inspired by https://stackoverflow.com/questions/2895342/java-how-can-i-split-an-arraylist-in-multiple-small-arraylists/2895365#2895365
fun <T> MutableList<T>.getChoppedList(chunkSize: Int = CHOPPED_LIST_DEFAULT_SIZE): ArrayList<ArrayList<T>> {
    val parts = ArrayList<ArrayList<T>>()
    val listSize = this.size
    var i = 0
    while (i < listSize) {
        val newList = subList(i, Math.min(listSize, i + chunkSize)).toMutableList() as ArrayList<T>
        parts.add(newList)
        i += chunkSize
    }
    return parts
}
