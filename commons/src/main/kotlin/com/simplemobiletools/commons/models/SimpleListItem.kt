package com.simplemobiletools.commons.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SimpleListItem(val id: Int, val imageRes: Int?, val textRes: Int) : Parcelable
