package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NaturalPrimary,
    secondary = NaturalSecondary,
    tertiary = NaturalTertiary,
    background = NaturalBgDark,
    surface = NaturalSurfaceDark,
    onPrimary = NaturalTextLight,
    onSecondary = NaturalTextLight,
    onBackground = NaturalTextLight,
    onSurface = NaturalTextLight,
    surfaceVariant = NaturalCardDark,
    onSurfaceVariant = NaturalSubtextLight,
    outline = NaturalBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalPrimary,
    secondary = NaturalSecondary,
    tertiary = NaturalTertiary,
    background = NaturalBgLight,
    surface = NaturalSurfaceLight,
    onPrimary = Color.White,
    onSecondary = NaturalTextDark,
    onBackground = NaturalTextDark,
    onSurface = NaturalTextDark,
    surfaceVariant = NaturalCardLight,
    onSurfaceVariant = NaturalTextDark,
    outline = NaturalBorderLight
)

@Composable
fun MyApplicationTheme(
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
