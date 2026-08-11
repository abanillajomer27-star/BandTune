package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode

// 1. Green & White Color Scheme for System Default (Light Mode)
private val GreenLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = GreenPrimaryLight,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = GreenLightBackground,
    onBackground = TextPrimaryDark,
    surface = GreenLightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = GreenLightCardSurface,
    onSurfaceVariant = TextSecondaryDark
)

// 2. Bright Violet & White Color Scheme for Bright Violet Light Mode
private val BrightVioletColorScheme = lightColorScheme(
    primary = BrightVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = BrightVioletContainer,
    onPrimaryContainer = OnBrightVioletContainer,
    secondary = BrightVioletPrimaryLight,
    onSecondary = Color.White,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = BrightVioletBackground,
    onBackground = TextPrimaryDark,
    surface = BrightVioletSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = BrightVioletCardSurface,
    onSurfaceVariant = TextSecondaryDark
)

// 3. Soft Warm Orange & White Color Scheme for Soft Warm Orange Light Mode
private val WarmOrangeColorScheme = lightColorScheme(
    primary = WarmOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = WarmOrangeContainer,
    onPrimaryContainer = OnWarmOrangeContainer,
    secondary = WarmOrangePrimaryLight,
    onSecondary = Color.White,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = WarmOrangeBackground,
    onBackground = TextPrimaryDark,
    surface = WarmOrangeSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = WarmOrangeCardSurface,
    onSurfaceVariant = TextSecondaryDark
)

@Composable
fun BandTuneTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.SYSTEM -> GreenLightColorScheme
        ThemeMode.LIGHT -> BrightVioletColorScheme
        ThemeMode.WARM_ORANGE -> WarmOrangeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
