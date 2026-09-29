package com.lamz.nebulabreaker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.lamz.nebulabreaker.game.entity.PowerUpType
import kotlin.math.cos
import kotlin.math.sin

/** A compact, distinct cosmic glyph for each collectible. */
@Composable
fun PowerUpGlyph(type: PowerUpType, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.39f
        val color = type.primaryColor

        drawCircle(color.copy(alpha = 0.18f), radius * 1.3f, center)
        when (type) {
            PowerUpType.SPEED_BOOST -> {
                val bolt = Path().apply {
                    moveTo(center.x + radius * 0.12f, center.y - radius)
                    lineTo(center.x - radius * 0.48f, center.y + radius * 0.05f)
                    lineTo(center.x - radius * 0.05f, center.y + radius * 0.05f)
                    lineTo(center.x - radius * 0.22f, center.y + radius)
                    lineTo(center.x + radius * 0.58f, center.y - radius * 0.18f)
                    lineTo(center.x + radius * 0.12f, center.y - radius * 0.18f)
                    close()
                }
                drawPath(bolt, color)
            }
            PowerUpType.MULTI_BALL -> {
                drawCircle(color, radius * 0.45f, Offset(center.x - radius * 0.35f, center.y))
                drawCircle(type.secondaryColor, radius * 0.45f, Offset(center.x + radius * 0.35f, center.y))
                drawCircle(Color.White.copy(alpha = 0.75f), radius * 0.12f, Offset(center.x - radius * 0.5f, center.y - radius * 0.16f))
            }
            PowerUpType.PADDLE_EXPAND -> {
                drawRoundRect(color, Offset(center.x - radius, center.y - radius * 0.28f), androidx.compose.ui.geometry.Size(radius * 2f, radius * 0.56f), androidx.compose.ui.geometry.CornerRadius(radius * 0.28f))
                drawLine(Color.White.copy(alpha = 0.8f), Offset(center.x - radius * 0.6f, center.y), Offset(center.x + radius * 0.6f, center.y), radius * 0.11f)
            }
            PowerUpType.TIME_DILATION -> {
                drawCircle(color, radius, center, style = Stroke(radius * 0.18f))
                drawLine(color, center, Offset(center.x, center.y - radius * 0.58f), radius * 0.16f)
                drawLine(color, center, Offset(center.x + radius * 0.42f, center.y + radius * 0.22f), radius * 0.16f)
            }
            PowerUpType.STAR_CACHE -> {
                val star = Path().apply {
                    repeat(10) { index ->
                        val angle = Math.toRadians(-90.0 + index * 36.0).toFloat()
                        val pointRadius = if (index % 2 == 0) radius else radius * 0.43f
                        val point = Offset(center.x + cos(angle) * pointRadius, center.y + sin(angle) * pointRadius)
                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                    close()
                }
                drawPath(star, color)
            }
            PowerUpType.PHASE_SHIELD -> {
                drawCircle(color.copy(alpha = 0.28f), radius, center)
                drawCircle(color, radius * 0.78f, center, style = Stroke(radius * 0.18f))
                drawCircle(Color.White.copy(alpha = 0.9f), radius * 0.17f, center)
            }
        }
    }
}
