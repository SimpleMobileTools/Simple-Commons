package com.simplemobiletools.commons.compose.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.simplemobiletools.commons.compose.theme.isLitWell
import com.simplemobiletools.commons.extensions.darkenColor

fun Context.getActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> getActivity()
    }
}

@Composable
fun TransparentSystemBars() {
    val systemUiController = rememberSystemUiController()
    val isSystemInDarkTheme = isSystemInDarkTheme()

    DisposableEffect(systemUiController, !isSystemInDarkTheme) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !isSystemInDarkTheme
        )

        onDispose {}
    }
}

@Composable
fun AdjustNavigationBarColors(canScroll: Boolean?) {
    val systemUiController = rememberSystemUiController()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isSurfaceLitWell = MaterialTheme.colorScheme.surface.isLitWell()
    val navigationBarColor = when (canScroll) {
        true -> Color(MaterialTheme.colorScheme.surface.toArgb().darkenColor()).copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    DisposableEffect(systemUiController, !isSystemInDarkTheme, navigationBarColor) {
        systemUiController.setNavigationBarColor(navigationBarColor, darkIcons = !isSystemInDarkTheme)
        systemUiController.navigationBarDarkContentEnabled = isSurfaceLitWell
        onDispose {}
    }
}

@Composable
fun <T : Any> onEventValue(event: Lifecycle.Event = Lifecycle.Event.ON_START, value: () -> T): T {
    val rememberLatestUpdateState by rememberUpdatedState(newValue = value)
    var rememberedValue by remember { mutableStateOf(value()) }
    LifecycleEventEffect(event = event) {
        rememberedValue = rememberLatestUpdateState()
    }
    return rememberedValue
}
