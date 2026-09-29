package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TempleColorScheme = darkColorScheme(
    primary = TempleGold,
    onPrimary = Color(0xFF2E1C00),
    primaryContainer = TempleGoldDark,
    onPrimaryContainer = TempleGoldLight,
    secondary = MysticCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = MysticCyanDark,
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = SacredAmber,
    onTertiary = Color(0xFF3E1C00),
    background = TempleDarkBg,
    onBackground = TextLight,
    surface = TempleSurface,
    onSurface = TextLight,
    surfaceVariant = TempleSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = TempleBorder
)

@Composable
fun TempleBreakerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TempleColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TempleBreakerTheme(content = content)
}
