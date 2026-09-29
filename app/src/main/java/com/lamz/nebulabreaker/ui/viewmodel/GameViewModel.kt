package com.lamz.nebulabreaker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lamz.nebulabreaker.audio.SoundManager
import com.lamz.nebulabreaker.data.HighScoreRepository
import com.lamz.nebulabreaker.game.engine.GameEngine
import com.lamz.nebulabreaker.game.state.GameState
import com.lamz.nebulabreaker.game.state.GameUiState
import com.lamz.nebulabreaker.input.TiltController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val engine = GameEngine()
    private val highScoreRepo = HighScoreRepository(application)
    val soundManager = SoundManager(application)
    private val tiltController = TiltController(application) { tilt ->
        val state = _uiState.value
        if (state.gameState == GameState.PLAYING && state.isGyroscopeEnabled) {
            engine.movePaddleWithTilt(tilt * state.gyroscopeSensitivity)
        }
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

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
        tiltController.stop()
        engine.startNewGame(targetLevel = 1)
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                score = 0,
                isNewHighScore = false,
                level = 1
            )
        }
        engine.startLoop(viewModelScope) {
            syncUiState()
        }
        startTiltControlIfEnabled()
        syncUiState()
    }

    fun restartGame() {
        startGame()
    }

    fun nextLevel() {
        tiltController.stop()
        val nextLvl = _uiState.value.level + 1
        engine.startNewGame(targetLevel = nextLvl)
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                isNewHighScore = false,
                level = nextLvl
            )
        }
        engine.startLoop(viewModelScope) {
            syncUiState()
        }
        startTiltControlIfEnabled()
        syncUiState()
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            engine.pauseLoop()
            tiltController.stop()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            engine.resumeLoop()
            startTiltControlIfEnabled()
        }
    }

    fun onAppBackgrounded() {
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

    fun setGyroscopeEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isGyroscopeEnabled = enabled) }
        if (enabled && _uiState.value.gameState == GameState.PLAYING) {
            startTiltControlIfEnabled()
        } else {
            tiltController.stop()
        }
    }

    fun setGyroscopeSensitivity(sensitivity: Float) {
        _uiState.update { it.copy(gyroscopeSensitivity = sensitivity.coerceIn(0.4f, 2.0f)) }
    }

    private fun startTiltControlIfEnabled() {
        if (_uiState.value.isGyroscopeEnabled) tiltController.start()
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
                activePowerUps = engine.activePowerUps.map { p -> p.copy() },
                frameId = it.frameId + 1
            )
        }
    }

    private fun handleGameOver() {
        tiltController.stop()
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
        tiltController.stop()
        soundManager.play(SoundManager.SoundEvent.VICTORY)
        val finalScore = engine.score + 500

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
        engine.stopLoop()
        tiltController.stop()
        soundManager.release()
    }
}
