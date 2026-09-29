package com.example.game.entity

data class Treasure(
    val id: Int,
    var x: Float,
    var y: Float,
    var vy: Float = 220f, // Falling speed
    val type: PowerUpType,
    val size: Float = 36f,
    var isCollected: Boolean = false,
    var isExpired: Boolean = false,
    var pulse: Float = 0f
) {
    val left: Float get() = x - size / 2f
    val right: Float get() = x + size / 2f
    val top: Float get() = y - size / 2f
    val bottom: Float get() = y + size / 2f

    fun step(dt: Float) {
        y += vy * dt
        pulse = (pulse + dt * 4f) % (2f * Math.PI.toFloat())
    }
}
