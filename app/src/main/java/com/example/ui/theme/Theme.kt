package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GlassPrimary,
    secondary = GlassSecondary,
    tertiary = GlassTertiary,
    background = GlassBackground,
    surface = GlassSurface,
    surfaceVariant = GlassSurfaceVariant,
    onPrimary = GlassOnPrimary,
    onSecondary = GlassOnSecondary,
    onTertiary = GlassOnTertiary,
    onBackground = GlassOnBackground,
    onSurface = GlassOnSurface,
    onSurfaceVariant = GlassOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the Sleep Coach app for bedtime ease
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our tailored night palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
