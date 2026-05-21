package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Frosted Glass Theme
val GlassPrimary = Color(0xFFB1C5FF) // fresh frosted light blue
val GlassSecondary = Color(0xFFD0BCFF) // dream pastel violet-lavender
val GlassTertiary = Color(0xFFFFB1C8) // warm bedtime rose-pink

val GlassBackground = Color(0xFF0F1113) // sleek charcoal-black background
val GlassSurface = Color(0x0EFFFFFF) // highly-translucent white glass background (~5.5%)
val GlassSurfaceVariant = Color(0x1CFFFFFF) // slightly thicker translucent glass (~11%)

val GlassOnPrimary = Color(0xFF002553)
val GlassOnSecondary = Color(0xFF25114D)
val GlassOnTertiary = Color(0xFF42001B)
val GlassOnBackground = Color(0xFFE2E2E6)
val GlassOnSurface = Color(0xFFE2E2E6)

// Glass highlighting border brush is a crucial visual asset in glassmorphism to look physical
fun getGlassBorderBrush() = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.16f),
        Color.White.copy(alpha = 0.03f)
    )
)

// Glass filling card background brush mimics dynamic lighting refraction
fun getGlassBackgroundBrush() = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.06f),
        Color.White.copy(alpha = 0.02f)
    )
)

