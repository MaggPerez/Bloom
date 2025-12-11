package com.example.bloom.ui.theme

import androidx.compose.ui.graphics.Color

// shadcn/ui official monochrome color palette (OKLCH converted to hex)

// Dark mode colors (from shadcn OKLCH values)
val ShadcnDarkBg = Color(0xFF0A0A0A)          // #0A0A0A - main background (near-black)
val ShadcnDarkCard = Color(0xFF353535)        // oklch(0.205 0 0) - elevated surfaces
val ShadcnDarkMuted = Color(0xFF454545)       // oklch(0.269 0 0) - muted backgrounds
val ShadcnDarkBorder = Color(0xFF3A3A3A)      // oklch(1 0 0 / 10%) - borders

// Foreground colors
val ShadcnDarkFg = Color(0xFFFAFAFA)          // oklch(0.985 0 0) - main text
val ShadcnDarkMutedFg = Color(0xFFB5B5B5)    // oklch(0.708 0 0) - muted text

// Light mode colors (inverse for light theme)
val ShadcnLightBg = Color(0xFFFFFFFF)         // Pure white background
val ShadcnLightCard = Color(0xFFFAFAFA)       // Slightly off-white for cards
val ShadcnLightMuted = Color(0xFFF4F4F5)      // Muted backgrounds
val ShadcnLightBorder = Color(0xFFE4E4E7)     // Light borders

// Light mode foreground
val ShadcnLightFg = Color(0xFF0A0A0A)         // Near-black text
val ShadcnLightMutedFg = Color(0xFF71717A)    // Muted text

// Additional grayscale shades for flexibility
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF4F4F5)
val Gray200 = Color(0xFFE4E4E7)
val Gray300 = Color(0xFFD4D4D8)
val Gray400 = Color(0xFFA1A1AA)
val Gray500 = Color(0xFF71717A)
val Gray600 = Color(0xFF52525B)
val Gray700 = Color(0xFF3F3F46)
val Gray800 = Color(0xFF27272A)
val Gray900 = Color(0xFF18181B)

val Orange = Color(0xFFFFA500) // Standard Orange color