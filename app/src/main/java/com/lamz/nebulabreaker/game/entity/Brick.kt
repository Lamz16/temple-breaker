package com.lamz.nebulabreaker.game.entity

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
        primaryColor = Color(0xFF8998E8),
        highlightColor = Color(0xFFC9D3FF),
        shadowColor = Color(0xFF46528F),
        label = "Orbital Panel"
    ),
    STRONG(
        maxDurability = 2,
        scoreValue = 200,
        primaryColor = Color(0xFF5DB9DB),
        highlightColor = Color(0xFFB9EDFF),
        shadowColor = Color(0xFF286583),
        label = "Ion Panel"
    ),
    ANCIENT(
        maxDurability = 3,
        scoreValue = 300,
        primaryColor = Color(0xFFC584D8),
        highlightColor = Color(0xFFF0C5FF),
        shadowColor = Color(0xFF70427F),
        label = "Nebula Core"
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
