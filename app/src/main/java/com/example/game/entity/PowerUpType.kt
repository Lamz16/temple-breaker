package com.example.game.entity

import androidx.compose.ui.graphics.Color

enum class PowerUpType(
    val title: String,
    val description: String,
    val durationSeconds: Float,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    SPEED_BOOST(
        title = "Jam Pasir Ajaib",
        description = "Speed Boost",
        durationSeconds = 5.0f,
        primaryColor = Color(0xFFFFB300), // Amber Gold
        secondaryColor = Color(0xFFFF6F00) // Deep Orange
    ),
    MULTI_BALL(
        title = "Kristal Kembar",
        description = "Multi Ball (+2 Bola)",
        durationSeconds = 0f, // Instant permanent addition until lost
        primaryColor = Color(0xFF00E5FF), // Cyan
        secondaryColor = Color(0xFFD500F9) // Purple Magenta
    ),
    PADDLE_EXPAND(
        title = "Papan Pusaka",
        description = "Papan Melebar",
        durationSeconds = 7.0f,
        primaryColor = Color(0xFF00E676), // Jade Emerald
        secondaryColor = Color(0xFFFFD700) // Gold
    )
}
