package com.lamz.nebulabreaker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamz.nebulabreaker.ui.theme.MysticCyan
import com.lamz.nebulabreaker.ui.theme.TempleSurface
import com.lamz.nebulabreaker.ui.theme.TextLight
import com.lamz.nebulabreaker.ui.theme.TextMuted

@Composable
fun SettingsDialog(
    gyroscopeEnabled: Boolean,
    gyroscopeSensitivity: Float,
    onGyroscopeEnabledChange: (Boolean) -> Unit,
    onGyroscopeSensitivityChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TempleSurface,
        title = {
            Text("CONTROL SETTINGS", fontWeight = FontWeight.Black, color = TextLight)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gyroscope", fontWeight = FontWeight.Bold, color = TextLight)
                        Text("Miringkan perangkat untuk menggerakkan paddle", fontSize = 12.sp, color = TextMuted)
                    }
                    Switch(checked = gyroscopeEnabled, onCheckedChange = onGyroscopeEnabledChange)
                }
                Text(
                    text = "Sensitivitas: ${(gyroscopeSensitivity * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = if (gyroscopeEnabled) MysticCyan else TextMuted
                )
                Slider(
                    value = gyroscopeSensitivity,
                    onValueChange = onGyroscopeSensitivityChange,
                    enabled = gyroscopeEnabled,
                    valueRange = 0.4f..2.0f,
                    steps = 7
                )
                Text(
                    text = "Mode layar dikunci portrait agar layout permainan tidak terbalik.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                Text("SELESAI", color = MysticCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}
