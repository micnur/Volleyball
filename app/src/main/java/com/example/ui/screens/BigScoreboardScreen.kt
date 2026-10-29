package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsVolleyball
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothScorePacket
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.SanctionRed
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold
import com.example.ui.viewmodel.RefereeScreen
import com.example.ui.viewmodel.RefereeViewModel
import com.example.util.BluetoothConnectionState

@Composable
fun BigScoreboardScreen(
    viewModel: RefereeViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(RefereeScreen.MATCH)
    }

    val syncManager = viewModel.bluetoothSyncManager
    val connectionState by syncManager.connectionState.collectAsState()
    val connectedDevice by syncManager.connectedDeviceName.collectAsState()
    val isSimulation by syncManager.isSimulationMode.collectAsState()
    val isServer by syncManager.isServerRole.collectAsState()
    val remotePacket by syncManager.latestReceivedPacket.collectAsState()

    // Local match state (used if no remote packet is active or in transmitter mode)
    val localMatchState by viewModel.matchState.collectAsState()
    val localClock by viewModel.matchClock.collectAsState()
    val localTimeoutSeconds by viewModel.activeTimeoutSeconds.collectAsState()
    val localTimeoutTeam by viewModel.activeTimeoutTeam.collectAsState()

    var showBluetoothDialog by remember { mutableStateOf(false) }

    // Derive display data: If remote packet is available and device is client receiver, use remote packet;
    // otherwise use local match state.
    val displayData = remember(remotePacket, localMatchState, localClock, localTimeoutSeconds, isServer) {
        if (remotePacket != null && !isServer) {
            remotePacket!!
        } else {
            viewModel.buildCurrentScorePacket()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(CourtDark)
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = CourtDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Back to Referee Mode & Bluetooth Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { viewModel.navigateTo(RefereeScreen.MATCH) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CourtSurface, CircleShape)
                        .border(1.dp, CourtLines, CircleShape)
                        .testTag("big_scoreboard_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to Referee Console",
                        tint = VolleyballGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Bluetooth Connection Status Pill
                Surface(
                    onClick = { showBluetoothDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = when (connectionState) {
                        BluetoothConnectionState.CONNECTED -> Color(0xFF064E3B)
                        BluetoothConnectionState.LISTENING, BluetoothConnectionState.CONNECTING -> Color(0xFF78350F)
                        BluetoothConnectionState.ERROR -> Color(0xFF7F1D1D)
                        BluetoothConnectionState.DISCONNECTED -> CourtSurface
                    },
                    border = BorderStroke(
                        1.dp,
                        when (connectionState) {
                            BluetoothConnectionState.CONNECTED -> Color(0xFF10B981)
                            BluetoothConnectionState.LISTENING, BluetoothConnectionState.CONNECTING -> VolleyballGold
                            BluetoothConnectionState.ERROR -> SanctionRed
                            BluetoothConnectionState.DISCONNECTED -> CourtLines
                        }
                    ),
                    modifier = Modifier.testTag("big_scoreboard_bt_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (connectionState) {
                                BluetoothConnectionState.CONNECTED -> Icons.Default.BluetoothConnected
                                BluetoothConnectionState.LISTENING, BluetoothConnectionState.CONNECTING -> Icons.Default.BluetoothSearching
                                BluetoothConnectionState.ERROR -> Icons.Default.BluetoothDisabled
                                BluetoothConnectionState.DISCONNECTED -> Icons.Default.Bluetooth
                            },
                            contentDescription = null,
                            tint = when (connectionState) {
                                BluetoothConnectionState.CONNECTED -> Color(0xFF34D399)
                                BluetoothConnectionState.LISTENING, BluetoothConnectionState.CONNECTING -> VolleyballGold
                                BluetoothConnectionState.ERROR -> SanctionRed
                                BluetoothConnectionState.DISCONNECTED -> Color(0xFF94A3B8)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isSimulation -> "DEMO SIMULATION"
                                connectionState == BluetoothConnectionState.CONNECTED && isServer -> "HOST (REFEREE)"
                                connectionState == BluetoothConnectionState.CONNECTED -> "CONNECTED: ${connectedDevice ?: "SCOREBOARD"}"
                                connectionState == BluetoothConnectionState.LISTENING -> "WAITING FOR SCOREBOARD..."
                                connectionState == BluetoothConnectionState.CONNECTING -> "CONNECTING..."
                                else -> "BLUETOOTH: OFF / STANDALONE"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Bluetooth Settings Button
                IconButton(
                    onClick = { showBluetoothDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CourtSurface, CircleShape)
                        .border(1.dp, CourtLines, CircleShape)
                        .testTag("open_bt_sync_dialog_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Bluetooth Settings",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Active Timeout Warning Banner (if a 30s timeout is running)
            if (displayData.activeTimeoutSeconds != null && displayData.activeTimeoutSeconds > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("big_scoreboard_active_timeout_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, VolleyballGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = VolleyballGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "30-SECOND TIMEOUT IN PROGRESS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = VolleyballGold
                                )
                                Text(
                                    text = "Requested by: ${displayData.activeTimeoutTeamName ?: "Team"}",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(VolleyballGold)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${displayData.activeTimeoutSeconds}s",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CourtDark
                            )
                        }
                    }
                }
            }

            // Match Info Banner: Set Number, Set Clock & Completed Sets Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("big_scoreboard_info_strip"),
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CourtLines)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VolleyballGold)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (displayData.isDecidingSet) "DECIDING SET 5" else "SET ${displayData.currentSet}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = CourtDark
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TARGET: ${displayData.targetPoints} PTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    // Real-Time Set Clock
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (displayData.clockIsRunning) Color(0xFF10B981) else VolleyballGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayData.clockFormatted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (displayData.clockIsRunning) Color(0xFF34D399) else Color.White
                        )
                    }

                    // Completed Sets breakdown
                    if (displayData.completedSetsSummary.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            displayData.completedSetsSummary.forEach { summary ->
                                Box(
                                    modifier = Modifier
                                        .background(CourtSurfaceHigh, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = summary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Two-Team Big Scoreboard Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TEAM A BIG SCOREBOARD CARD
                BigTeamScoreCard(
                    teamName = displayData.teamAName,
                    teamCode = displayData.teamACode,
                    teamColorHex = displayData.teamAColorHex,
                    score = displayData.scoreA,
                    setsWon = displayData.setsWonA,
                    isServing = displayData.servingTeamId == "team_a",
                    timeoutsUsed = displayData.timeoutsUsedA,
                    timeoutsRemaining = displayData.timeoutsRemainingA,
                    maxTimeouts = displayData.maxTimeoutsPerSet,
                    subsUsed = displayData.subsUsedA,
                    subsRemaining = displayData.subsRemainingA,
                    onScorePlus = {
                        viewModel.scorePoint("team_a")
                    },
                    onScoreMinus = {
                        viewModel.undo()
                    },
                    onToggleServingTeam = {
                        viewModel.toggleServingTeam()
                    },
                    modifier = Modifier.weight(1f)
                )

                // TEAM B BIG SCOREBOARD CARD
                BigTeamScoreCard(
                    teamName = displayData.teamBName,
                    teamCode = displayData.teamBCode,
                    teamColorHex = displayData.teamBColorHex,
                    score = displayData.scoreB,
                    setsWon = displayData.setsWonB,
                    isServing = displayData.servingTeamId == "team_b",
                    timeoutsUsed = displayData.timeoutsUsedB,
                    timeoutsRemaining = displayData.timeoutsRemainingB,
                    maxTimeouts = displayData.maxTimeoutsPerSet,
                    subsUsed = displayData.subsUsedB,
                    subsRemaining = displayData.subsRemainingB,
                    onScorePlus = {
                        viewModel.scorePoint("team_b")
                    },
                    onScoreMinus = {
                        viewModel.undo()
                    },
                    onToggleServingTeam = {
                        viewModel.toggleServingTeam()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Bluetooth Connectivity & Device Selection Dialog
    if (showBluetoothDialog) {
        BluetoothSyncModalDialog(
            viewModel = viewModel,
            onDismiss = { showBluetoothDialog = false }
        )
    }
}

/**
 * Big Scoreboard Card for an individual team
 * Featuring giant score numbers, sets won, timeouts taken & remaining, and substitutions.
 */
@Composable
private fun BigTeamScoreCard(
    teamName: String,
    teamCode: String,
    teamColorHex: Long,
    score: Int,
    setsWon: Int,
    isServing: Boolean,
    timeoutsUsed: Int,
    timeoutsRemaining: Int,
    maxTimeouts: Int,
    subsUsed: Int,
    subsRemaining: Int,
    onScorePlus: () -> Unit,
    onScoreMinus: () -> Unit,
    onToggleServingTeam: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val teamColor = Color(teamColorHex)

    Card(
        modifier = modifier
            .testTag("big_score_card_$teamCode"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isServing) 2.5.dp else 1.5.dp,
            color = if (isServing) VolleyballGold else teamColor.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Team Header with Color bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(teamColor)
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teamName.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Service Indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isServing) VolleyballGold else Color.Transparent)
                    .clickable(enabled = onToggleServingTeam != null) {
                        onToggleServingTeam?.invoke()
                    }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .testTag("scoreboard_serve_indicator_$teamCode"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isServing) {
                    Icon(
                        imageVector = Icons.Default.SportsVolleyball,
                        contentDescription = "Serving - Tap to change serve",
                        tint = CourtDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SERVING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CourtDark
                    )
                } else {
                    Text(
                        text = "RECEIVING",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // GIANT SCORE NUMBER DISPLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CourtDark)
                    .border(1.dp, CourtLines, RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = score.toString().padStart(2, '0'),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (isServing) VolleyballGold else Color.White,
                    letterSpacing = (-2).sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sets Won Counter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CourtSurfaceHigh)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SETS WON: $setsWon",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFE2E8F0)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TIMEOUTS SECTION (FIVB RULE 15.1) - Clear taken & remaining
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CourtLines)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TIMEOUTS (FIVB 15.1)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VolleyballGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Timeouts Taken visual dots
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..maxTimeouts) {
                            val isTaken = i <= timeoutsUsed
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isTaken) SanctionYellow else Color(0xFF334155))
                                    .border(1.dp, if (isTaken) SanctionYellow else CourtLines, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTaken) {
                                    Text("T", fontSize = 9.sp, fontWeight = FontWeight.Black, color = CourtDark)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Taken: $timeoutsUsed/$maxTimeouts",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Remaining: $timeoutsRemaining",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeoutsRemaining > 0) Color(0xFF34D399) else SanctionRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SUBSTITUTIONS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CourtLines)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBS: $subsUsed/6",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Rem: $subsRemaining",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (subsRemaining > 0) Color(0xFF34D399) else SanctionRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Standalone Quick Adjust Buttons (+1 / -1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onScoreMinus,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = BorderStroke(1.dp, CourtLines)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus Point", modifier = Modifier.size(16.dp))
                }

                Button(
                    onClick = onScorePlus,
                    modifier = Modifier
                        .weight(2f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = teamColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Point", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1 Point", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Bluetooth Setup & Pairing Dialog
 */
@Composable
private fun BluetoothSyncModalDialog(
    viewModel: RefereeViewModel,
    onDismiss: () -> Unit
) {
    val syncManager = viewModel.bluetoothSyncManager
    val connectionState by syncManager.connectionState.collectAsState()
    val statusMsg by syncManager.statusMessage.collectAsState()
    val pairedDevices by syncManager.pairedDevices.collectAsState()
    val isSimulation by syncManager.isSimulationMode.collectAsState()
    val isServer by syncManager.isServerRole.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bluetooth, null, tint = VolleyballGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bluetooth Scoreboard Sync", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Connection Status Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CourtDark)
                        .border(1.dp, CourtLines, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "STATUS: ${connectionState.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (connectionState) {
                                BluetoothConnectionState.CONNECTED -> Color(0xFF34D399)
                                BluetoothConnectionState.LISTENING -> VolleyballGold
                                else -> Color(0xFFCBD5E1)
                            }
                        )
                        Text(
                            text = statusMsg,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Role Action 1: Host as Referee Console
                Text(
                    text = "DEVICE ROLES:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VolleyballGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        viewModel.startBluetoothServer()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("1. Host as Referee Console (Transmitter)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Demo Simulation Toggle Button
                OutlinedButton(
                    onClick = {
                        viewModel.toggleBluetoothSimulation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSimulation) SanctionYellow else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSimulation) SanctionYellow else CourtLines)
                ) {
                    Icon(
                        imageVector = if (isSimulation) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSimulation) "Stop Demo Simulation Mode" else "Start Demo Simulation Mode (Test Display)",
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Paired Devices List (Connect as Scoreboard Client)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. CONNECT AS SCOREBOARD (PAIRED DEVICES):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    IconButton(
                        onClick = { syncManager.refreshPairedDevices() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = VolleyballGold, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (pairedDevices.isEmpty()) {
                    Text(
                        text = "No paired Bluetooth devices detected. Pair your second Android device in Android System Settings, or tap 'Refresh'.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                } else {
                    pairedDevices.forEach { device ->
                        Surface(
                            onClick = {
                                viewModel.connectBluetoothScoreboard(device.address)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = CourtDark,
                            border = BorderStroke(1.dp, CourtLines),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(device.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(device.address, fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VolleyballGold)
                            }
                        }
                    }
                }

                if (connectionState == BluetoothConnectionState.CONNECTED || isSimulation) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.disconnectBluetooth() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SanctionRed)
                    ) {
                        Text("Disconnect Bluetooth", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VolleyballGold, contentColor = CourtDark)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CourtSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
