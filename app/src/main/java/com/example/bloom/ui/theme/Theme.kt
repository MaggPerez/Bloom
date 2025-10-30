package com.example.bloom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// shadcn/ui official dark color scheme (using OKLCH-based colors)
private val DarkColorScheme = darkColorScheme(
    primary = ShadcnDarkFg,
    onPrimary = ShadcnDarkCard,
    primaryContainer = ShadcnDarkMuted,
    onPrimaryContainer = ShadcnDarkFg,

    secondary = ShadcnDarkMutedFg,
    onSecondary = ShadcnDarkBg,
    secondaryContainer = ShadcnDarkMuted,
    onSecondaryContainer = ShadcnDarkFg,

    tertiary = Gray400,
    onTertiary = ShadcnDarkBg,
    tertiaryContainer = ShadcnDarkCard,
    onTertiaryContainer = ShadcnDarkFg,

    background = ShadcnDarkBg,              // #252525 - softer than pure black
    onBackground = ShadcnDarkFg,            // #FAFAFA - softer than pure white

    surface = ShadcnDarkCard,               // #353535 - elevated surfaces
    onSurface = ShadcnDarkFg,
    surfaceVariant = ShadcnDarkMuted,       // #454545 - muted surfaces
    onSurfaceVariant = ShadcnDarkMutedFg,

    surfaceTint = Gray500,

    error = Gray400,
    onError = ShadcnDarkBg,
    errorContainer = Gray700,
    onErrorContainer = Gray200,

    outline = ShadcnDarkBorder,             // #3A3A3A - subtle borders
    outlineVariant = ShadcnDarkMuted,

    scrim = ShadcnDarkBg,

    inverseSurface = ShadcnLightBg,
    inverseOnSurface = ShadcnLightFg,
    inversePrimary = Gray700
)

// shadcn/ui official light color scheme
private val LightColorScheme = lightColorScheme(
    primary = ShadcnLightFg,
    onPrimary = ShadcnLightBg,
    primaryContainer = ShadcnLightMuted,
    onPrimaryContainer = ShadcnLightFg,

    secondary = Gray600,
    onSecondary = ShadcnLightBg,
    secondaryContainer = ShadcnLightMuted,
    onSecondaryContainer = ShadcnLightFg,

    tertiary = Gray700,
    onTertiary = ShadcnLightBg,
    tertiaryContainer = Gray200,
    onTertiaryContainer = ShadcnLightFg,

    background = ShadcnLightBg,             // #FFFFFF - pure white
    onBackground = ShadcnLightFg,           // #0A0A0A - near-black text

    surface = ShadcnLightCard,              // #FAFAFA - slightly off-white
    onSurface = ShadcnLightFg,
    surfaceVariant = ShadcnLightMuted,      // #F4F4F5 - muted surfaces
    onSurfaceVariant = ShadcnLightMutedFg,

    surfaceTint = Gray600,

    error = Gray700,
    onError = ShadcnLightBg,
    errorContainer = Gray100,
    onErrorContainer = Gray900,

    outline = ShadcnLightBorder,            // #E4E4E7 - light borders
    outlineVariant = ShadcnLightMuted,

    scrim = ShadcnLightFg,

    inverseSurface = ShadcnDarkBg,
    inverseOnSurface = ShadcnDarkFg,
    inversePrimary = Gray300
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