package com.simplemobiletools.commons.compose.theme.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.simplemobiletools.commons.compose.theme.isInDarkThemeAndSurfaceIsNotLitWell
import com.simplemobiletools.commons.compose.extensions.config

@Stable
sealed class Theme : CommonTheme {

    @Stable
    data class SystemDefaultMaterialYou(
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int
    ) : Theme()

    @Stable
    data class White(
        val accentColor: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int
    ) : Theme()

    @Stable
    data class Dark(
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int
    ) : Theme()

    @Stable
    data class BlackAndWhite(
        val accentColor: Int,
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int
    ) : Theme()

    @Stable
    data class Custom(
        override val primaryColorInt: Int,
        override val backgroundColorInt: Int,
        override val appIconColorInt: Int,
        override val textColorInt: Int
    ) : Theme()


    companion object {
        @Composable
        fun systemDefaultMaterialYou() = SystemDefaultMaterialYou(
            appIconColorInt = LocalContext.current.config.appIconColor,
            primaryColorInt = LocalContext.current.config.primaryColor,
            backgroundColorInt = LocalContext.current.config.backgroundColor,
            textColorInt = (if (isInDarkThemeAndSurfaceIsNotLitWell()) Color.White else Color.Black).toArgb()
        )
    }
}
