package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// CODEFORGE PRO / CodeXAs Palette
val CodeForgeBlue = Color(0xFF6A5AF9)
val CodeForgePurple = Color(0xFF8E2DE2)
val CodeForgeCyan = Color(0xFF00E5FF)
val CodeForgePink = Color(0xFFFF2A85)
val CodeForgeGreen = Color(0xFF00E676)
val CodeForgeGold = Color(0xFFFFB300)

// AMOLED & Dark backgrounds
val DarkBg = Color(0xFF0A0D14)
val DarkSurface = Color(0xFF131826)
val DarkSurfaceVariant = Color(0xFF1B2236)
val DarkSurfaceBorder = Color(0xFF28324E)

val AmoledBg = Color(0xFF000000)
val AmoledSurface = Color(0xFF0E0E10)
val AmoledBorder = Color(0xFF1F1F24)

// Text & Content colors
val TextPrimary = Color(0xFFF0F4FF)
val TextSecondary = Color(0xFF9AA7C2)
val TextMuted = Color(0xFF64748B)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(CodeForgeBlue, CodeForgePurple)
)

val GlowGradient = Brush.linearGradient(
    colors = listOf(CodeForgeCyan, CodeForgeBlue, CodeForgePurple)
)

val CardGlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x286A5AF9),
        Color(0x121B2236)
    )
)
