package com.example.game.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.game.collision.CollisionSystem
import com.example.game.entity.Ball
import com.example.game.entity.Brick
import com.example.game.entity.BrickType
import com.example.game.entity.Paddle
import com.example.game.entity.Particle
import com.example.game.entity.PowerUpType
import com.example.game.entity.Treasure
import com.example.game.state.ActivePowerUp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine {

    var arenaWidth: Float = 1080f
        private set
    var arenaHeight: Float = 1920f
        private set

    val balls = mutableListOf<Ball>()
    val paddle = Paddle()
    val bricks = mutableListOf<Brick>()
    val treasures = mutableListOf<Treasure>()
    val particles = mutableListOf<Particle>()
    val activePowerUps = mutableListOf<ActivePowerUp>()

    var score: Int = 0
        private set
    var level: Int = 1
        private set

    private var nextBallId = 1
    private var nextTreasureId = 1

    // Callbacks
    var onBallHitPaddle: (() -> Unit)? = null
    var onBallHitBrick: (() -> Unit)? = null
    var onBrickDestroyed: ((scoreGained: Int) -> Unit)? = null
    var onTreasureCollected: ((PowerUpType) -> Unit)? = null
    var onGameOver: (() -> Unit)? = null
    var onVictory: (() -> Unit)? = null

    fun setArenaDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val changed = (width != arenaWidth || height != arenaHeight)
        arenaWidth = width
        arenaHeight = height

        // Update paddle dimensions based on screen proportions
        paddle.baseWidth = (width * 0.28f).coerceIn(120f, 260f)
        paddle.expandedWidth = (width * 0.44f).coerceIn(180f, 380f)
        paddle.height = (height * 0.022f).coerceIn(18f, 32f)
        paddle.y = height - paddle.height - (height * 0.09f)

        if (paddle.centerX == 0f) {
            paddle.centerX = width / 2f
        }
        paddle.clampPosition(0f, arenaWidth)

        if (changed && bricks.isEmpty()) {
            setupLevel(level)
            resetBallOnPaddle()
        }
    }

    fun startNewGame(targetLevel: Int = 1) {
        score = 0
        level = targetLevel
        activePowerUps.clear()
        treasures.clear()
        particles.clear()
        paddle.targetWidth = paddle.baseWidth
        paddle.currentWidth = paddle.baseWidth
        paddle.centerX = arenaWidth / 2f

        setupLevel(level)
        resetBallOnPaddle()
        launchInitialBall()
    }

    fun setupLevel(lvl: Int) {
        bricks.clear()
        level = lvl

        val cols = 7
        val rows = 8
        val topMargin = arenaHeight * 0.12f
        val sidePadding = arenaWidth * 0.05f
        val usableWidth = arenaWidth - (sidePadding * 2f)
        val spacing = (arenaWidth * 0.015f).coerceIn(4f, 10f)
        val brickWidth = (usableWidth - (spacing * (cols - 1))) / cols
        val brickHeight = (arenaHeight * 0.032f).coerceIn(24f, 44f)

        var brickId = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Design stepped temple ruins / pyramid layout
                val centerCol = cols / 2
                val distanceFromCenter = kotlin.math.abs(c - centerCol)

                // Skip top corner bricks to create stepped temple silhouette
                if (r == 0 && distanceFromCenter > 1) continue
                if (r == 1 && distanceFromCenter > 2) continue

                val brickType = when {
                    r < 2 -> BrickType.ANCIENT
                    r < 5 -> BrickType.STRONG
                    else -> BrickType.NORMAL
                }

                // Random power up chance
                val powerUp = if (Random.nextFloat() < 0.26f) {
                    val rand = Random.nextFloat()
                    when {
                        rand < 0.35f -> PowerUpType.SPEED_BOOST
                        rand < 0.70f -> PowerUpType.PADDLE_EXPAND
                        else -> PowerUpType.MULTI_BALL
                    }
                } else null

                val left = sidePadding + c * (brickWidth + spacing)
                val top = topMargin + r * (brickHeight + spacing)
                val rect = Rect(left, top, left + brickWidth, top + brickHeight)

                bricks.add(
                    Brick(
                        id = brickId++,
                        row = r,
                        col = c,
                        rect = rect,
                        type = brickType,
                        currentDurability = brickType.maxDurability,
                        containsPowerUp = powerUp
                    )
                )
            }
        }
    }

    fun resetBallOnPaddle() {
        balls.clear()
        val radius = (arenaWidth * 0.024f).coerceIn(10f, 20f)
        val initialBall = Ball(
            id = nextBallId++,
            x = paddle.centerX,
            y = paddle.top - radius - 2f,
            vx = 0f,
            vy = 0f,
            radius = radius
        )
        balls.add(initialBall)
    }

    fun launchInitialBall() {
        if (balls.isEmpty()) resetBallOnPaddle()
        val baseSpeed = (arenaHeight * 0.42f).coerceIn(460f, 820f)
        val ball = balls.first()
        val angleDeg = Random.nextFloat() * 40f - 20f // -20 to +20 degrees from vertical
        val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()

        ball.vx = baseSpeed * sin(angleRad)
        ball.vy = -baseSpeed * cos(angleRad)
        ball.speedMultiplier = getSpeedMultiplier()
    }

    fun movePaddleTo(targetX: Float) {
        paddle.centerX = targetX
        paddle.clampPosition(0f, arenaWidth)

        // If in launch preparation mode (ball has vy == 0), keep ball pinned on paddle
        for (ball in balls) {
            if (ball.vy == 0f && ball.vx == 0f) {
                ball.x = paddle.centerX
                ball.y = paddle.top - ball.radius - 2f
            }
        }
    }

    fun step(dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.033f)

        // 1. Update power-up timers
        updatePowerUps(dt)

        // 2. Smoothly animate paddle width
        paddle.updateWidth(dt)
        paddle.clampPosition(0f, arenaWidth)

        // 3. Move and collide balls
        val ballsIterator = balls.iterator()
        while (ballsIterator.hasNext()) {
            val ball = ballsIterator.next()
            ball.speedMultiplier = getSpeedMultiplier()
            ball.step(dt)

            // Arena boundaries
            val isOutOfBounds = CollisionSystem.checkBoundaryCollision(ball, arenaWidth, arenaHeight)
            if (isOutOfBounds) {
                ballsIterator.remove()
                continue
            }

            // Paddle collision
            if (CollisionSystem.checkPaddleCollision(ball, paddle)) {
                onBallHitPaddle?.invoke()
                spawnPaddleHitSparks(ball.x, paddle.top)
            }

            // Brick collision
            for (brick in bricks) {
                if (brick.isDestroyed) continue

                if (CollisionSystem.checkBrickCollision(ball, brick)) {
                    val wasDestroyed = brick.hit()
                    if (wasDestroyed) {
                        score += brick.type.scoreValue
                        onBrickDestroyed?.invoke(brick.type.scoreValue)
                        spawnBrickDebris(brick)

                        // Spawn falling treasure if present
                        if (brick.containsPowerUp != null) {
                            spawnTreasure(brick.rect.center.x, brick.rect.center.y, brick.containsPowerUp)
                        }
                    } else {
                        onBallHitBrick?.invoke()
                        spawnBrickImpactSparks(ball.x, ball.y, brick.type.highlightColor)
                    }
                    // Break so one ball does not hit multiple bricks simultaneously in a single frame
                    break
                }
            }
        }

        // 4. Check lose condition
        if (balls.isEmpty()) {
            onGameOver?.invoke()
            return
        }

        // 5. Check victory condition (all bricks destroyed)
        val remainingBricks = bricks.count { !it.isDestroyed }
        if (remainingBricks == 0 && bricks.isNotEmpty()) {
            onVictory?.invoke()
            return
        }

        // 6. Update treasures
        val treasureIterator = treasures.iterator()
        while (treasureIterator.hasNext()) {
            val treasure = treasureIterator.next()
            treasure.step(dt)

            // Collect by paddle
            if (CollisionSystem.checkTreasurePaddleCollision(treasure, paddle)) {
                treasure.isCollected = true
                applyPowerUp(treasure.type)
                score += 150 // Treasure bonus points
                onTreasureCollected?.invoke(treasure.type)
                spawnTreasureCollectedBurst(treasure.x, paddle.top, treasure.type.primaryColor)
                treasureIterator.remove()
                continue
            }

            // Expired below screen
            if (treasure.top > arenaHeight) {
                treasure.isExpired = true
                treasureIterator.remove()
            }
        }

        // 7. Update particles
        val particleIterator = particles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.step(dt)
            if (p.isDead) {
                particleIterator.remove()
            }
        }
    }

    private fun applyPowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.SPEED_BOOST -> {
                val existing = activePowerUps.find { it.type == PowerUpType.SPEED_BOOST }
                if (existing != null) {
                    existing.remainingSeconds = PowerUpType.SPEED_BOOST.durationSeconds
                } else {
                    activePowerUps.add(
                        ActivePowerUp(
                            type = PowerUpType.SPEED_BOOST,
                            remainingSeconds = PowerUpType.SPEED_BOOST.durationSeconds,
                            totalSeconds = PowerUpType.SPEED_BOOST.durationSeconds
                        )
                    )
                }
            }
            PowerUpType.MULTI_BALL -> {
                // Multi Ball spawns 2 additional balls from existing balls
                if (balls.isNotEmpty()) {
                    val referenceBall = balls.first()
                    val baseSpeed = referenceBall.speed().coerceAtLeast(500f)

                    // Ball 2: angled left (-35 degrees)
                    val ball2 = Ball(
                        id = nextBallId++,
                        x = referenceBall.x,
                        y = referenceBall.y,
                        vx = baseSpeed * sin(Math.toRadians(-35.0).toFloat()),
                        vy = -baseSpeed * cos(Math.toRadians(35.0).toFloat()),
                        radius = referenceBall.radius,
                        speedMultiplier = getSpeedMultiplier()
                    )
                    // Ball 3: angled right (+35 degrees)
                    val ball3 = Ball(
                        id = nextBallId++,
                        x = referenceBall.x,
                        y = referenceBall.y,
                        vx = baseSpeed * sin(Math.toRadians(35.0).toFloat()),
                        vy = -baseSpeed * cos(Math.toRadians(35.0).toFloat()),
                        radius = referenceBall.radius,
                        speedMultiplier = getSpeedMultiplier()
                    )

                    balls.add(ball2)
                    balls.add(ball3)
                }
            }
            PowerUpType.PADDLE_EXPAND -> {
                paddle.targetWidth = paddle.expandedWidth
                val existing = activePowerUps.find { it.type == PowerUpType.PADDLE_EXPAND }
                if (existing != null) {
                    existing.remainingSeconds = PowerUpType.PADDLE_EXPAND.durationSeconds
                } else {
                    activePowerUps.add(
                        ActivePowerUp(
                            type = PowerUpType.PADDLE_EXPAND,
                            remainingSeconds = PowerUpType.PADDLE_EXPAND.durationSeconds,
                            totalSeconds = PowerUpType.PADDLE_EXPAND.durationSeconds
                        )
                    )
                }
            }
        }
    }

    private fun updatePowerUps(dt: Float) {
        val iterator = activePowerUps.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.remainingSeconds -= dt
            if (p.remainingSeconds <= 0f) {
                // Revert effects
                if (p.type == PowerUpType.PADDLE_EXPAND) {
                    paddle.targetWidth = paddle.baseWidth
                }
                iterator.remove()
            }
        }
    }

    private fun getSpeedMultiplier(): Float {
        return if (activePowerUps.any { it.type == PowerUpType.SPEED_BOOST }) 1.45f else 1.0f
    }

    private fun spawnTreasure(x: Float, y: Float, type: PowerUpType) {
        val fallSpeed = (arenaHeight * 0.16f).coerceIn(190f, 320f)
        treasures.add(
            Treasure(
                id = nextTreasureId++,
                x = x,
                y = y,
                vy = fallSpeed,
                type = type
            )
        )
    }

    private fun spawnBrickDebris(brick: Brick) {
        val count = 12
        val centerX = brick.rect.center.x
        val centerY = brick.rect.center.y
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 280f + 60f
            particles.add(
                Particle(
                    x = centerX + (Random.nextFloat() - 0.5f) * brick.rect.width * 0.6f,
                    y = centerY + (Random.nextFloat() - 0.5f) * brick.rect.height * 0.6f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 60f,
                    color = if (i % 2 == 0) brick.type.primaryColor else brick.type.highlightColor,
                    size = Random.nextFloat() * 8f + 4f,
                    maxLifetime = Random.nextFloat() * 0.35f + 0.35f
                )
            )
        }
    }

    private fun spawnBrickImpactSparks(x: Float, y: Float, color: Color) {
        for (i in 0 until 5) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 140f + 40f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 5f + 2f,
                    maxLifetime = 0.25f
                )
            )
        }
    }

    private fun spawnPaddleHitSparks(x: Float, y: Float) {
        for (i in 0 until 7) {
            val angle = Math.toRadians((Random.nextFloat() * 120.0 + 210.0)).toFloat()
            val speed = Random.nextFloat() * 180f + 80f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = Color(0xFFFFD54F),
                    size = Random.nextFloat() * 6f + 3f,
                    maxLifetime = 0.3f
                )
            )
        }
    }

    private fun spawnTreasureCollectedBurst(x: Float, y: Float, color: Color) {
        for (i in 0 until 18) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 240f + 80f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = Random.nextFloat() * 8f + 4f,
                    maxLifetime = 0.5f
                )
            )
        }
    }
}
