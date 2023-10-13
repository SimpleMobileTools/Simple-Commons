package com.simplemobiletools.commons.extensions

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

context (SharedPreferences)
fun <T> sharedPreferencesCallback(
    sendOnCollect: Boolean = false,
    value: () -> T?,
): Flow<T?> = callbackFlow {
    val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(value())
        }

    if (sendOnCollect) {
        trySend(value())
    }
    registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    awaitClose { unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener) }
}
