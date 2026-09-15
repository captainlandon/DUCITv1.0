package com.ducit.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = DucitBackground,
    surface = DucitSurface,
    surfaceVariant = DucitSurfaceVariant,
    onBackground = DucitOnBackground,
    onSurface = DucitOnBackground,
    primary = DucitAccent,
    onPrimary = DucitBackground,
    secondaryContainer = DucitAccentMuted,
    outline = DucitDivider,
)

// The launcher shell always renders dark, matching the reference UI; the
// light scheme exists only so future surfaces (Memory Inspector, Trust
// Receipt) that DO respect system theme have somewhere to inherit from.
private val LightColors = lightColorScheme(
    primary = DucitAccent,
)

@Composable
fun DucitTheme(
    useDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme || isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = DucitTypography,
        content = content,
    )
}
