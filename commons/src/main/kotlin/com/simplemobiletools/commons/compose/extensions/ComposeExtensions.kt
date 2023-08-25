package com.simplemobiletools.commons.compose.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.simplemobiletools.commons.compose.theme.isLitWell
import com.simplemobiletools.commons.extensions.darkenColor

fun Context.getActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> getActivity()
    }
}

fun Context.getComponentActivity(): ComponentActivity = getActivity() as ComponentActivity

@Composable
fun AdjustNavigationBarColors(canScroll: Boolean?) {
    val componentActivity = (LocalContext.current.getComponentActivity())
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isSurfaceLitWell = MaterialTheme.colorScheme.surface.isLitWell()
    val navigationBarColor = when (canScroll) {
        true -> Color(MaterialTheme.colorScheme.surface.toArgb().darkenColor()).copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val insetController = rememberWindowInsetsController()
    DisposableEffect(!isSystemInDarkTheme, navigationBarColor) {
        componentActivity.enableEdgeToEdge(
            navigationBarStyle = if (isSystemInDarkTheme) SystemBarStyle.dark(navigationBarColor.toArgb()) else SystemBarStyle.light(
                navigationBarColor.toArgb(),
                navigationBarColor.toArgb()
            )
        )
        insetController.isAppearanceLightNavigationBars = isSurfaceLitWell
        onDispose {}
    }
}

@Composable
fun rememberWindowInsetsController(): WindowInsetsControllerCompat {
    val componentActivity = LocalContext.current.getComponentActivity()
    val view = LocalView.current
    return remember { WindowCompat.getInsetsController(componentActivity.window, view) }
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
