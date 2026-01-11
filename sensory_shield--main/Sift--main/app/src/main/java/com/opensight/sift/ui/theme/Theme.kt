package com.opensight.sift.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SiftColorScheme = lightColorScheme(
    primary = SiftAccent,
    onPrimary = Color.White,
    primaryContainer = SiftButtonBackground,
    onPrimaryContainer = SiftText,
    secondary = SiftAccent,
    onSecondary = Color.White,
    background = SiftBackground,
    onBackground = SiftText,
    surface = SiftCardBackground,
    onSurface = SiftText,
    surfaceVariant = SiftButtonBackground,
    onSurfaceVariant = SiftText,
)

@Composable
fun SiftTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SiftColorScheme,
        content = content
    )
}
