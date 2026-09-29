package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import com.example.game.engine.GameEngine
import com.example.game.entity.Ball
import com.example.game.entity.Brick
import com.example.game.entity.Paddle
import com.example.game.entity.Particle
import com.example.game.entity.PowerUpType
import com.example.game.entity.Treasure
import com.example.ui.theme.MysticCyan
import com.example.ui.theme.SacredEmerald
import com.example.ui.theme.TempleGold
import com.example.ui.theme.TempleGoldLight
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    onPaddleMove: (Float) -> Unit,
    onSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_canvas")
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    onSizeChanged(size.width.toFloat(), size.height.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onPaddleMove(offset.x)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onPaddleMove(change.position.x)
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw Ancient Temple background
        drawTempleBackground(width, height)

        // 2. Draw Bricks
        for (brick in engine.bricks) {
            if (!brick.isDestroyed) {
                drawTempleBrick(brick)
            }
        }

        // 3. Draw Falling Treasures
        for (treasure in engine.treasures) {
            drawTreasure(treasure)
        }

        // 4. Draw Particles
        for (particle in engine.particles) {
            drawParticle(particle)
        }

        // 5. Draw Paddle (Sacred Ancient Slab)
        drawTemplePaddle(engine.paddle)

        // 6. Draw Balls (Radiant Glowing Orbs)
        val isSpeedBoostActive = engine.activePowerUps.any { it.type == PowerUpType.SPEED_BOOST }
        for (ball in engine.balls) {
            drawGlowingBall(ball, isSpeedBoostActive)
        }
    }
}

private fun DrawScope.drawTempleBackground(width: Float, height: Float) {
    // Deep stone background with vertical ancient chamber gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0B08),
            Color(0xFF17120D),
            Color(0xFF1E1710),
            Color(0xFF120E0A)
        )
    )
    drawRect(brush = backgroundBrush, size = Size(width, height))

    // Subtle stone chamber column lines at edges
    val columnWidth = width * 0.035f
    val pillarBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2E2218), Color(0xFF15100B))
    )
    drawRect(brush = pillarBrush, topLeft = Offset(0f, 0f), size = Size(columnWidth, height))
    drawRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFF15100B), Color(0xFF2E2218))),
        topLeft = Offset(width - columnWidth, 0f),
        size = Size(columnWidth, height)
    )

    // Subtle bottom torchlight warmth
    val torchGlow = Brush.radialGradient(
        colors = listOf(Color(0x22FF8F00), Color(0x00FF8F00)),
        center = Offset(width / 2f, height),
        radius = height * 0.45f
    )
    drawRect(brush = torchGlow, size = Size(width, height))
}

private fun DrawScope.drawTempleBrick(brick: Brick) {
    val rect = brick.rect
    val cornerRadius = CornerRadius(10f, 10f)

    // Base stone fill
    drawRoundRect(
        color = brick.type.primaryColor,
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        cornerRadius = cornerRadius
    )

    // 3D Bevel highlight on top & left
    val highlightPath = Path().apply {
        moveTo(rect.left, rect.bottom)
        lineTo(rect.left, rect.top)
        lineTo(rect.right, rect.top)
    }
    drawPath(
        path = highlightPath,
        color = brick.type.highlightColor.copy(alpha = 0.85f),
        style = Stroke(width = 3.5f)
    )

    // 3D Bevel shadow on bottom & right
    val shadowPath = Path().apply {
        moveTo(rect.right, rect.top)
        lineTo(rect.right, rect.bottom)
        lineTo(rect.left, rect.bottom)
    }
    drawPath(
        path = shadowPath,
        color = brick.type.shadowColor.copy(alpha = 0.9f),
        style = Stroke(width = 3.5f)
    )

    // Carved ancient symbol or rune inside brick
    val centerX = rect.center.x
    val centerY = rect.center.y
    val symbolSize = (rect.height * 0.32f).coerceAtMost(14f)

    // Small golden rune diamond in center
    val runePath = Path().apply {
        moveTo(centerX, centerY - symbolSize)
        lineTo(centerX + symbolSize, centerY)
        lineTo(centerX, centerY + symbolSize)
        lineTo(centerX - symbolSize, centerY)
        close()
    }
    drawPath(
        path = runePath,
        color = brick.type.highlightColor.copy(alpha = 0.55f),
        style = Stroke(width = 2f)
    )

    // Cracks indicator when durability is damaged
    if (brick.damageFraction > 0f) {
        val crackColor = Color(0xDD120D09)

        // First crack line
        drawLine(
            color = crackColor,
            start = Offset(rect.left + rect.width * 0.3f, rect.top + 2f),
            end = Offset(centerX, centerY),
            strokeWidth = 3f
        )
        drawLine(
            color = crackColor,
            start = Offset(centerX, centerY),
            end = Offset(rect.left + rect.width * 0.65f, rect.bottom - 2f),
            strokeWidth = 2.5f
        )

        // Second branching crack if severe damage
        if (brick.damageFraction >= 0.65f) {
            drawLine(
                color = crackColor,
                start = Offset(centerX, centerY),
                end = Offset(rect.right - 4f, centerY - rect.height * 0.2f),
                strokeWidth = 2.5f
            )
        }
    }
}

private fun DrawScope.drawTemplePaddle(paddle: Paddle) {
    val corner = CornerRadius(14f, 14f)
    val topLeft = Offset(paddle.left, paddle.top)
    val paddleSize = Size(paddle.currentWidth, paddle.height)

    // Outer glow
    val glowBrush = Brush.radialGradient(
        colors = listOf(Color(0x66FFC107), Color(0x00FFC107)),
        center = Offset(paddle.centerX, paddle.top + paddle.height / 2f),
        radius = paddle.currentWidth * 0.6f
    )
    drawRoundRect(
        brush = glowBrush,
        topLeft = Offset(paddle.left - 12f, paddle.top - 8f),
        size = Size(paddle.currentWidth + 24f, paddle.height + 16f),
        cornerRadius = CornerRadius(18f, 18f)
    )

    // Paddle body (Golden Relic with gradient)
    val paddleBrush = Brush.verticalGradient(
        colors = listOf(TempleGoldLight, TempleGold, Color(0xFFC79100)),
        startY = paddle.top,
        endY = paddle.bottom
    )
    drawRoundRect(
        brush = paddleBrush,
        topLeft = topLeft,
        size = paddleSize,
        cornerRadius = corner
    )

    // Bevel border
    drawRoundRect(
        color = Color(0xFFFFF9C4),
        topLeft = topLeft,
        size = paddleSize,
        cornerRadius = corner,
        style = Stroke(width = 2.5f)
    )

    // Embedded jade/cyan jewel in center
    val jewelRadius = (paddle.height * 0.35f).coerceAtMost(8f)
    drawCircle(
        color = MysticCyan,
        radius = jewelRadius,
        center = Offset(paddle.centerX, paddle.top + paddle.height / 2f)
    )
    drawCircle(
        color = Color.White,
        radius = jewelRadius * 0.4f,
        center = Offset(paddle.centerX - 1.5f, paddle.top + paddle.height / 2f - 1.5f)
    )
}

private fun DrawScope.drawGlowingBall(ball: Ball, isSpeedBoostActive: Boolean) {
    val center = Offset(ball.x, ball.y)
    val coreColor = if (isSpeedBoostActive) Color(0xFFFF9100) else MysticCyan
    val auraColor = if (isSpeedBoostActive) Color(0xFFFFAB40) else Color(0xFF80D8FF)

    // Fading motion trail
    for (i in ball.trail.indices) {
        val (tx, ty) = ball.trail[i]
        val trailAlpha = ((ball.trail.size - i).toFloat() / ball.trail.size.toFloat()) * 0.45f
        val trailRadius = ball.radius * (1f - (i.toFloat() / ball.trail.size.toFloat()) * 0.4f)
        drawCircle(
            color = auraColor.copy(alpha = trailAlpha),
            radius = trailRadius,
            center = Offset(tx, ty)
        )
    }

    // Outer radiant aura
    drawCircle(
        color = auraColor.copy(alpha = 0.35f),
        radius = ball.radius * 1.7f,
        center = center
    )

    // Ball main orb
    drawCircle(
        color = coreColor,
        radius = ball.radius,
        center = center
    )

    // White core hot-spot
    drawCircle(
        color = Color.White,
        radius = ball.radius * 0.45f,
        center = Offset(ball.x - ball.radius * 0.25f, ball.y - ball.radius * 0.25f)
    )
}

private fun DrawScope.drawTreasure(treasure: Treasure) {
    val center = Offset(treasure.x, treasure.y)
    val pulseScale = 1.0f + 0.12f * sin(treasure.pulse)
    val size = treasure.size * pulseScale
    val half = size / 2f

    // Outer pulsating halo
    drawCircle(
        color = treasure.type.primaryColor.copy(alpha = 0.35f),
        radius = size * 0.85f,
        center = center
    )

    when (treasure.type) {
        PowerUpType.SPEED_BOOST -> {
            // Hourglass shape (Jam Pasir Ajaib)
            val path = Path().apply {
                moveTo(center.x - half, center.y - half)
                lineTo(center.x + half, center.y - half)
                lineTo(center.x - half, center.y + half)
                lineTo(center.x + half, center.y + half)
                close()
            }
            drawPath(path = path, color = treasure.type.primaryColor)
            drawPath(path = path, color = Color.White, style = Stroke(width = 2.5f))
        }

        PowerUpType.MULTI_BALL -> {
            // Twin Crystals shape (Kristal Kembar)
            // Left crystal
            val pathLeft = Path().apply {
                moveTo(center.x - half * 0.5f, center.y - half)
                lineTo(center.x - half * 0.1f, center.y)
                lineTo(center.x - half * 0.5f, center.y + half)
                lineTo(center.x - half * 0.9f, center.y)
                close()
            }
            drawPath(path = pathLeft, color = MysticCyan)

            // Right crystal
            val pathRight = Path().apply {
                moveTo(center.x + half * 0.5f, center.y - half)
                lineTo(center.x + half * 0.9f, center.y)
                lineTo(center.x + half * 0.5f, center.y + half)
                lineTo(center.x + half * 0.1f, center.y)
                close()
            }
            drawPath(path = pathRight, color = treasure.type.secondaryColor)
        }

        PowerUpType.PADDLE_EXPAND -> {
            // Ancient Sacred Board / Shield shape (Papan Pusaka)
            val path = Path().apply {
                moveTo(center.x - half, center.y - half * 0.6f)
                lineTo(center.x + half, center.y - half * 0.6f)
                lineTo(center.x + half * 0.8f, center.y + half * 0.7f)
                lineTo(center.x, center.y + half)
                lineTo(center.x - half * 0.8f, center.y + half * 0.7f)
                close()
            }
            drawPath(path = path, color = SacredEmerald)
            drawPath(path = path, color = TempleGoldLight, style = Stroke(width = 2.5f))
        }
    }
}

private fun DrawScope.drawParticle(particle: Particle) {
    drawCircle(
        color = particle.color.copy(alpha = particle.alpha),
        radius = particle.size * particle.alpha,
        center = Offset(particle.x, particle.y)
    )
}
