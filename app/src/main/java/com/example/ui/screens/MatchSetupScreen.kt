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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.data.repository.MatchRepository
import com.example.model.Player
import com.example.model.PositionRole
import com.example.model.Team
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.VolleyballGold
import com.example.ui.viewmodel.RefereeScreen
import com.example.ui.viewmodel.RefereeViewModel

@Composable
fun MatchSetupScreen(
    viewModel: RefereeViewModel,
    modifier: Modifier = Modifier
) {
    val currentMatch by viewModel.matchState.collectAsState()

    BackHandler {
        viewModel.navigateTo(RefereeScreen.MATCH)
    }

    var teamAName by remember { mutableStateOf(currentMatch.teamA.name) }
    var teamACode by remember { mutableStateOf(currentMatch.teamA.shortCode) }
    var teamAColorHex by remember { mutableStateOf(currentMatch.teamA.colorHex) }

    var teamBName by remember { mutableStateOf(currentMatch.teamB.name) }
    var teamBCode by remember { mutableStateOf(currentMatch.teamB.shortCode) }
    var teamBColorHex by remember { mutableStateOf(currentMatch.teamB.colorHex) }

    var serverFirstTeamId by remember { mutableStateOf("team_a") }

    fun startConfiguredMatch() {
        val rosterA = currentMatch.teamA.roster.ifEmpty { MatchRepository.createDefaultTeamA().roster }
        val rosterB = currentMatch.teamB.roster.ifEmpty { MatchRepository.createDefaultTeamB().roster }

        val teamA = Team(
            id = "team_a",
            name = teamAName.ifBlank { "Team A" },
            shortCode = teamACode.ifBlank { "TMA" }.take(4).uppercase(),
            colorHex = teamAColorHex,
            roster = rosterA
        )

        val teamB = Team(
            id = "team_b",
            name = teamBName.ifBlank { "Team B" },
            shortCode = teamBCode.ifBlank { "TMB" }.take(4).uppercase(),
            colorHex = teamBColorHex,
            roster = rosterB
        )

        viewModel.startNewMatch(
            teamA = teamA,
            teamB = teamB,
            rules = currentMatch.rules,
            initialServerTeamId = serverFirstTeamId
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(RefereeScreen.MATCH) },
                    modifier = Modifier.testTag("match_setup_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "New Match Setup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
            // Quick Team Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("POPULAR MATCHUPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                teamAName = "Brazil"
                                teamACode = "BRA"
                                teamAColorHex = 0xFFF59E0B
                                teamBName = "Poland"
                                teamBCode = "POL"
                                teamBColorHex = 0xFFEF4444
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("BRA vs POL", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                teamAName = "Italy"
                                teamACode = "ITA"
                                teamAColorHex = 0xFF3B82F6
                                teamBName = "USA"
                                teamBCode = "USA"
                                teamBColorHex = 0xFFEF4444
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("ITA vs USA", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                teamAName = "Japan"
                                teamACode = "JPN"
                                teamAColorHex = 0xFFEF4444
                                teamBName = "France"
                                teamBCode = "FRA"
                                teamBColorHex = 0xFF3B82F6
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("JPN vs FRA", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Team A Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(teamAColorHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEAM A CONFIGURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = teamAName,
                            onValueChange = { teamAName = it },
                            label = { Text("Team Name") },
                            modifier = Modifier.weight(2f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VolleyballGold,
                                unfocusedBorderColor = CourtLines,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = teamACode,
                            onValueChange = { teamACode = it.take(4).uppercase() },
                            label = { Text("Code") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VolleyballGold,
                                unfocusedBorderColor = CourtLines,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ShirtColorSelector(
                        selectedColorHex = teamAColorHex,
                        onColorSelected = { teamAColorHex = it }
                    )
                }
            }

            // Team B Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(teamBColorHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEAM B CONFIGURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = teamBName,
                            onValueChange = { teamBName = it },
                            label = { Text("Team Name") },
                            modifier = Modifier.weight(2f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VolleyballGold,
                                unfocusedBorderColor = CourtLines,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = teamBCode,
                            onValueChange = { teamBCode = it.take(4).uppercase() },
                            label = { Text("Code") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VolleyballGold,
                                unfocusedBorderColor = CourtLines,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ShirtColorSelector(
                        selectedColorHex = teamBColorHex,
                        onColorSelected = { teamBColorHex = it }
                    )
                }
            }

            // Coin Toss / First Serving Team
            Card(
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("COIN TOSS: FIRST SERVING TEAM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (serverFirstTeamId == "team_a") CourtSurfaceHigh else Color(0xFF0F172A))
                                .border(
                                    width = if (serverFirstTeamId == "team_a") 2.dp else 1.dp,
                                    color = if (serverFirstTeamId == "team_a") VolleyballGold else CourtLines,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { serverFirstTeamId = "team_a" }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = teamAName.ifBlank { "Team A" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (serverFirstTeamId == "team_a") VolleyballGold else Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (serverFirstTeamId == "team_b") CourtSurfaceHigh else Color(0xFF0F172A))
                                .border(
                                    width = if (serverFirstTeamId == "team_b") 2.dp else 1.dp,
                                    color = if (serverFirstTeamId == "team_b") VolleyballGold else CourtLines,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { serverFirstTeamId = "team_b" }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = teamBName.ifBlank { "Team B" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (serverFirstTeamId == "team_b") VolleyballGold else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Start Match Button
            Button(
                onClick = { startConfiguredMatch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_new_match_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = VolleyballGold, contentColor = CourtDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Match & Open Stand Console", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

data class VolleyballShirtColor(
    val hex: Long,
    val name: String,
    val checkTint: Long = 0xFFFFFFFF
)

val FIVB_SHIRT_PALETTE = listOf(
    VolleyballShirtColor(0xFF2563EB, "Royal Blue"),
    VolleyballShirtColor(0xFF1E3A8A, "Navy Blue"),
    VolleyballShirtColor(0xFF0284C7, "Sky Cyan"),
    VolleyballShirtColor(0xFF0D9488, "Teal Green"),
    VolleyballShirtColor(0xFF059669, "Pine Green"),
    VolleyballShirtColor(0xFF84CC16, "Neon Lime", checkTint = 0xFF000000),
    VolleyballShirtColor(0xFFEAB308, "Volley Gold", checkTint = 0xFF000000),
    VolleyballShirtColor(0xFFF59E0B, "Amber Orange"),
    VolleyballShirtColor(0xFFEA580C, "Tangerine"),
    VolleyballShirtColor(0xFFDC2626, "Crimson Red"),
    VolleyballShirtColor(0xFF881337, "Burgundy Wine"),
    VolleyballShirtColor(0xFFDB2777, "Hot Pink"),
    VolleyballShirtColor(0xFF7C3AED, "Electric Violet"),
    VolleyballShirtColor(0xFF4C1D95, "Deep Indigo"),
    VolleyballShirtColor(0xFF1E293B, "Charcoal Black"),
    VolleyballShirtColor(0xFFE2E8F0, "Silver White", checkTint = 0xFF000000)
)

@Composable
private fun ShirtColorSelector(
    selectedColorHex: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentColor = FIVB_SHIRT_PALETTE.find { it.hex == selectedColorHex }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jersey / Shirt Color",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(selectedColorHex))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentColor?.name ?: "Custom Color",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Display 2 rows of 8 swatches
        val chunked = FIVB_SHIRT_PALETTE.chunked(8)
        chunked.forEach { rowColors ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowColors.forEach { shirtColor ->
                    val isSelected = selectedColorHex == shirtColor.hex
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(shirtColor.hex))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else CourtLines,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(shirtColor.hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(shirtColor.checkTint))
                            )
                        }
                    }
                }
            }
        }
    }
}
