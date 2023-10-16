package com.simplemobiletools.commons.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class SimpleListItem(val id: Int, val textRes: Int, val imageRes: Int? = null, val selected: Boolean = false) : Parcelable {

    companion object {
        fun areItemsTheSame(old: SimpleListItem, new: SimpleListItem): Boolean {
            return old.id == new.id
        }

        fun areContentsTheSame(old: SimpleListItem, new: SimpleListItem): Boolean {
            return old.imageRes == new.imageRes && old.textRes == new.textRes && old.selected == new.selected
        }
    }
}
