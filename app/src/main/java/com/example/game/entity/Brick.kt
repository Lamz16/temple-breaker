package com.example.game.entity

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

enum class BrickType(
    val maxDurability: Int,
    val scoreValue: Int,
    val primaryColor: Color,
    val highlightColor: Color,
    val shadowColor: Color,
    val label: String
) {
    NORMAL(
        maxDurability = 1,
        scoreValue = 100,
        primaryColor = Color(0xFFD4A359), // Sandstone Amber
        highlightColor = Color(0xFFFFE082),
        shadowColor = Color(0xFF795548),
        label = "Bata Kuil"
    ),
    STRONG(
        maxDurability = 2,
        scoreValue = 200,
        primaryColor = Color(0xFF00ACC1), // Lapis Lazuli Gem
        highlightColor = Color(0xFF80DEEA),
        shadowColor = Color(0xFF006064),
        label = "Batu Safir"
    ),
    ANCIENT(
        maxDurability = 3,
        scoreValue = 300,
        primaryColor = Color(0xFFD81B60), // Ruby Rune Stone
        highlightColor = Color(0xFFFF80AB),
        shadowColor = Color(0xFF880E4F),
        label = "Batu Kuno"
    )
}

data class Brick(
    val id: Int,
    val row: Int,
    val col: Int,
    var rect: Rect,
    val type: BrickType,
    var currentDurability: Int = type.maxDurability,
    var isDestroyed: Boolean = false,
    val containsPowerUp: PowerUpType? = null
) {
    val damageFraction: Float
        get() = (type.maxDurability - currentDurability).toFloat() / type.maxDurability.toFloat()

    fun hit(): Boolean {
        if (isDestroyed) return false
        currentDurability--
        if (currentDurability <= 0) {
            isDestroyed = true
            return true // destroyed
        }
        return false // still intact but damaged
    }
}
