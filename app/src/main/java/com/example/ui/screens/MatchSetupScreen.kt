package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.example.data.repository.MatchRepository
import com.example.model.Player
import com.example.model.PositionRole
import com.example.model.Team
import com.example.ui.components.EditPlayerDialog
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
    var teamARoster by remember {
        mutableStateOf(currentMatch.teamA.roster.ifEmpty { MatchRepository.createDefaultTeamA().roster })
    }

    var teamBName by remember { mutableStateOf(currentMatch.teamB.name) }
    var teamBCode by remember { mutableStateOf(currentMatch.teamB.shortCode) }
    var teamBColorHex by remember { mutableStateOf(currentMatch.teamB.colorHex) }
    var teamBRoster by remember {
        mutableStateOf(currentMatch.teamB.roster.ifEmpty { MatchRepository.createDefaultTeamB().roster })
    }

    var serverFirstTeamId by remember { mutableStateOf("team_a") }
    var playerToEditInSetup by remember { mutableStateOf<Pair<String, Player?>?>(null) } // "team_a" / "team_b" to player

    fun startConfiguredMatch() {
        val teamA = Team(
            id = "team_a",
            name = teamAName.ifBlank { "Team A" },
            shortCode = teamACode.ifBlank { "TMA" }.take(4).uppercase(),
            colorHex = teamAColorHex,
            roster = teamARoster
        )

        val teamB = Team(
            id = "team_b",
            name = teamBName.ifBlank { "Team B" },
            shortCode = teamBCode.ifBlank { "TMB" }.take(4).uppercase(),
            colorHex = teamBColorHex,
            roster = teamBRoster
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Team A Line-up Player Editing Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Line-up Players (${teamARoster.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        TextButton(
                            onClick = { playerToEditInSetup = Pair("team_a", null) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("add_player_team_a_btn")
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(13.dp), tint = VolleyballGold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+ Add Player", fontSize = 11.sp, color = VolleyballGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(teamARoster) { player ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CourtSurfaceHigh,
                                border = BorderStroke(1.dp, if (player.isLibero) Color(0xFFA855F7) else CourtLines),
                                modifier = Modifier
                                    .clickable { playerToEditInSetup = Pair("team_a", player) }
                                    .testTag("setup_player_a_${player.number}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "#${player.number}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (player.isLibero) Color(0xFFD8B4FE) else Color.White
                                            )
                                            if (player.isCaptain) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("(C)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VolleyballGold)
                                            }
                                            if (player.isLibero) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("[L]", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                                            }
                                        }
                                        Text(
                                            text = player.name.ifBlank { "Player #${player.number}" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFCBD5E1),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Player Name / Number",
                                        tint = VolleyballGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Team B Line-up Player Editing Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Line-up Players (${teamBRoster.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        TextButton(
                            onClick = { playerToEditInSetup = Pair("team_b", null) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("add_player_team_b_btn")
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(13.dp), tint = VolleyballGold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+ Add Player", fontSize = 11.sp, color = VolleyballGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(teamBRoster) { player ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CourtSurfaceHigh,
                                border = BorderStroke(1.dp, if (player.isLibero) Color(0xFFA855F7) else CourtLines),
                                modifier = Modifier
                                    .clickable { playerToEditInSetup = Pair("team_b", player) }
                                    .testTag("setup_player_b_${player.number}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "#${player.number}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (player.isLibero) Color(0xFFD8B4FE) else Color.White
                                            )
                                            if (player.isCaptain) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("(C)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VolleyballGold)
                                            }
                                            if (player.isLibero) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("[L]", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD8B4FE))
                                            }
                                        }
                                        Text(
                                            text = player.name.ifBlank { "Player #${player.number}" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFCBD5E1),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Player Name / Number",
                                        tint = VolleyballGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
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

    // Edit Player Dialog in Match Setup
    playerToEditInSetup?.let { (teamKey, player) ->
        val teamName = if (teamKey == "team_a") teamAName.ifBlank { "Team A" } else teamBName.ifBlank { "Team B" }
        val teamColor = if (teamKey == "team_a") teamAColorHex else teamBColorHex
        EditPlayerDialog(
            player = player,
            teamName = teamName,
            teamColorHex = teamColor,
            onDismiss = { playerToEditInSetup = null },
            onSavePlayer = { updated ->
                if (teamKey == "team_a") {
                    val idx = teamARoster.indexOfFirst { it.id == updated.id }
                    teamARoster = if (idx >= 0) {
                        teamARoster.toMutableList().apply { set(idx, updated) }
                    } else {
                        teamARoster + updated
                    }
                } else {
                    val idx = teamBRoster.indexOfFirst { it.id == updated.id }
                    teamBRoster = if (idx >= 0) {
                        teamBRoster.toMutableList().apply { set(idx, updated) }
                    } else {
                        teamBRoster + updated
                    }
                }
                playerToEditInSetup = null
            }
        )
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
