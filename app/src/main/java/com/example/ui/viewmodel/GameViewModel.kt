package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.HighScoreRepository
import com.example.game.engine.GameEngine
import com.example.game.entity.PowerUpType
import com.example.game.state.GameState
import com.example.game.state.GameUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val engine = GameEngine()
    private val highScoreRepo = HighScoreRepository(application)
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastFrameTimeNanos: Long = 0L

    init {
        // Collect persistent high score from DataStore
        viewModelScope.launch {
            highScoreRepo.highScoreFlow.collect { storedHighScore ->
                _uiState.update { it.copy(highScore = storedHighScore) }
            }
        }

        // Setup engine callbacks
        engine.onBallHitPaddle = {
            soundManager.play(SoundManager.SoundEvent.BALL_HIT_PADDLE)
        }
        engine.onBallHitBrick = {
            soundManager.play(SoundManager.SoundEvent.BALL_HIT_BRICK)
        }
        engine.onBrickDestroyed = { _ ->
            soundManager.play(SoundManager.SoundEvent.BRICK_DESTROYED)
            syncUiState()
        }
        engine.onTreasureCollected = { _ ->
            soundManager.play(SoundManager.SoundEvent.TREASURE_COLLECTED)
            syncUiState()
        }
        engine.onGameOver = {
            handleGameOver()
        }
        engine.onVictory = {
            handleVictory()
        }
    }

    fun onCanvasSizeChanged(width: Float, height: Float) {
        engine.setArenaDimensions(width, height)
        syncUiState()
    }

    fun startGame() {
        engine.startNewGame(targetLevel = 1)
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                score = 0,
                isNewHighScore = false,
                level = 1
            )
        }
        startGameLoop()
        syncUiState()
    }

    fun restartGame() {
        startGame()
    }

    fun nextLevel() {
        val nextLvl = _uiState.value.level + 1
        engine.startNewGame(targetLevel = nextLvl)
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                isNewHighScore = false,
                level = nextLvl
            )
        }
        startGameLoop()
        syncUiState()
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            stopGameLoop()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startGameLoop()
        }
    }

    fun onAppBackgrounded() {
        // Automatically pause game when app moves to background
        pauseGame()
    }

    fun onPaddleMove(touchX: Float) {
        if (_uiState.value.gameState == GameState.PLAYING || _uiState.value.gameState == GameState.READY) {
            engine.movePaddleTo(touchX)
        }
    }

    fun toggleMute() {
        soundManager.isMuted = !soundManager.isMuted
        _uiState.update { it.copy(isMuted = soundManager.isMuted) }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastFrameTimeNanos = System.nanoTime()

        gameLoopJob = viewModelScope.launch {
            while (isActive && _uiState.value.gameState == GameState.PLAYING) {
                val now = System.nanoTime()
                val deltaNanos = now - lastFrameTimeNanos
                lastFrameTimeNanos = now

                val dtSeconds = (deltaNanos / 1_000_000_000f).coerceIn(0.001f, 0.033f)

                // Advance game physics
                engine.step(dtSeconds)

                // Periodically update reactive state
                syncUiState()

                // Target ~60 FPS update cycle
                delay(16L)
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private fun syncUiState() {
        val remainingBricks = engine.bricks.count { !it.isDestroyed }
        val currentScore = engine.score
        val isNew = currentScore > _uiState.value.highScore

        _uiState.update {
            it.copy(
                score = currentScore,
                highScore = if (isNew) currentScore else it.highScore,
                isNewHighScore = isNew,
                ballsCount = engine.balls.size,
                bricksRemaining = remainingBricks,
                totalBricks = engine.bricks.size,
                level = engine.level,
                activePowerUps = engine.activePowerUps.map { p -> p.copy() }
            )
        }
    }

    private fun handleGameOver() {
        stopGameLoop()
        soundManager.play(SoundManager.SoundEvent.GAME_OVER)
        val finalScore = engine.score

        viewModelScope.launch {
            val isRecord = highScoreRepo.saveHighScoreIfGreater(finalScore)
            _uiState.update {
                it.copy(
                    gameState = GameState.GAME_OVER,
                    score = finalScore,
                    isNewHighScore = isRecord || finalScore >= it.highScore
                )
            }
        }
    }

    private fun handleVictory() {
        stopGameLoop()
        soundManager.play(SoundManager.SoundEvent.VICTORY)
        val finalScore = engine.score + 500 // Completion bonus

        viewModelScope.launch {
            val isRecord = highScoreRepo.saveHighScoreIfGreater(finalScore)
            _uiState.update {
                it.copy(
                    gameState = GameState.VICTORY,
                    score = finalScore,
                    isNewHighScore = isRecord || finalScore >= it.highScore
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
        soundManager.release()
    }
}
