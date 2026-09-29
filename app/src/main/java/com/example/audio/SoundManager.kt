package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.util.Log

/**
 * SoundManager handles game sound effects using Android's SoundPool.
 *
 * Supported sound events:
 * - Ball hits paddle
 * - Ball hits brick
 * - Brick destroyed
 * - Treasure / power-up collected
 * - Game over
 * - Victory
 *
 * Audio assets can be dropped into:
 * `app/src/main/res/raw/`
 * Expected files (e.g. .ogg or .wav):
 * - sound_hit_paddle
 * - sound_hit_brick
 * - sound_brick_destroyed
 * - sound_treasure_collected
 * - sound_game_over
 * - sound_victory
 *
 * If raw resource files are not yet present, a fallback tone generator provides
 * immediate audio feedback without crashing or requiring external assets.
 */
class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundEvent, Int>()
    private var toneGenerator: ToneGenerator? = null
    var isMuted: Boolean = false

    enum class SoundEvent {
        BALL_HIT_PADDLE,
        BALL_HIT_BRICK,
        BRICK_DESTROYED,
        TREASURE_COLLECTED,
        GAME_OVER,
        VICTORY
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            Log.w("SoundManager", "ToneGenerator initialization failed", e)
        }

        loadResourceSounds()
    }

    private fun loadResourceSounds() {
        val resources = context.resources
        val packageName = context.packageName

        fun tryLoad(event: SoundEvent, resName: String) {
            val resId = resources.getIdentifier(resName, "raw", packageName)
            if (resId != 0) {
                val soundId = soundPool.load(context, resId, 1)
                soundMap[event] = soundId
            }
        }

        tryLoad(SoundEvent.BALL_HIT_PADDLE, "sound_hit_paddle")
        tryLoad(SoundEvent.BALL_HIT_BRICK, "sound_hit_brick")
        tryLoad(SoundEvent.BRICK_DESTROYED, "sound_brick_destroyed")
        tryLoad(SoundEvent.TREASURE_COLLECTED, "sound_treasure_collected")
        tryLoad(SoundEvent.GAME_OVER, "sound_game_over")
        tryLoad(SoundEvent.VICTORY, "sound_victory")
    }

    fun play(event: SoundEvent, rate: Float = 1.0f) {
        if (isMuted) return

        val soundId = soundMap[event]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, rate)
        } else {
            // Graceful synthesized tone fallback
            playFallbackTone(event)
        }
    }

    private fun playFallbackTone(event: SoundEvent) {
        val tone = toneGenerator ?: return
        try {
            when (event) {
                SoundEvent.BALL_HIT_PADDLE -> tone.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
                SoundEvent.BALL_HIT_BRICK -> tone.startTone(ToneGenerator.TONE_PROP_ACK, 40)
                SoundEvent.BRICK_DESTROYED -> tone.startTone(ToneGenerator.TONE_SUP_CONFIRM, 65)
                SoundEvent.TREASURE_COLLECTED -> tone.startTone(ToneGenerator.TONE_PROP_PROMPT, 90)
                SoundEvent.GAME_OVER -> tone.startTone(ToneGenerator.TONE_SUP_ERROR, 220)
                SoundEvent.VICTORY -> tone.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200)
            }
        } catch (e: Exception) {
            Log.d("SoundManager", "Error playing fallback tone: ${e.message}")
        }
    }

    fun release() {
        soundPool.release()
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
        toneGenerator = null
    }
}
