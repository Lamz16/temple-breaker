package com.lamz.nebulabreaker.game.entity

import androidx.compose.ui.graphics.Color

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var lifetime: Float = 0f,
    val maxLifetime: Float = 0.6f
) {
    val alpha: Float
        get() = (1f - (lifetime / maxLifetime)).coerceIn(0f, 1f)

    val isDead: Boolean
        get() = lifetime >= maxLifetime

    fun step(dt: Float) {
        x += vx * dt
        y += vy * dt
        vy += 300f * dt // slight gravity
        vx *= (1f - 0.9f * dt) // air drag
        lifetime += dt
    }
}
