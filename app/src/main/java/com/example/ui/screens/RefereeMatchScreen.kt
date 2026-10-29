package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CourtSide
import com.example.model.Player
import com.example.model.Team
import com.example.ui.components.Court6x6View
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.MatchTimerBar
import com.example.ui.components.PointReasonDialog
import com.example.ui.components.SanctionDialog
import com.example.ui.components.ScoreBoardHeader
import com.example.ui.components.SetLineupEntryDialog
import com.example.ui.components.SubstitutionSheet
import com.example.ui.components.TimeoutTimerDialog
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold
import com.example.ui.viewmodel.RefereeScreen
import com.example.ui.viewmodel.RefereeViewModel

@Composable
fun RefereeMatchScreen(
    viewModel: RefereeViewModel,
    modifier: Modifier = Modifier
) {
    val matchState by viewModel.matchState.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val showLineupDialog by viewModel.showLineupDialog.collectAsState()
    val activeTimeoutSeconds by viewModel.activeTimeoutSeconds.collectAsState()
    val activeTimeoutTeam by viewModel.activeTimeoutTeam.collectAsState()
    val pendingSideSwitchMessage by viewModel.pendingSideSwitchMessage.collectAsState()
    val technicalTimeoutAlert by viewModel.technicalTimeoutAlert.collectAsState()
    val clockState by viewModel.matchClock.collectAsState()

    // Dialog states
    var showPointReasonDialogForTeam by remember { mutableStateOf<Team?>(null) }
    var showSubstitutionSheetForTeam by remember { mutableStateOf<Team?>(null) }
    var preSelectedPlayerForSub by remember { mutableStateOf<Player?>(null) }
    var showSanctionDialog by remember { mutableStateOf(false) }
    var showExportReportDialog by remember { mutableStateOf(false) }

    val teamLeft = matchState.teamOnSide(CourtSide.LEFT)
    val teamRight = matchState.teamOnSide(CourtSide.RIGHT)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CourtDark)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CourtDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(VolleyballGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsVolleyball,
                            contentDescription = "VolleyRef",
                            tint = CourtDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "VOLLEYREF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "FIVB 6x6 Referee Console",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Quick Navigation Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.openLineupDialog() },
                        modifier = Modifier.size(36.dp).testTag("nav_set_lineup_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = "Set Line-up Sheet",
                            tint = VolleyballGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(RefereeScreen.MATCH_LOG) },
                        modifier = Modifier.size(36.dp).testTag("nav_match_log_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Score History",
                            tint = RefereeWhistle,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(RefereeScreen.RULES_CONFIG) },
                        modifier = Modifier.size(36.dp).testTag("nav_rules_config_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "FIVB Rules Settings",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(RefereeScreen.MATCH_SETUP) },
                        modifier = Modifier.size(36.dp).testTag("nav_match_setup_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Match Setup",
                            tint = VolleyballGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Scoreboard
            ScoreBoardHeader(
                matchState = matchState,
                clockState = clockState,
                onSwitchSidesClick = { viewModel.switchCourtSides() },
                onTimerToggleClick = { viewModel.toggleClock() },
                onToggleServingTeamClick = { viewModel.toggleServingTeam() }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Real-time Match & Set Clock
            MatchTimerBar(
                clockState = clockState,
                currentSetNumber = matchState.currentSet,
                onStartClick = { viewModel.startClock() },
                onPauseClick = { viewModel.pauseClock() },
                onResetClick = { viewModel.resetClock() },
                onAdjustTime = { delta -> viewModel.adjustClock(delta) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Starting Lineup Verification Pill (FIVB Rule 7.3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    onClick = { viewModel.openLineupDialog() },
                    shape = RoundedCornerShape(20.dp),
                    color = CourtSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CourtLines),
                    modifier = Modifier.testTag("quick_lineup_sheet_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = VolleyballGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Set ${matchState.currentSet} Line-up Sheet (Pos I-VI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (matchState.setLineupHistory.containsKey(matchState.currentSet)) Color(0xFF064E3B) else Color(0xFF78350F))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (matchState.setLineupHistory.containsKey(matchState.currentSet)) "REGISTERED" else "EDIT / VERIFY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (matchState.setLineupHistory.containsKey(matchState.currentSet)) Color(0xFF34D399) else VolleyballGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Set Finished / Match Won Banner
            if (matchState.isMatchFinished) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("match_won_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VolleyballGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = VolleyballGold, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val winner = if (matchState.winnerTeamId == matchState.teamA.id) matchState.teamA else matchState.teamB
                            Text(
                                text = "MATCH WON BY ${winner.displayName.uppercase()}!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = VolleyballGold
                            )
                        }
                        Text(
                            text = "Final Sets: ${matchState.setsWonTeamA} - ${matchState.setsWonTeamB}",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showExportReportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                                modifier = Modifier.testTag("match_finished_export_btn")
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Report", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.navigateTo(RefereeScreen.MATCH_LOG) },
                                colors = ButtonDefaults.buttonColors(containerColor = VolleyballGold, contentColor = CourtDark)
                            ) {
                                Text("Score History", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(RefereeScreen.MATCH_SETUP) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("New Match")
                            }
                        }
                    }
                }
            } else if (matchState.isSetFinished) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("set_won_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "SET ${matchState.currentSet} COMPLETED!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF34D399)
                            )
                            val winner = if (matchState.scoreTeamA > matchState.scoreTeamB) matchState.teamA else matchState.teamB
                            Text(
                                text = "Won by ${winner.name} (${matchState.scoreTeamA}-${matchState.scoreTeamB})",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { viewModel.startNextSet() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = CourtDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_next_set_btn")
                        ) {
                            Text("Start Set ${matchState.currentSet + 1}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Technical Timeout Banner Alert
            technicalTimeoutAlert?.let { alert ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = VolleyballGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = alert, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        TextButton(onClick = { viewModel.dismissTechnicalTimeoutAlert() }) {
                            Text("Dismiss", color = VolleyballGold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Deciding Set Side Switch Alert
            pendingSideSwitchMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RefereeWhistle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = RefereeWhistle, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = msg, fontSize = 12.sp, color = Color.White)
                        }
                        TextButton(onClick = { viewModel.dismissSideSwitchAlert() }) {
                            Text("OK", color = RefereeWhistle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 6x6 Interactive Court Visualization
            Court6x6View(
                matchState = matchState,
                onPlayerClick = { teamId, pos, player ->
                    val team = if (teamId == matchState.teamA.id) matchState.teamA else matchState.teamB
                    preSelectedPlayerForSub = player
                    showSubstitutionSheetForTeam = team
                },
                onManualRotateClick = { teamId, clockwise ->
                    viewModel.manualRotate(teamId, clockwise)
                },
                onToggleServingTeamClick = { viewModel.toggleServingTeam() }
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Referee Controls Section
            RefereeControlsSection(
                teamLeft = teamLeft,
                teamRight = teamRight,
                canUndo = canUndo,
                onScorePoint = { team, detailed ->
                    if (detailed) {
                        showPointReasonDialogForTeam = team
                    } else {
                        viewModel.scorePoint(team.id)
                    }
                },
                onTimeout = { team -> viewModel.requestTimeout(team.id) },
                onSub = { team ->
                    preSelectedPlayerForSub = null
                    showSubstitutionSheetForTeam = team
                },
                onUndo = { viewModel.undo() },
                onSanction = { showSanctionDialog = true }
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }

    // Active Timeout Timer Dialog
    activeTimeoutSeconds?.let { sec ->
        TimeoutTimerDialog(
            initialSeconds = sec,
            team = activeTimeoutTeam,
            onDismiss = { viewModel.dismissTimeoutTimer() },
            onFinished = { viewModel.onTimeoutTimerFinished() }
        )
    }

    // Detailed Point Reason Dialog
    showPointReasonDialogForTeam?.let { scoringTeam ->
        PointReasonDialog(
            scoringTeam = scoringTeam,
            onDismiss = { showPointReasonDialogForTeam = null },
            onSelectReason = { reasonType, reasonLabel ->
                viewModel.scorePoint(
                    benefitingTeamId = scoringTeam.id,
                    eventType = reasonType,
                    note = reasonLabel
                )
            }
        )
    }

    // Substitution Sheet
    showSubstitutionSheetForTeam?.let { team ->
        val rotation = if (team.id == matchState.teamA.id) matchState.teamARotation else matchState.teamBRotation
        SubstitutionSheet(
            team = team,
            rotation = rotation,
            maxSubs = matchState.rules.maxSubstitutionsPerSet,
            enforcePairing = matchState.rules.enforceFivbSubPairing,
            preSelectedPlayerOut = preSelectedPlayerForSub,
            onDismiss = { showSubstitutionSheetForTeam = null },
            onConfirmSubstitution = { playerOut, playerIn ->
                viewModel.performSubstitution(team.id, playerOut, playerIn)
            },
            onLiberoReplacement = { pos, libero ->
                viewModel.performLiberoReplacement(team.id, pos, libero)
            }
        )
    }

    // Sanction Dialog
    if (showSanctionDialog) {
        SanctionDialog(
            teamA = matchState.teamA,
            teamB = matchState.teamB,
            onDismiss = { showSanctionDialog = false },
            onApplySanction = { teamId, type, player, note ->
                viewModel.applySanction(teamId, type, player, note)
            }
        )
    }

    // Official FIVB Set Line-up Entry Dialog
    if (showLineupDialog) {
        SetLineupEntryDialog(
            matchState = matchState,
            targetSetNumber = matchState.currentSet,
            onDismiss = { viewModel.closeLineupDialog() },
            onUpdatePlayer = { teamId, player ->
                viewModel.updatePlayer(teamId, player)
            },
            onUpdateTeamName = { teamId, newName, newCode ->
                viewModel.updateTeamName(teamId, newName, newCode)
            },
            onConfirmLineups = { setNum, teamALineup, teamBLineup, teamALibero, teamBLibero, serverId ->
                viewModel.applyStartingLineups(
                    setNumber = setNum,
                    teamALineup = teamALineup,
                    teamBLineup = teamBLineup,
                    teamALibero = teamALibero,
                    teamBLibero = teamBLibero,
                    firstServingTeamId = serverId
                )
            }
        )
    }

    if (showExportReportDialog) {
        ExportReportDialog(
            matchState = matchState,
            onDismiss = { showExportReportDialog = false }
        )
    }
}

@Composable
private fun RefereeControlsSection(
    teamLeft: Team,
    teamRight: Team,
    canUndo: Boolean,
    onScorePoint: (team: Team, detailed: Boolean) -> Unit,
    onTimeout: (team: Team) -> Unit,
    onSub: (team: Team) -> Unit,
    onUndo: () -> Unit,
    onSanction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("referee_controls_section"),
        color = CourtSurface,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Main Big Point Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Point Left Team Button
                Button(
                    onClick = { onScorePoint(teamLeft, false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("score_left_point_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(teamLeft.colorHex),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "+1 ${teamLeft.shortCode}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "POINT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                // Center Fast Tools: Undo & Sanction
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Undo Button
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (canUndo) CourtSurfaceHigh else Color(0xFF1E293B), CircleShape)
                            .border(1.dp, if (canUndo) RefereeWhistle else Color(0xFF334155), CircleShape)
                            .testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) RefereeWhistle else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Sanction Button
                    IconButton(
                        onClick = onSanction,
                        modifier = Modifier
                            .size(28.dp)
                            .background(CourtSurfaceHigh, CircleShape)
                            .border(1.dp, SanctionYellow, CircleShape)
                            .testTag("sanction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Sanction Cards",
                            tint = SanctionYellow,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Point Right Team Button
                Button(
                    onClick = { onScorePoint(teamRight, false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("score_right_point_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(teamRight.colorHex),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "+1 ${teamRight.shortCode}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "POINT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub, Timeout & Detailed Rally Tag Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Team: Timeout & Sub
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { onTimeout(teamLeft) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("timeout_left_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Timer, null, tint = SanctionYellow, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("T.O.", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSub(teamLeft) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("sub_left_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.SwapVert, null, tint = RefereeWhistle, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("SUB", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Center: Detailed Tag button
                TextButton(
                    onClick = { onScorePoint(teamLeft, true) },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp).testTag("tag_rally_btn")
                ) {
                    Text("Tag Rally...", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }

                // Right Team: Sub & Timeout
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { onSub(teamRight) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("sub_right_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.SwapVert, null, tint = RefereeWhistle, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("SUB", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onTimeout(teamRight) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag("timeout_right_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Timer, null, tint = SanctionYellow, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("T.O.", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
