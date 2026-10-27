package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MatchClockState
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.VolleyballGold

@Composable
fun MatchTimerBar(
    clockState: MatchClockState,
    currentSetNumber: Int,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onAdjustTime: (deltaSeconds: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Pulsing animation for active running state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            clockState.isRunning -> Color(0xFF10B981) // Emerald Green
            clockState.currentSeconds > 0L -> VolleyballGold // Amber / Paused
            else -> Color(0xFF64748B) // Slate Gray / Stopped
        },
        label = "statusColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_timer_card"),
        colors = CardDefaults.cardColors(containerColor = CourtSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (clockState.isRunning) Color(0xFF10B981).copy(alpha = 0.5f) else CourtLines)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Timer Label & Status Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (clockState.isRunning) VolleyballGold else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "SET $currentSetNumber CLOCK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                                    .alpha(if (clockState.isRunning) pulseAlpha else 1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    clockState.isRunning -> "RUNNING"
                                    clockState.currentSeconds > 0L -> "PAUSED"
                                    else -> "STOPPED"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                        }
                    }
                }

                // Quick Adjustment Buttons (+1m / -1m)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { onAdjustTime(-60L) },
                        shape = RoundedCornerShape(6.dp),
                        color = CourtSurfaceHigh,
                        border = BorderStroke(1.dp, CourtLines),
                        modifier = Modifier
                            .size(height = 24.dp, width = 34.dp)
                            .testTag("timer_adjust_minus_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("-1m", fontSize = 10.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        onClick = { onAdjustTime(60L) },
                        shape = RoundedCornerShape(6.dp),
                        color = CourtSurfaceHigh,
                        border = BorderStroke(1.dp, CourtLines),
                        modifier = Modifier
                            .size(height = 24.dp, width = 34.dp)
                            .testTag("timer_adjust_plus_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+1m", fontSize = 10.sp, color = VolleyballGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Clock Display & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Clock Digits Display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CourtDark)
                        .border(1.dp, if (clockState.isRunning) Color(0xFF10B981).copy(alpha = 0.5f) else CourtLines, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("match_timer_display")
                ) {
                    Text(
                        text = clockState.formattedTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (clockState.isRunning) Color.White else Color(0xFFCBD5E1)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Control Buttons: Start/Pause & Reset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Reset Button
                    OutlinedButton(
                        onClick = {
                            if (clockState.currentSeconds > 0L) {
                                showResetConfirmDialog = true
                            } else {
                                onResetClick()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(1.dp, CourtLines),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("timer_reset_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Clock",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("RESET", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Start / Pause Button
                    Button(
                        onClick = {
                            if (clockState.isRunning) {
                                onPauseClick()
                            } else {
                                onStartClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (clockState.isRunning) Color(0xFFEA580C) else Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("timer_start_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (clockState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (clockState.isRunning) "Pause Clock" else "Start Clock",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (clockState.isRunning) "PAUSE" else if (clockState.currentSeconds > 0L) "RESUME" else "START",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Set Clock?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Do you want to reset the clock back to 00:00 for Set $currentSetNumber?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetClick()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reset Clock", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CourtSurface,
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1)
        )
    }
}
