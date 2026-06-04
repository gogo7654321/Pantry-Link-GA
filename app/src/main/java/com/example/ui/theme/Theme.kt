package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrganicGreenDark,
    secondary = MutedSlateBlueDark,
    tertiary = WarmBeigeDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = DarkSurface,
    primaryContainer = OrganicGreenPrimary,
    onPrimaryContainer = TextLight,
    secondaryContainer = MutedSlateBlue,
    onSecondaryContainer = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = OrganicGreenPrimary,
    secondary = MutedSlateBlue,
    tertiary = WarmBeige,
    background = CalmBackground,
    surface = SoftSurface,
    onPrimary = SoftSurface,
    onSecondary = SoftSurface,
    onTertiary = SoftSurface,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = WarmBeigeContainer,
    onSurfaceVariant = TextDark,
    primaryContainer = OrganicGreenContainer,
    onPrimaryContainer = OrganicGreenPrimary,
    secondaryContainer = MutedSlateBlueContainer,
    onSecondaryContainer = MutedSlateBlue
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamicColor to ensure our custom beautiful organic color palette is shown by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
