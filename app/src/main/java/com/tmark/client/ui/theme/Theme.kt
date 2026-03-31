package com.tmark.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TMarkColorScheme = lightColorScheme(
    primary          = TMarkRed,
    onPrimary        = White,
    primaryContainer = TMarkRedDark,
    secondary        = TMarkBlackSoft,
    onSecondary      = White,
    background       = TMarkOffWhite,
    onBackground     = TMarkBlack,
    surface          = White,
    onSurface        = TMarkBlack,
    surfaceVariant   = TMarkSurface,
    onSurfaceVariant = TMarkMuted,
    outline          = TMarkBorder,
    error            = TMarkRed,
    onError          = White,
)

@Composable
fun TMarkClientTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TMarkColorScheme,
        typography  = TMarkTypography,
        content     = content
    )
}
