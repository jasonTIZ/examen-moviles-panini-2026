package com.example.exame2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FifaGold,
    onPrimary = FifaNavy,
    primaryContainer = FifaNavy,
    onPrimaryContainer = FifaNavyContainer,
    secondary = FifaNavyLight,
    tertiary = FifaRed
)

private val LightColorScheme = lightColorScheme(
    primary = FifaNavy,
    onPrimary = Color.White,
    primaryContainer = FifaNavyContainer,
    onPrimaryContainer = FifaNavy,
    secondary = FifaNavyLight,
    onSecondary = Color.White,
    tertiary = FifaRed,
    onTertiary = Color.White
)

@Composable
fun Examen2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
