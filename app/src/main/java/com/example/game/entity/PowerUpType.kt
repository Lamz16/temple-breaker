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
        title = "Photon Drive",
        description = "Speed Boost",
        durationSeconds = 5.0f,
        primaryColor = Color(0xFFFFB4C8),
        secondaryColor = Color(0xFFE878A3)
    ),
    MULTI_BALL(
        title = "Twin Orbs",
        description = "Multi Ball (+2 Bola)",
        durationSeconds = 0f, // Instant permanent addition until lost
        primaryColor = Color(0xFF82D8FF),
        secondaryColor = Color(0xFFC7AEFF)
    ),
    PADDLE_EXPAND(
        title = "Nova Field",
        description = "Papan Melebar",
        durationSeconds = 7.0f,
        primaryColor = Color(0xFF8BE1C2),
        secondaryColor = Color(0xFFE9E2FF)
    )
}
