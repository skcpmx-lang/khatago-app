package com.shohan.khatago.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = KhataPrimary,
    onPrimary = KhataSurface,
    primaryContainer = KhataPrimaryContainer,
    onPrimaryContainer = KhataPrimary,
    background = KhataBackground,
    onBackground = KhataTextPrimary,
    surface = KhataSurface,
    onSurface = KhataTextPrimary,
    secondary = KhataBlue,
    onSecondary = KhataSurface,
    error = KhataNegative,
    onError = KhataSurface,
    outline = KhataOutline
)

@Composable
fun KhataGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = KhataTypography,
        content = content
    )
}
