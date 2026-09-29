package com.example.game.state

import com.example.game.entity.PowerUpType

enum class GameState {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY
}

data class ActivePowerUp(
    val type: PowerUpType,
    var remainingSeconds: Float,
    val totalSeconds: Float
) {
    val progress: Float
        get() = (remainingSeconds / totalSeconds).coerceIn(0f, 1f)
}

data class GameUiState(
    val gameState: GameState = GameState.READY,
    val score: Int = 0,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val ballsCount: Int = 1,
    val bricksRemaining: Int = 0,
    val totalBricks: Int = 0,
    val level: Int = 1,
    val activePowerUps: List<ActivePowerUp> = emptyList(),
    val isMuted: Boolean = false
)
