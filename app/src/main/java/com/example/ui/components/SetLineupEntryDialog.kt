package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MatchState
import com.example.model.Player
import com.example.model.Team
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.LiberoBadge
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.SanctionRed
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold

private val ROMAN_POSITIONS = mapOf(
    1 to "I (Server)",
    2 to "II",
    3 to "III",
    4 to "IV",
    5 to "V",
    6 to "VI"
)

@Composable
fun SetLineupEntryDialog(
    matchState: MatchState,
    targetSetNumber: Int = matchState.currentSet,
    onDismiss: () -> Unit,
    onUpdatePlayer: ((teamId: String, player: Player) -> Unit)? = null,
    onConfirmLineups: (
        setNumber: Int,
        teamALineup: Map<Int, Player>,
        teamBLineup: Map<Int, Player>,
        teamALibero: Player?,
        teamBLibero: Player?,
        firstServingTeamId: String
    ) -> Unit
) {
    var activeTeamTab by remember { mutableIntStateOf(0) } // 0 = Team A, 1 = Team B

    // Mutable Rosters to support adding & editing players
    var rosterA by remember { mutableStateOf(matchState.teamA.roster) }
    var rosterB by remember { mutableStateOf(matchState.teamB.roster) }

    // Dialog state for editing a player's name and shirt number
    var playerBeingEdited by remember { mutableStateOf<Player?>(null) }
    var showEditPlayerDialog by remember { mutableStateOf(false) }

    // Team A Lineup State (Positions 1..6)
    var teamALineup by remember {
        val existing = matchState.setLineupHistory[targetSetNumber]?.teamALineup
            ?: matchState.teamARotation.startingLineup.ifEmpty { matchState.teamARotation.positions }
        mutableStateOf(existing.toMutableMap())
    }

    // Team B Lineup State (Positions 1..6)
    var teamBLineup by remember {
        val existing = matchState.setLineupHistory[targetSetNumber]?.teamBLineup
            ?: matchState.teamBRotation.startingLineup.ifEmpty { matchState.teamBRotation.positions }
        mutableStateOf(existing.toMutableMap())
    }

    // Selected position slot being edited (1..6)
    var activePositionSlot by remember { mutableIntStateOf(1) }

    // Designated Liberos
    var teamALibero by remember {
        mutableStateOf(matchState.teamA.getLiberos().firstOrNull())
    }
    var teamBLibero by remember {
        mutableStateOf(matchState.teamB.getLiberos().firstOrNull())
    }

    // First Serving Team for the set
    var firstServingTeamId by remember {
        mutableStateOf(
            matchState.setLineupHistory[targetSetNumber]?.firstServingTeamId.takeIf { !it.isNullOrBlank() }
                ?: if (targetSetNumber % 2 == 1) matchState.teamA.id else matchState.teamB.id
        )
    }

    val activeTeam = if (activeTeamTab == 0) matchState.teamA else matchState.teamB
    val currentLineup = if (activeTeamTab == 0) teamALineup else teamBLineup

    fun updateCurrentLineup(newMap: MutableMap<Int, Player>) {
        if (activeTeamTab == 0) {
            teamALineup = newMap
        } else {
            teamBLineup = newMap
        }
    }

    val isTeamAComplete = teamALineup.size == 6 && (1..6).all { teamALineup[it] != null }
    val isTeamBComplete = teamBLineup.size == 6 && (1..6).all { teamBLineup[it] != null }
    val canConfirm = isTeamAComplete && isTeamBComplete

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 12.dp)
                .testTag("set_lineup_entry_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CourtSurface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, CourtLines)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VolleyballGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = CourtDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Set $targetSetNumber Starting Line-up",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "FIVB Rule 7.3 - Verification & Rotation Order",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_lineup_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Team Tabs
                TabRow(
                    selectedTabIndex = activeTeamTab,
                    containerColor = Color(0xFF131D31),
                    contentColor = VolleyballGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTeamTab]),
                            color = VolleyballGold
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    // Tab Team A
                    Tab(
                        selected = activeTeamTab == 0,
                        onClick = {
                            activeTeamTab = 0
                            activePositionSlot = 1
                        },
                        modifier = Modifier.testTag("tab_team_a_lineup"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(matchState.teamA.colorHex))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = matchState.teamA.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTeamTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTeamTab == 0) VolleyballGold else Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${teamALineup.size}/6)",
                                    fontSize = 10.sp,
                                    color = if (isTeamAComplete) Color(0xFF34D399) else SanctionYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )

                    // Tab Team B
                    Tab(
                        selected = activeTeamTab == 1,
                        onClick = {
                            activeTeamTab = 1
                            activePositionSlot = 1
                        },
                        modifier = Modifier.testTag("tab_team_b_lineup"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(matchState.teamB.colorHex))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = matchState.teamB.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTeamTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTeamTab == 1) VolleyballGold else Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${teamBLineup.size}/6)",
                                    fontSize = 10.sp,
                                    color = if (isTeamBComplete) Color(0xFF34D399) else SanctionYellow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lineup Quick Tools (Copy Prev Set, Rotate +1, Default 6, Clear)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Copy previous set lineup
                    if (targetSetNumber > 1 && matchState.setLineupHistory.containsKey(targetSetNumber - 1)) {
                        OutlinedButton(
                            onClick = {
                                val prev = matchState.setLineupHistory[targetSetNumber - 1]
                                if (prev != null) {
                                    val copy = if (activeTeamTab == 0) prev.teamALineup else prev.teamBLineup
                                    updateCurrentLineup(copy.toMutableMap())
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy S${targetSetNumber - 1}", fontSize = 10.sp)
                        }
                    }

                    // Rotate Lineup +1 (Clockwise)
                    OutlinedButton(
                        onClick = {
                            if (currentLineup.size == 6) {
                                val rotated = mutableMapOf(
                                    1 to currentLineup[2]!!,
                                    2 to currentLineup[3]!!,
                                    3 to currentLineup[4]!!,
                                    4 to currentLineup[5]!!,
                                    5 to currentLineup[6]!!,
                                    6 to currentLineup[1]!!
                                )
                                updateCurrentLineup(rotated)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = currentLineup.size == 6,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.RotateRight, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rotate +1", fontSize = 10.sp)
                    }

                    // Default 6
                    OutlinedButton(
                        onClick = {
                            val defaultList = activeTeam.roster.filter { !it.isLibero }.take(6)
                            val map = mutableMapOf<Int, Player>()
                            defaultList.forEachIndexed { idx, p -> map[idx + 1] = p }
                            updateCurrentLineup(map)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Default 6", fontSize = 10.sp)
                    }

                    // Clear
                    OutlinedButton(
                        onClick = {
                            updateCurrentLineup(mutableMapOf())
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SanctionRed)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(12.dp), tint = SanctionRed)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Visual Court Lineup Grid: 6 Positions
                // Front Row: Pos IV (Left Front), Pos III (Middle Front), Pos II (Right Front)
                // Back Row:  Pos V (Left Back),  Pos VI (Middle Back), Pos I (Right Back - Server!)
                Text(
                    text = "${activeTeam.displayName.uppercase()} COURT POSITIONS (FIVB ROTATIONAL ORDER)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CourtLines)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        // Net Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("NET (ATTACK ZONE)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Front Row: IV, III, II
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PositionSlotCard(
                                posNumber = 4,
                                label = "IV (LF)",
                                player = currentLineup[4],
                                isSelected = activePositionSlot == 4,
                                isServer = false,
                                onSelectSlot = { activePositionSlot = 4 },
                                onClearSlot = {
                                    val map = currentLineup.toMutableMap()
                                    map.remove(4)
                                    updateCurrentLineup(map)
                                },
                                onEditPlayer = {
                                    currentLineup[4]?.let {
                                        playerBeingEdited = it
                                        showEditPlayerDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PositionSlotCard(
                                posNumber = 3,
                                label = "III (MF)",
                                player = currentLineup[3],
                                isSelected = activePositionSlot == 3,
                                isServer = false,
                                onSelectSlot = { activePositionSlot = 3 },
                                onClearSlot = {
                                    val map = currentLineup.toMutableMap()
                                    map.remove(3)
                                    updateCurrentLineup(map)
                                },
                                onEditPlayer = {
                                    currentLineup[3]?.let {
                                        playerBeingEdited = it
                                        showEditPlayerDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PositionSlotCard(
                                posNumber = 2,
                                label = "II (RF)",
                                player = currentLineup[2],
                                isSelected = activePositionSlot == 2,
                                isServer = false,
                                onSelectSlot = { activePositionSlot = 2 },
                                onClearSlot = {
                                    val map = currentLineup.toMutableMap()
                                    map.remove(2)
                                    updateCurrentLineup(map)
                                },
                                onEditPlayer = {
                                    currentLineup[2]?.let {
                                        playerBeingEdited = it
                                        showEditPlayerDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Attack Line marker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF475569)))
                            Text(" 3m Attack Line ", fontSize = 9.sp, color = Color(0xFF64748B))
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF475569)))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Back Row: V, VI, I (Server)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PositionSlotCard(
                                posNumber = 5,
                                label = "V (LB)",
                                player = currentLineup[5],
                                isSelected = activePositionSlot == 5,
                                isServer = false,
                                onSelectSlot = { activePositionSlot = 5 },
                                onClearSlot = {
                                    val map = currentLineup.toMutableMap()
                                    map.remove(5)
                                    updateCurrentLineup(map)
                                },
                                onEditPlayer = {
                                    currentLineup[5]?.let {
                                        playerBeingEdited = it
                                        showEditPlayerDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PositionSlotCard(
                                posNumber = 6,
                                label = "VI (MB)",
                                player = currentLineup[6],
                                isSelected = activePositionSlot == 6,
                                isServer = false,
                                onSelectSlot = { activePositionSlot = 6 },
                                onClearSlot = {
                                    val map = currentLineup.toMutableMap()
                                    map.remove(6)
                                    updateCurrentLineup(map)
                                },
                                onEditPlayer = {
                                    currentLineup[6]?.let {
                                        playerBeingEdited = it
                                        showEditPlayerDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PositionSlotCard(
                                posNumber = 1,
                                label = "I (SERVER)",
                                player = currentLineup[1],
                                isSelected = activePositionSlot == 1,
                                isServer = true,
                                onSelectSlot = { activePositionSlot = 1 },
                                onClearSlot = {
                                    val map = currentLineup.toMutableMap()
                                    map.remove(1)
                                    updateCurrentLineup(map)
                                },
                                onEditPlayer = {
                                    currentLineup[1]?.let {
                                        playerBeingEdited = it
                                        showEditPlayerDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Designated Libero Selector
                val liberos = activeTeam.getLiberos()
                if (liberos.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E1065))
                            .border(1.dp, LiberoBadge, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(LiberoBadge, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIBERO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val currentLibero = if (activeTeamTab == 0) teamALibero else teamBLibero
                            Text(
                                text = currentLibero?.let { if (it.name.isNotBlank()) "#${it.number} ${it.name}" else "#${it.number} (Libero)" } ?: "No Libero Designated",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Toggle through liberos if more than 1
                        if (liberos.size > 1) {
                            TextButton(
                                onClick = {
                                    val currentLib = if (activeTeamTab == 0) teamALibero else teamBLibero
                                    val nextLib = if (currentLib?.id == liberos.first().id) liberos.last() else liberos.first()
                                    if (activeTeamTab == 0) teamALibero = nextLib else teamBLibero = nextLib
                                }
                            ) {
                                Text("Switch Libero", color = Color(0xFFD8B4FE), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Player Selection Tray
                val currentRoster = if (activeTeamTab == 0) rosterA else rosterB
                val availableRoster = currentRoster.filter { !it.isLibero }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT PLAYER FOR POSITION ${ROMAN_POSITIONS[activePositionSlot]}:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VolleyballGold
                    )

                    TextButton(
                        onClick = {
                            playerBeingEdited = null
                            showEditPlayerDialog = true
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("add_player_lineup_dialog_btn")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(13.dp), tint = VolleyballGold)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+ Add Player", fontSize = 10.sp, color = VolleyballGold, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableRoster) { player ->
                        val alreadyAssignedPos = currentLineup.entries.find { it.value.id == player.id }?.key
                        val isAssignedToCurrent = alreadyAssignedPos == activePositionSlot

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isAssignedToCurrent -> Color(0xFF78350F)
                                        alreadyAssignedPos != null -> Color(0xFF1E293B)
                                        else -> CourtSurfaceHigh
                                    }
                                )
                                .border(
                                    width = if (isAssignedToCurrent) 2.dp else 1.dp,
                                    color = if (isAssignedToCurrent) VolleyballGold else CourtLines,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val map = currentLineup.toMutableMap()
                                    // If already assigned elsewhere, swap or move
                                    if (alreadyAssignedPos != null) {
                                        map.remove(alreadyAssignedPos)
                                    }
                                    map[activePositionSlot] = player
                                    updateCurrentLineup(map)

                                    // Auto-advance to next empty slot
                                    val nextEmpty = (1..6).firstOrNull { map[it] == null }
                                    if (nextEmpty != null) {
                                        activePositionSlot = nextEmpty
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("select_player_item_${player.number}")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${player.number}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isAssignedToCurrent) VolleyballGold else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Player Name / Number",
                                        tint = VolleyballGold,
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clickable {
                                                playerBeingEdited = player
                                                showEditPlayerDialog = true
                                            }
                                    )
                                }
                                Text(
                                    text = player.name.ifBlank { "Player #${player.number}" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (player.name.isNotBlank()) Color(0xFFCBD5E1) else Color(0xFF64748B),
                                    maxLines = 1
                                )
                                Text(
                                    text = alreadyAssignedPos?.let { "Pos ${ROMAN_POSITIONS[it]}" } ?: player.role.code,
                                    fontSize = 9.sp,
                                    color = if (alreadyAssignedPos != null) Color(0xFF94A3B8) else RefereeWhistle,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Set First Server Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
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
                            Icon(Icons.Default.SportsVolleyball, null, tint = VolleyballGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1st Serve of Set $targetSetNumber:", fontSize = 12.sp, color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (firstServingTeamId == matchState.teamA.id) VolleyballGold else CourtSurfaceHigh)
                                    .clickable { firstServingTeamId = matchState.teamA.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = matchState.teamA.displayCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (firstServingTeamId == matchState.teamA.id) CourtDark else Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (firstServingTeamId == matchState.teamB.id) VolleyballGold else CourtSurfaceHigh)
                                    .clickable { firstServingTeamId = matchState.teamB.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = matchState.teamB.displayCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (firstServingTeamId == matchState.teamB.id) CourtDark else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Validation Feedback
                if (!canConfirm) {
                    val missingMsg = buildString {
                        if (!isTeamAComplete) append("${matchState.teamA.displayCode} needs ${6 - teamALineup.size} more players. ")
                        if (!isTeamBComplete) append("${matchState.teamB.displayCode} needs ${6 - teamBLineup.size} more players.")
                    }
                    Text(
                        text = "Complete all 6 positions for both teams: $missingMsg",
                        fontSize = 11.sp,
                        color = SanctionYellow,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Confirm and Apply Lineup Button
                Button(
                    onClick = {
                        if (canConfirm) {
                            onConfirmLineups(
                                targetSetNumber,
                                teamALineup,
                                teamBLineup,
                                teamALibero,
                                teamBLibero,
                                firstServingTeamId
                            )
                            onDismiss()
                        }
                    },
                    enabled = canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_set_lineup_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VolleyballGold,
                        contentColor = CourtDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Register Line-ups & Authorize Play",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }

    // Edit Player Dialog (Name & Shirt Number)
    if (showEditPlayerDialog) {
        val targetTeam = if (activeTeamTab == 0) matchState.teamA else matchState.teamB
        EditPlayerDialog(
            player = playerBeingEdited,
            teamName = targetTeam.displayName,
            teamColorHex = targetTeam.colorHex,
            onDismiss = {
                showEditPlayerDialog = false
                playerBeingEdited = null
            },
            onSavePlayer = { updated ->
                if (activeTeamTab == 0) {
                    val idx = rosterA.indexOfFirst { it.id == updated.id }
                    rosterA = if (idx >= 0) rosterA.toMutableList().apply { set(idx, updated) } else rosterA + updated
                    val map = teamALineup.toMutableMap()
                    map.entries.forEach { (pos, p) ->
                        if (p.id == updated.id) map[pos] = updated
                    }
                    teamALineup = map
                    if (teamALibero?.id == updated.id) teamALibero = updated
                } else {
                    val idx = rosterB.indexOfFirst { it.id == updated.id }
                    rosterB = if (idx >= 0) rosterB.toMutableList().apply { set(idx, updated) } else rosterB + updated
                    val map = teamBLineup.toMutableMap()
                    map.entries.forEach { (pos, p) ->
                        if (p.id == updated.id) map[pos] = updated
                    }
                    teamBLineup = map
                    if (teamBLibero?.id == updated.id) teamBLibero = updated
                }

                onUpdatePlayer?.invoke(targetTeam.id, updated)
                showEditPlayerDialog = false
                playerBeingEdited = null
            }
        )
    }
}

@Composable
private fun PositionSlotCard(
    posNumber: Int,
    label: String,
    player: Player?,
    isSelected: Boolean,
    isServer: Boolean,
    onSelectSlot: () -> Unit,
    onClearSlot: () -> Unit,
    onEditPlayer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSelected -> VolleyballGold
        isServer -> Color(0xFFF59E0B)
        player != null -> CourtLines
        else -> Color(0xFF334155)
    }

    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelectSlot() }
            .padding(4.dp)
            .testTag("position_slot_$posNumber")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Position Label & Server indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(if (isServer) VolleyballGold else Color(0xFF334155), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isServer) CourtDark else Color.White
                    )
                }

                if (player != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Player",
                            tint = VolleyballGold,
                            modifier = Modifier
                                .size(13.dp)
                                .clickable { onEditPlayer?.invoke() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onClearSlot() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Player Number & Name
            if (player != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "#${player.number}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isServer) VolleyballGold else Color.White
                    )
                    if (player.name.isNotBlank()) {
                        Text(
                            text = player.name,
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Assign",
                        fontSize = 11.sp,
                        color = if (isSelected) VolleyballGold else Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
