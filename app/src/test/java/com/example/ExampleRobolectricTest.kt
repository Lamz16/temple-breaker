package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.engine.GameEngine
import org.junit.Assert.assertEquals
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
        assertEquals("Temple Breaker", appName)
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
}
