package com.example.game.entity

import kotlin.math.hypot

data class Ball(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float = 14f,
    var speedMultiplier: Float = 1.0f,
    var trail: MutableList<Pair<Float, Float>> = mutableListOf()
) {
    fun speed(): Float = hypot(vx, vy)

    fun setSpeed(targetSpeed: Float) {
        val current = hypot(vx, vy)
        if (current > 0.001f) {
            val scale = targetSpeed / current
            vx *= scale
            vy *= scale
        } else {
            vx = 0f
            vy = -targetSpeed
        }
    }

    fun step(dt: Float) {
        x += vx * speedMultiplier * dt
        y += vy * speedMultiplier * dt

        // Keep a short trail for the glowing orb visual
        trail.add(0, Pair(x, y))
        if (trail.size > 5) {
            trail.removeAt(trail.size - 1)
        }
    }
}
