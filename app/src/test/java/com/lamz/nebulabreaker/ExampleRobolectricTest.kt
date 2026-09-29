package com.lamz.nebulabreaker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamz.nebulabreaker.game.engine.GameEngine
import com.lamz.nebulabreaker.game.entity.Ball
import com.lamz.nebulabreaker.game.entity.Paddle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nebula Breaker", appName)
    }

    @Test
    fun `game engine initializes arena and bricks properly`() {
        val engine = GameEngine()
        engine.setArenaDimensions(1080f, 1920f)
        engine.startNewGame(1)

        assertTrue(engine.bricks.isNotEmpty())
        assertTrue(engine.balls.isNotEmpty())
        assertEquals(0, engine.score)
    }

    @Test
    fun `ball attaches to paddle and launches with velocity`() {
        val paddle = Paddle(centerX = 500f, y = 1700f, baseWidth = 200f, height = 24f)
        val ball = Ball(id = 1, radius = 15f)

        ball.attachToPaddle(paddle)
        assertEquals(500f, ball.x, 0.01f)
        assertEquals(1700f - 15f - 2f, ball.y, 0.01f)
        assertFalse(ball.isLaunched)
        assertEquals(0f, ball.vx, 0.01f)
        assertEquals(0f, ball.vy, 0.01f)

        ball.launch(launchSpeed = 600f, angleDegrees = 0f)
        assertTrue(ball.isLaunched)
        assertEquals(0f, ball.vx, 0.01f)
        assertEquals(-600f, ball.vy, 0.01f)

        ball.step(0.1f) // moves upward by 60 pixels
        assertEquals(500f, ball.x, 0.01f)
        assertEquals(1683f - 60f, ball.y, 0.01f)
    }

    @Test
    fun `paddle moveTo clamps within horizontal bounds`() {
        val paddle = Paddle(centerX = 500f, y = 1700f, baseWidth = 200f, currentWidth = 200f, height = 24f)
        paddle.moveTo(-100f, minX = 0f, maxX = 1000f)
        assertEquals(100f, paddle.centerX, 0.01f) // 0 + halfWidth(100)

        paddle.moveTo(2000f, minX = 0f, maxX = 1000f)
        assertEquals(900f, paddle.centerX, 0.01f) // 1000 - halfWidth(100)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `game engine coroutine loop execution test`() = runTest {
        val engine = GameEngine()
        engine.setArenaDimensions(1080f, 1920f)
        engine.startNewGame(1)

        val initialY = engine.balls.first().y
        engine.step(0.016f)

        // Ball should have moved upward
        assertTrue(engine.balls.first().y < initialY)
    }
}
