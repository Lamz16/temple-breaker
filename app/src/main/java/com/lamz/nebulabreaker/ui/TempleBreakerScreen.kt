package com.lamz.nebulabreaker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamz.nebulabreaker.game.state.GameState
import com.lamz.nebulabreaker.ui.components.GameCanvas
import com.lamz.nebulabreaker.ui.components.GameHud
import com.lamz.nebulabreaker.ui.components.GameOverOverlay
import com.lamz.nebulabreaker.ui.components.PauseOverlay
import com.lamz.nebulabreaker.ui.components.ReadyOverlay
import com.lamz.nebulabreaker.ui.components.SettingsDialog
import com.lamz.nebulabreaker.ui.components.VictoryOverlay
import com.lamz.nebulabreaker.ui.theme.TempleDarkBg
import com.lamz.nebulabreaker.ui.viewmodel.GameViewModel

@Composable
fun TempleBreakerScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSettings by rememberSaveable { mutableStateOf(false) }

    // Android Lifecycle listener: auto-pause when moving to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TempleDarkBg)
        ) {
            // Main Interactive Game Canvas
            GameCanvas(
                engine = viewModel.engine,
                frameId = uiState.frameId,
                onPaddleMove = { x -> viewModel.onPaddleMove(x) },
                onSizeChanged = { w, h -> viewModel.onCanvasSizeChanged(w, h) },
                modifier = Modifier.fillMaxSize()
            )

            // Top HUD Overlay
            GameHud(
                uiState = uiState,
                onPauseClick = {
                    if (uiState.gameState == GameState.PLAYING) {
                        viewModel.pauseGame()
                    } else if (uiState.gameState == GameState.PAUSED) {
                        viewModel.resumeGame()
                    }
                },
                onToggleMute = { viewModel.toggleMute() },
                onSettingsClick = { showSettings = true }
            )

            if (showSettings) {
                SettingsDialog(
                    gyroscopeEnabled = uiState.isGyroscopeEnabled,
                    gyroscopeSensitivity = uiState.gyroscopeSensitivity,
                    onGyroscopeEnabledChange = viewModel::setGyroscopeEnabled,
                    onGyroscopeSensitivityChange = viewModel::setGyroscopeSensitivity,
                    onDismiss = { showSettings = false }
                )
            }

            // Game State Conditional Overlays
            when (uiState.gameState) {
                GameState.READY -> {
                    ReadyOverlay(
                        uiState = uiState,
                        onStartGame = { viewModel.startGame() }
                    )
                }
                GameState.PAUSED -> {
                    PauseOverlay(
                        uiState = uiState,
                        onResume = { viewModel.resumeGame() },
                        onRestart = { viewModel.restartGame() }
                    )
                }
                GameState.GAME_OVER -> {
                    GameOverOverlay(
                        uiState = uiState,
                        onRestart = { viewModel.restartGame() }
                    )
                }
                GameState.VICTORY -> {
                    VictoryOverlay(
                        uiState = uiState,
                        onPlayAgain = { viewModel.nextLevel() }
                    )
                }
                GameState.PLAYING -> {
                    // Gameplay is active, canvas + HUD visible
                }
            }
        }
    }
}
