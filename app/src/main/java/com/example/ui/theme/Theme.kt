package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CodeForgeBlue,
    onPrimary = Color.White,
    primaryContainer = CodeForgePurple,
    onPrimaryContainer = Color.White,
    secondary = CodeForgeCyan,
    onSecondary = Color.Black,
    tertiary = CodeForgePink,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder
)

@Composable
fun CodeForgeTheme(
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (amoled) {
        DarkColorScheme.copy(
            background = AmoledBg,
            surface = AmoledSurface,
            surfaceVariant = AmoledBorder,
            outline = Color(0xFF2A2A32)
        )
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
