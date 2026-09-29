package com.example.game.collision

import androidx.compose.ui.geometry.Rect
import com.example.game.entity.Ball
import com.example.game.entity.Brick
import com.example.game.entity.Paddle
import com.example.game.entity.Treasure
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

object CollisionSystem {

    /**
     * Resolves collision between ball and arena boundaries.
     * Returns true if the ball fell below the bottom edge (lost ball).
     */
    fun checkBoundaryCollision(ball: Ball, arenaWidth: Float, arenaHeight: Float): Boolean {
        // Left wall
        if (ball.x - ball.radius < 0f) {
            ball.x = ball.radius
            ball.vx = abs(ball.vx)
        }
        // Right wall
        if (ball.x + ball.radius > arenaWidth) {
            ball.x = arenaWidth - ball.radius
            ball.vx = -abs(ball.vx)
        }
        // Top ceiling
        if (ball.y - ball.radius < 0f) {
            ball.y = ball.radius
            ball.vy = abs(ball.vy)
        }
        // Bottom boundary
        return ball.y - ball.radius > arenaHeight
    }

    /**
     * Resolves collision between ball and player paddle with dynamic angle reflection.
     * Angle depends on the point of impact on the paddle.
     */
    fun checkPaddleCollision(ball: Ball, paddle: Paddle): Boolean {
        val paddleRect = Rect(
            left = paddle.left,
            top = paddle.top,
            right = paddle.right,
            bottom = paddle.bottom
        )

        // Only collide if ball is moving downwards or roughly touching paddle top
        if (ball.vy <= 0 && ball.y > paddle.top) return false

        val closestX = ball.x.coerceIn(paddleRect.left, paddleRect.right)
        val closestY = ball.y.coerceIn(paddleRect.top, paddleRect.bottom)

        val dx = ball.x - closestX
        val dy = ball.y - closestY
        val distSq = dx * dx + dy * dy

        if (distSq <= ball.radius * ball.radius) {
            // Position correction above paddle
            ball.y = paddle.top - ball.radius - 1f

            // Calculate hit offset relative to center (-1.0 = left edge, 0 = center, 1.0 = right edge)
            val halfWidth = (paddle.currentWidth / 2f).coerceAtLeast(1f)
            val offset = ((ball.x - paddle.centerX) / halfWidth).coerceIn(-1f, 1f)

            // Max reflection angle: 62 degrees (radians: ~1.08)
            val maxAngleRadians = 1.082f
            val bounceAngle = offset * maxAngleRadians

            val currentSpeed = ball.speed().coerceAtLeast(420f)
            ball.vx = currentSpeed * sin(bounceAngle)
            ball.vy = -abs(currentSpeed * cos(bounceAngle))
            return true
        }
        return false
    }

    /**
     * Resolves collision between ball and brick.
     * Corrects penetration and reflects velocity.
     * Returns true if collision occurred.
     */
    fun checkBrickCollision(ball: Ball, brick: Brick): Boolean {
        if (brick.isDestroyed) return false

        val rect = brick.rect
        val closestX = ball.x.coerceIn(rect.left, rect.right)
        val closestY = ball.y.coerceIn(rect.top, rect.bottom)

        val dx = ball.x - closestX
        val dy = ball.y - closestY
        val distSq = dx * dx + dy * dy

        if (distSq <= ball.radius * ball.radius) {
            val dist = sqrt(distSq)

            if (dist > 0.001f) {
                val penetration = ball.radius - dist
                val nx = dx / dist
                val ny = dy / dist

                // Push out of collision
                ball.x += nx * penetration
                ball.y += ny * penetration

                // Reflect along dominant axis
                if (abs(dx) > abs(dy)) {
                    ball.vx = if (dx > 0) abs(ball.vx) else -abs(ball.vx)
                } else {
                    ball.vy = if (dy > 0) abs(ball.vy) else -abs(ball.vy)
                }
            } else {
                // Ball center was inside brick; push out to nearest edge
                val distLeft = abs(ball.x - rect.left)
                val distRight = abs(ball.x - rect.right)
                val distTop = abs(ball.y - rect.top)
                val distBottom = abs(ball.y - rect.bottom)

                val minDistance = minOf(distLeft, distRight, distTop, distBottom)
                when (minDistance) {
                    distLeft -> {
                        ball.x = rect.left - ball.radius
                        ball.vx = -abs(ball.vx)
                    }
                    distRight -> {
                        ball.x = rect.right + ball.radius
                        ball.vx = abs(ball.vx)
                    }
                    distTop -> {
                        ball.y = rect.top - ball.radius
                        ball.vy = -abs(ball.vy)
                    }
                    else -> {
                        ball.y = rect.bottom + ball.radius
                        ball.vy = abs(ball.vy)
                    }
                }
            }
            return true
        }
        return false
    }

    /**
     * Checks if a falling treasure intersects the player's paddle.
     */
    fun checkTreasurePaddleCollision(treasure: Treasure, paddle: Paddle): Boolean {
        if (treasure.isCollected || treasure.isExpired) return false

        val treasureOverlapX = treasure.right >= paddle.left && treasure.left <= paddle.right
        val treasureOverlapY = treasure.bottom >= paddle.top && treasure.top <= paddle.bottom

        return treasureOverlapX && treasureOverlapY
    }
}
