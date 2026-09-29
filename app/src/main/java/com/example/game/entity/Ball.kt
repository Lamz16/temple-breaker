package com.example.game.entity

import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class Ball(
    val id: Int,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 14f,
    var speedMultiplier: Float = 1.0f,
    var isLaunched: Boolean = false,
    val trail: MutableList<Pair<Float, Float>> = mutableListOf()
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

    /**
     * Attaches the ball to the center of the paddle (initial resting state).
     */
    fun attachToPaddle(paddle: Paddle) {
        x = paddle.centerX
        y = paddle.top - radius - 2f
        vx = 0f
        vy = 0f
        isLaunched = false
        trail.clear()
    }

    /**
     * Launches the ball upward from the paddle with a given speed and launch angle.
     * @param launchSpeed speed magnitude in pixels/second.
     * @param angleDegrees angle in degrees from vertical (-45 to 45).
     */
    fun launch(launchSpeed: Float, angleDegrees: Float = 0f) {
        val radians = Math.toRadians(angleDegrees.toDouble()).toFloat()
        vx = launchSpeed * sin(radians)
        vy = -launchSpeed * cos(radians)
        isLaunched = true
    }

    /**
     * Advances ball movement by delta time (seconds).
     */
    fun step(dt: Float) {
        if (!isLaunched) return

        x += vx * speedMultiplier * dt
        y += vy * speedMultiplier * dt

        // Keep a short trail for the glowing orb visual
        trail.add(0, Pair(x, y))
        if (trail.size > 5) {
            trail.removeAt(trail.size - 1)
        }
    }
}
