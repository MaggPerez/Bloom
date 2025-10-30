package com.example.bloom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// shadcn-inspired dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Gray800,
    onPrimaryContainer = Gray50,

    secondary = Gray400,
    onSecondary = Black,
    secondaryContainer = Gray800,
    onSecondaryContainer = Gray100,

    tertiary = Gray300,
    onTertiary = Black,
    tertiaryContainer = Gray700,
    onTertiaryContainer = Gray100,

    background = Black,
    onBackground = White,

    surface = Gray950,
    onSurface = White,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray300,

    surfaceTint = Gray400,

    error = Gray300,
    onError = Black,
    errorContainer = Gray800,
    onErrorContainer = Gray100,

    outline = Gray800,
    outlineVariant = Gray900,

    scrim = Black,

    inverseSurface = White,
    inverseOnSurface = Black,
    inversePrimary = Gray900
)

// shadcn-inspired light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Gray100,
    onPrimaryContainer = Gray900,

    secondary = Gray600,
    onSecondary = White,
    secondaryContainer = Gray100,
    onSecondaryContainer = Gray900,

    tertiary = Gray700,
    onTertiary = White,
    tertiaryContainer = Gray200,
    onTertiaryContainer = Gray900,

    background = White,
    onBackground = Black,

    surface = Gray50,
    onSurface = Black,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,

    surfaceTint = Gray600,

    error = Gray700,
    onError = White,
    errorContainer = Gray100,
    onErrorContainer = Gray900,

    outline = Gray200,
    outlineVariant = Gray100,

    scrim = Black,

    inverseSurface = Black,
    inverseOnSurface = White,
    inversePrimary = Gray100
)

@Composable
fun BloomTheme(
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