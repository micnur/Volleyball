package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import com.example.util.BluetoothConnectionState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import com.example.model.RefereeCorps
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.FivbRulesConfig
import com.example.model.MatchFormat
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.VolleyballGold
import com.example.ui.viewmodel.RefereeScreen
import com.example.ui.viewmodel.RefereeViewModel

@Composable
fun FivbRulesConfigScreen(
    viewModel: RefereeViewModel,
    modifier: Modifier = Modifier
) {
    val matchState by viewModel.matchState.collectAsState()
    val initialRules = matchState.rules

    BackHandler {
        viewModel.navigateTo(RefereeScreen.MATCH)
    }

    var format by remember { mutableStateOf(initialRules.format) }
    var pointsRegular by remember { mutableIntStateOf(initialRules.pointsRegularSet) }
    var pointsDeciding by remember { mutableIntStateOf(initialRules.pointsDecidingSet) }
    var advantage by remember { mutableIntStateOf(initialRules.advantageRequired) }
    var isCapped by remember { mutableStateOf(initialRules.pointCapRegular != null) }
    var capPointsRegular by remember { mutableIntStateOf(initialRules.pointCapRegular ?: 27) }
    var timeoutsCount by remember { mutableIntStateOf(initialRules.timeoutsPerTeamPerSet) }
    var timeoutDuration by remember { mutableIntStateOf(initialRules.timeoutDurationSeconds) }
    var techTimeouts by remember { mutableStateOf(initialRules.technicalTimeoutsEnabled) }
    var maxSubs by remember { mutableIntStateOf(initialRules.maxSubstitutionsPerSet) }
    var enforceSubPairing by remember { mutableStateOf(initialRules.enforceFivbSubPairing) }
    var allowLibero by remember { mutableStateOf(initialRules.allowLibero) }
    var switchSidesBetweenSets by remember { mutableStateOf(initialRules.switchSidesBetweenSets) }
    var decidingSwitchPoints by remember { mutableIntStateOf(initialRules.decidingSetSwitchPoints) }
    var whistleAudio by remember { mutableStateOf(initialRules.whistleAudioEnabled) }
    var hapticFeedback by remember { mutableStateOf(initialRules.hapticFeedbackEnabled) }

    val initialOfficials = matchState.officials
    var firstReferee by remember { mutableStateOf(initialOfficials.firstReferee) }
    var secondReferee by remember { mutableStateOf(initialOfficials.secondReferee) }
    var scorer1 by remember { mutableStateOf(initialOfficials.scorer1) }
    var scorer2 by remember { mutableStateOf(initialOfficials.scorer2) }
    var scorer3 by remember { mutableStateOf(initialOfficials.scorer3) }
    var lineReferee1 by remember { mutableStateOf(initialOfficials.lineReferee1) }
    var lineReferee2 by remember { mutableStateOf(initialOfficials.lineReferee2) }

    fun buildCurrentOfficials(): RefereeCorps = RefereeCorps(
        firstReferee = firstReferee.trim(),
        secondReferee = secondReferee.trim(),
        scorer1 = scorer1.trim(),
        scorer2 = scorer2.trim(),
        scorer3 = scorer3.trim(),
        lineReferee1 = lineReferee1.trim(),
        lineReferee2 = lineReferee2.trim()
    )

    fun buildCurrentConfig(): FivbRulesConfig {
        return FivbRulesConfig(
            format = format,
            pointsRegularSet = pointsRegular,
            pointsDecidingSet = pointsDeciding,
            advantageRequired = advantage,
            pointCapRegular = if (isCapped) capPointsRegular else null,
            pointCapDeciding = if (isCapped) (capPointsRegular - (pointsRegular - pointsDeciding)).coerceAtLeast(pointsDeciding + 1) else null,
            timeoutsPerTeamPerSet = timeoutsCount,
            timeoutDurationSeconds = timeoutDuration,
            technicalTimeoutsEnabled = techTimeouts,
            maxSubstitutionsPerSet = maxSubs,
            enforceFivbSubPairing = enforceSubPairing,
            allowLibero = allowLibero,
            switchSidesBetweenSets = switchSidesBetweenSets,
            decidingSetSwitchPoints = decidingSwitchPoints,
            whistleAudioEnabled = whistleAudio,
            hapticFeedbackEnabled = hapticFeedback
        )
    }

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
                    .background(CourtSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(RefereeScreen.MATCH) },
                        modifier = Modifier.testTag("rules_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FIVB Rules Customizer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Apply button
                Button(
                    onClick = {
                        viewModel.updateRules(buildCurrentConfig())
                        viewModel.updateOfficials(buildCurrentOfficials())
                        viewModel.navigateTo(RefereeScreen.MATCH)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VolleyballGold, contentColor = CourtDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("apply_rules_btn")
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Rules", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Big Scoreboard & Bluetooth Sync Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, VolleyballGold),
                modifier = Modifier.testTag("settings_big_scoreboard_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = VolleyballGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "BIG SCOREBOARD DISPLAY",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Full-Screen Score & Timeouts (FIVB Rule 15.1)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VolleyballGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NEW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = CourtDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Turn this device into a giant digital scoreboard displaying real-time points, sets won, timeouts taken & remaining, and the match clock. Use standalone or sync between two Android devices via Bluetooth (Device 1: VOLLEYREF Controller, Device 2: Big Scoreboard Display).",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.navigateTo(RefereeScreen.BIG_SCOREBOARD) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("settings_open_big_scoreboard_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VolleyballGold,
                            contentColor = CourtDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Tv, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Big Scoreboard", fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.startBluetoothServer()
                                viewModel.navigateTo(RefereeScreen.BIG_SCOREBOARD)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, CourtLines)
                        ) {
                            Icon(Icons.Default.Bluetooth, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Host as Referee", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.toggleBluetoothSimulation()
                                viewModel.navigateTo(RefereeScreen.BIG_SCOREBOARD)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, CourtLines)
                        ) {
                            Text("Demo Simulation", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Rule Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("QUICK RULE PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val def = FivbRulesConfig.defaultFivb()
                                format = def.format
                                pointsRegular = def.pointsRegularSet
                                pointsDeciding = def.pointsDecidingSet
                                isCapped = false
                                maxSubs = def.maxSubstitutionsPerSet
                                enforceSubPairing = def.enforceFivbSubPairing
                                techTimeouts = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Official FIVB", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val rec = FivbRulesConfig.recreationalFast()
                                format = rec.format
                                pointsRegular = rec.pointsRegularSet
                                pointsDeciding = rec.pointsDecidingSet
                                isCapped = true
                                capPointsRegular = rec.pointCapRegular ?: 25
                                maxSubs = rec.maxSubstitutionsPerSet
                                enforceSubPairing = false
                                techTimeouts = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Fast (21 pts)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val youth = FivbRulesConfig.youthLeague()
                                format = youth.format
                                pointsRegular = youth.pointsRegularSet
                                pointsDeciding = youth.pointsDecidingSet
                                isCapped = false
                                maxSubs = youth.maxSubstitutionsPerSet
                                enforceSubPairing = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Youth/Club", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Match Format
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("MATCH FORMAT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    MatchFormat.entries.forEach { f ->
                        val isSelected = format == f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CourtSurfaceHigh else Color(0xFF0F172A))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) VolleyballGold else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { format = f }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = f.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // Set Scoring Rules
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SET SCORING RULES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Regular Sets Target
                    Text("Regular Sets Target: $pointsRegular points", fontSize = 13.sp, color = Color.White)
                    Slider(
                        value = pointsRegular.toFloat(),
                        onValueChange = { pointsRegular = it.toInt() },
                        valueRange = 15f..35f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = VolleyballGold, activeTrackColor = VolleyballGold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Deciding Set Target
                    Text("Deciding Set Target: $pointsDeciding points", fontSize = 13.sp, color = Color.White)
                    Slider(
                        value = pointsDeciding.toFloat(),
                        onValueChange = { pointsDeciding = it.toInt() },
                        valueRange = 7f..25f,
                        steps = 17,
                        colors = SliderDefaults.colors(thumbColor = RefereeWhistle, activeTrackColor = RefereeWhistle)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Deciding Set Side Switch point threshold
                    Text("Deciding Set Side Switch at: $decidingSwitchPoints points", fontSize = 13.sp, color = Color.White)
                    Slider(
                        value = decidingSwitchPoints.toFloat(),
                        onValueChange = { decidingSwitchPoints = it.toInt() },
                        valueRange = 4f..15f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = VolleyballGold, activeTrackColor = VolleyballGold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Point Cap Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cap Max Points (No unlimited deuce)", fontSize = 13.sp, color = Color.White)
                            Text(
                                text = if (isCapped) "Capped at $capPointsRegular points" else "Official FIVB: Uncapped (+2 advantage)",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isCapped,
                            onCheckedChange = { isCapped = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = VolleyballGold)
                        )
                    }

                    if (isCapped) {
                        Slider(
                            value = capPointsRegular.toFloat(),
                            onValueChange = { capPointsRegular = it.toInt() },
                            valueRange = (pointsRegular + 1).toFloat()..40f,
                            steps = (40 - pointsRegular - 2).coerceAtLeast(0),
                            colors = SliderDefaults.colors(thumbColor = VolleyballGold, activeTrackColor = VolleyballGold)
                        )
                    }
                }
            }

            // Timeouts & Substitutions Rules
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("TIMEOUTS & SUBSTITUTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Timeouts Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Timeouts per team per set:", fontSize = 13.sp, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 3).forEach { count ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (timeoutsCount == count) VolleyballGold else CourtSurfaceHigh)
                                        .clickable { timeoutsCount = count }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontWeight = FontWeight.Bold,
                                        color = if (timeoutsCount == count) CourtDark else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Technical Timeouts Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FIVB Technical Timeouts", fontSize = 13.sp, color = Color.White)
                            Text("Automatic 60s at 8th and 16th points (Sets 1-4)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = techTimeouts,
                            onCheckedChange = { techTimeouts = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = VolleyballGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Max Substitutions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Substitutions per set:", fontSize = 13.sp, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(6, 8, 12).forEach { subs ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (maxSubs == subs) RefereeWhistle else CourtSurfaceHigh)
                                        .clickable { maxSubs = subs }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$subs" + (if (subs == 6) " (FIVB)" else ""),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (maxSubs == subs) CourtDark else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Enforce FIVB 15.6 Sub Pairing Rule
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enforce FIVB Rule 15.6 Pairing", fontSize = 13.sp, color = Color.White)
                            Text("Players can only re-enter for their original counterpart", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = enforceSubPairing,
                            onCheckedChange = { enforceSubPairing = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = VolleyballGold)
                        )
                    }
                }
            }

            // Sound & Haptic Feedback Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("REFEREE AUDIO & HAPTICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Whistle Audio Tones (Points & Timeouts)", fontSize = 13.sp, color = Color.White)
                        Switch(
                            checked = whistleAudio,
                            onCheckedChange = { whistleAudio = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = VolleyballGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Haptic Vibrations", fontSize = 13.sp, color = Color.White)
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = { hapticFeedback = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = VolleyballGold)
                        )
                    }
                }
            }

            // Match Officials & Referee Corps (1st referee, 2nd referee, 3 scorers, 2 line referees)
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("match_officials_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MATCH OFFICIALS & REFEREES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "FIVB Chapter 8: Referees, Scorers & Line Judges",
                                fontSize = 10.sp,
                                color = VolleyballGold
                            )
                        }
                        if (firstReferee.isNotBlank() || secondReferee.isNotBlank() || scorer1.isNotBlank() ||
                            scorer2.isNotBlank() || scorer3.isNotBlank() || lineReferee1.isNotBlank() || lineReferee2.isNotBlank()
                        ) {
                            TextButton(
                                onClick = {
                                    firstReferee = ""
                                    secondReferee = ""
                                    scorer1 = ""
                                    scorer2 = ""
                                    scorer3 = ""
                                    lineReferee1 = ""
                                    lineReferee2 = ""
                                    viewModel.updateOfficials(RefereeCorps())
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("clear_officials_btn")
                            ) {
                                Text("Clear All", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1st & 2nd Referees
                    Text(
                        text = "REFEREES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OfficialInputField(
                        value = firstReferee,
                        onValueChange = {
                            firstReferee = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(firstReferee = it.trim()))
                        },
                        label = "1st Referee",
                        placeholder = "Name of 1st Referee",
                        testTag = "input_first_referee"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OfficialInputField(
                        value = secondReferee,
                        onValueChange = {
                            secondReferee = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(secondReferee = it.trim()))
                        },
                        label = "2nd Referee",
                        placeholder = "Name of 2nd Referee",
                        testTag = "input_second_referee"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scorers Section (3 scorers)
                    Text(
                        text = "SCORERS (3 OFFICIALS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OfficialInputField(
                        value = scorer1,
                        onValueChange = {
                            scorer1 = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(scorer1 = it.trim()))
                        },
                        label = "Scorer 1 (Official Scorer)",
                        placeholder = "Name of 1st Scorer",
                        testTag = "input_scorer_1"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OfficialInputField(
                        value = scorer2,
                        onValueChange = {
                            scorer2 = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(scorer2 = it.trim()))
                        },
                        label = "Scorer 2 (Assistant Scorer)",
                        placeholder = "Name of 2nd Scorer",
                        testTag = "input_scorer_2"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OfficialInputField(
                        value = scorer3,
                        onValueChange = {
                            scorer3 = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(scorer3 = it.trim()))
                        },
                        label = "Scorer 3",
                        placeholder = "Name of 3rd Scorer",
                        testTag = "input_scorer_3"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line Referees Section (2 line judges)
                    Text(
                        text = "LINE REFEREES (2 JUDGES)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OfficialInputField(
                        value = lineReferee1,
                        onValueChange = {
                            lineReferee1 = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(lineReferee1 = it.trim()))
                        },
                        label = "Line Referee 1",
                        placeholder = "Name of Line Referee 1",
                        testTag = "input_line_referee_1"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OfficialInputField(
                        value = lineReferee2,
                        onValueChange = {
                            lineReferee2 = it
                            viewModel.updateOfficials(buildCurrentOfficials().copy(lineReferee2 = it.trim()))
                        },
                        label = "Line Referee 2",
                        placeholder = "Name of Line Referee 2",
                        testTag = "input_line_referee_2"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun OfficialInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp, color = Color(0xFF64748B)) },
        singleLine = true,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear $label",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VolleyballGold,
            unfocusedBorderColor = CourtLines,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = VolleyballGold,
            unfocusedLabelColor = Color(0xFF94A3B8)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
