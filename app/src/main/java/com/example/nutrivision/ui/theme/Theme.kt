package com.example.nutrivision.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = PrimaryInk, // Nunca usar branco sobre verde
    secondary = Secondary,
    background = Bone,
    surface = Card,
    onBackground = Ink,
    onSurface = Ink,
    outline = Ink
)

@Composable
fun NutrivisionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
