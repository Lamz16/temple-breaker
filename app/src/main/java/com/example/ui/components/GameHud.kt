package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.entity.PowerUpType
import com.example.game.state.ActivePowerUp
import com.example.game.state.GameState
import com.example.game.state.GameUiState
import com.example.ui.theme.MysticCyan
import com.example.ui.theme.SacredEmerald
import com.example.ui.theme.TempleBorder
import com.example.ui.theme.TempleDarkBg
import com.example.ui.theme.TempleGold
import com.example.ui.theme.TempleGoldLight
import com.example.ui.theme.TempleSurface
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import java.util.Locale

@Composable
fun GameHud(
    uiState: GameUiState,
    onPauseClick: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top status row: Score, High Score, Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Display
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TempleSurface.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TempleBorder),
                modifier = Modifier.testTag("score_panel")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Score",
                        tint = TempleGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${uiState.score}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextGold
                        )
                    }
                }
            }

            // High Score Display
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TempleSurface.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TempleBorder),
                modifier = Modifier.testTag("high_score_panel")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HIGH SCORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${uiState.highScore}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (uiState.isNewHighScore) MysticCyan else TextLight
                    )
                }
            }

            // Actions: Mute and Pause
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TempleSurface.copy(alpha = 0.88f))
                        .border(1.dp, TempleBorder, CircleShape)
                        .testTag("mute_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (uiState.isMuted) "Unmute" else "Mute",
                        tint = TextLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TempleSurface.copy(alpha = 0.88f))
                        .border(1.dp, TempleGold, CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = if (uiState.gameState == GameState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TempleGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Secondary Info Bar: Balls, Remaining Bricks & Active Power-Ups
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ball Count indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TempleDarkBg.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "BOLA:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                for (i in 0 until uiState.ballsCount.coerceAtMost(5)) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MysticCyan)
                    )
                }
                if (uiState.ballsCount > 5) {
                    Text(
                        text = "+${uiState.ballsCount - 5}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MysticCyan
                    )
                }
            }

            // Brick count
            Text(
                text = "BATA: ${uiState.bricksRemaining}/${uiState.totalBricks}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TempleDarkBg.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Power-Up Countdown Badges
        AnimatedVisibility(
            visible = uiState.activePowerUps.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.activePowerUps.forEach { powerUp ->
                    PowerUpBadge(powerUp = powerUp, modifier = Modifier.weight(1f, fill = false))
                }
            }
        }
    }
}

@Composable
private fun PowerUpBadge(
    powerUp: ActivePowerUp,
    modifier: Modifier = Modifier
) {
    val (label, icon) = when (powerUp.type) {
        PowerUpType.SPEED_BOOST -> Pair("SPEED BOOST", Icons.Default.HourglassTop)
        PowerUpType.PADDLE_EXPAND -> Pair("PADDLE BOOST", Icons.Default.Shield)
        PowerUpType.MULTI_BALL -> Pair("MULTI BALL", Icons.Default.Star)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = TempleSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, powerUp.type.primaryColor),
        modifier = modifier.testTag("powerup_badge_${powerUp.type.name.lowercase(Locale.ROOT)}")
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = powerUp.type.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = powerUp.type.primaryColor
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.1fs", powerUp.remainingSeconds.coerceAtLeast(0f)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextLight
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { powerUp.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = powerUp.type.primaryColor,
                trackColor = TempleDarkBg,
            )
        }
    }
}
