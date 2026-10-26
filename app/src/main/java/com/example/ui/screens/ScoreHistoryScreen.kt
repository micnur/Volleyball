package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsVolleyball
import com.example.ui.components.ExportReportDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EventType
import com.example.model.MatchEvent
import com.example.model.MatchState
import com.example.model.Team
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

@Composable
fun ScoreHistoryScreen(
    viewModel: RefereeViewModel,
    modifier: Modifier = Modifier
) {
    val matchState by viewModel.matchState.collectAsState()
    val context = LocalContext.current

    // Handle Android system back button
    BackHandler {
        viewModel.navigateTo(RefereeScreen.MATCH)
    }

    var selectedSetFilter by remember { mutableIntStateOf(0) } // 0 = All Sets, 1..N = specific set
    var selectedTeamFilterId by remember { mutableStateOf<String?>(null) } // null = both teams
    var onlyPointsFilter by remember { mutableStateOf(true) } // true = points only, false = all events
    var isChronologicalAscending by remember { mutableStateOf(true) } // true = oldest first (1-0 -> 25-23), false = newest first

    // Filter events
    val filteredEvents = remember(matchState.events, selectedSetFilter, selectedTeamFilterId, onlyPointsFilter, isChronologicalAscending) {
        var list = matchState.events

        // Set filter
        if (selectedSetFilter > 0) {
            list = list.filter { it.setNumber == selectedSetFilter }
        }

        // Only points filter
        if (onlyPointsFilter) {
            list = list.filter { it.eventType.category == "Point" }
        }

        // Team filter
        if (selectedTeamFilterId != null) {
            list = list.filter { it.teamId == selectedTeamFilterId }
        }

        if (!isChronologicalAscending) {
            list.reversed()
        } else {
            list
        }
    }

    // Points stats calculation
    val allPointEvents = matchState.events.filter { it.eventType.category == "Point" }
    val teamAPoints = allPointEvents.count { it.teamId == matchState.teamA.id }
    val teamBPoints = allPointEvents.count { it.teamId == matchState.teamB.id }
    val totalKills = allPointEvents.count { it.eventType == EventType.POINT_KILL }
    val totalAces = allPointEvents.count { it.eventType == EventType.POINT_ACE }
    val totalBlocks = allPointEvents.count { it.eventType == EventType.POINT_BLOCK }

    var showExportReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CourtDark)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = CourtDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CourtSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Top Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.navigateTo(RefereeScreen.MATCH) },
                            modifier = Modifier.testTag("score_history_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Match",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Score History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${matchState.teamA.displayName} vs ${matchState.teamB.displayName} (Sets ${matchState.setsWonTeamA}-${matchState.setsWonTeamB})",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Export PDF / CSV / Copy Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Export PDF / CSV Report Button
                        IconButton(
                            onClick = { showExportReportDialog = true },
                            modifier = Modifier.testTag("export_report_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export PDF or CSV Match Report",
                                tint = VolleyballGold
                            )
                        }

                        // Copy Text to Clipboard
                        IconButton(
                            onClick = {
                                val exportText = generateScoresheetExport(matchState)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("VolleyRef Score History", exportText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Score history copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_scoresheet_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Score History",
                                tint = RefereeWhistle
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Set Selector Tabs
                val totalSetsCount = maxOf(matchState.currentSet, matchState.completedSets.size)
                val setTabs = listOf("All Sets") + (1..totalSetsCount).map { "Set $it" }

                ScrollableTabRow(
                    selectedTabIndex = selectedSetFilter,
                    containerColor = CourtSurface,
                    contentColor = VolleyballGold,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        if (selectedSetFilter < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSetFilter]),
                                color = VolleyballGold
                            )
                        }
                    },
                    divider = {}
                ) {
                    setTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSetFilter == index,
                            onClick = { selectedSetFilter = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSetFilter == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedSetFilter == index) VolleyballGold else Color(0xFF94A3B8)
                                )
                            }
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
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Score History Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("score_history_summary_card"),
                colors = CardDefaults.cardColors(containerColor = CourtSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CourtLines)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Team Points Total Comparison
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(matchState.teamA.colorHex))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${matchState.teamA.displayName}: $teamAPoints pts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "TOTAL RALLIES: ${allPointEvents.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${matchState.teamB.displayName}: $teamBPoints pts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(matchState.teamB.colorHex))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = CourtSurfaceHigh)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Rally types count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("Kills: $totalKills", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text("Aces: $totalAces", fontSize = 11.sp, color = VolleyballGold, fontWeight = FontWeight.SemiBold)
                        Text("Blocks: $totalBlocks", fontSize = 11.sp, color = RefereeWhistle, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Lead Changes: ${calculateLeadChanges(allPointEvents, matchState.teamA.id)}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Starting Lineup display for selected set
            if (selectedSetFilter > 0 && matchState.setLineupHistory.containsKey(selectedSetFilter)) {
                val lineupRecord = matchState.setLineupHistory[selectedSetFilter]!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("set_lineup_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CourtLines)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OFFICIAL SET $selectedSetFilter STARTING ROTATIONS (FIVB 7.3)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VolleyballGold
                            )
                            val serverCode = if (lineupRecord.firstServingTeamId == matchState.teamA.id) matchState.teamA.displayCode else matchState.teamB.displayCode
                            Text(
                                text = "1st Serve: $serverCode",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Team A Starting 6
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = matchState.teamA.displayName + ":",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(matchState.teamA.colorHex)
                                )
                                (1..6).forEach { pos ->
                                    val p = lineupRecord.teamALineup[pos]
                                    val label = p?.let {
                                        if (it.name.isNotBlank()) "#${it.number} ${it.name}" else "#${it.number}"
                                    } ?: "--"
                                    Text(
                                        text = "Pos $pos: $label",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }

                            // Team B Starting 6
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = matchState.teamB.displayName + ":",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(matchState.teamB.colorHex)
                                )
                                (1..6).forEach { pos ->
                                    val p = lineupRecord.teamBLineup[pos]
                                    val label = p?.let {
                                        if (it.name.isNotBlank()) "#${it.number} ${it.name}" else "#${it.number}"
                                    } ?: "--"
                                    Text(
                                        text = "Pos $pos: $label",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filters & Chronological Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Team & Type Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTeamFilterId == null && onlyPointsFilter,
                            onClick = {
                                selectedTeamFilterId = null
                                onlyPointsFilter = true
                            },
                            label = { Text("All Points", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VolleyballGold,
                                selectedLabelColor = CourtDark
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTeamFilterId == matchState.teamA.id,
                            onClick = {
                                selectedTeamFilterId = if (selectedTeamFilterId == matchState.teamA.id) null else matchState.teamA.id
                            },
                            label = { Text("${matchState.teamA.shortCode} Points", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(matchState.teamA.colorHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTeamFilterId == matchState.teamB.id,
                            onClick = {
                                selectedTeamFilterId = if (selectedTeamFilterId == matchState.teamB.id) null else matchState.teamB.id
                            },
                            label = { Text("${matchState.teamB.shortCode} Points", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(matchState.teamB.colorHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = !onlyPointsFilter,
                            onClick = { onlyPointsFilter = !onlyPointsFilter },
                            label = { Text("+ All Events", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RefereeWhistle,
                                selectedLabelColor = CourtDark
                            )
                        )
                    }
                }

                // Chronological Sort Order Toggle (Oldest first vs Newest first)
                IconButton(
                    onClick = { isChronologicalAscending = !isChronologicalAscending },
                    modifier = Modifier.testTag("sort_order_toggle_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Toggle Sort Order",
                        tint = if (isChronologicalAscending) VolleyballGold else Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle indicating order
            Text(
                text = if (isChronologicalAscending) "CHRONOLOGICAL ORDER (FIRST POINT TO LAST)" else "REVERSE CHRONOLOGICAL (NEWEST FIRST)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            // Chronological Point Event List
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsVolleyball,
                            contentDescription = null,
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No point events recorded yet",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("score_history_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(filteredEvents, key = { _, event -> event.id }) { index, event ->
                        ScoreHistoryItemCard(
                            event = event,
                            matchState = matchState,
                            index = if (isChronologicalAscending) index + 1 else filteredEvents.size - index
                        )
                    }
                }
            }
        }
    }

    if (showExportReportDialog) {
        ExportReportDialog(
            matchState = matchState,
            onDismiss = { showExportReportDialog = false }
        )
    }
}

@Composable
private fun ScoreHistoryItemCard(
    event: MatchEvent,
    matchState: MatchState,
    index: Int
) {
    val isPoint = event.eventType.category == "Point"
    val benefitingTeam = when (event.teamId) {
        matchState.teamA.id -> matchState.teamA
        matchState.teamB.id -> matchState.teamB
        else -> null
    }

    val isTeamA = benefitingTeam?.id == matchState.teamA.id

    val badgeColor = when (event.eventType) {
        EventType.POINT_KILL -> Color(0xFF3B82F6) // Blue
        EventType.POINT_ACE -> VolleyballGold     // Gold
        EventType.POINT_BLOCK -> Color(0xFF10B981) // Green
        EventType.POINT_OPPONENT_ERROR,
        EventType.POINT_SERVICE_ERROR -> Color(0xFFF97316) // Orange
        EventType.POINT_NET_TOUCH,
        EventType.POINT_ROTATION_FAULT -> Color(0xFFEF4444) // Red
        EventType.POINT_PENALTY -> SanctionRed
        EventType.TIMEOUT,
        EventType.TECHNICAL_TIMEOUT -> SanctionYellow
        EventType.SUBSTITUTION -> RefereeWhistle
        EventType.SANCTION_YELLOW_CARD -> SanctionYellow
        EventType.SANCTION_RED_CARD -> SanctionRed
        EventType.SET_START,
        EventType.SET_END,
        EventType.MATCH_END -> Color(0xFF8B5CF6)
        EventType.TIMER_START -> Color(0xFF10B981)
        EventType.TIMER_PAUSE -> VolleyballGold
        EventType.TIMER_RESET -> Color(0xFF94A3B8)
        else -> Color(0xFF64748B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("score_history_item_${event.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPoint) CourtSurface else Color(0xFF131D31)
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isPoint) 1.dp else 0.5.dp,
            color = if (isPoint) CourtLines else Color(0xFF334155)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Index & Timestamp Column
            Column(
                modifier = Modifier.width(68.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "#$index",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = event.formattedTime,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "SET ${event.setNumber}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = VolleyballGold
                )
            }

            // Running Score Display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, CourtLines, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.scoreDisplay,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (benefitingTeam != null) Color(benefitingTeam.colorHex) else Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Event Description & Team Badge
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Benefiting Team Chip / Event Category
                    if (benefitingTeam != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(benefitingTeam.colorHex))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = benefitingTeam.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = event.eventType.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Event Type Tag Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.eventType.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Detail Note or Description
                if (event.note.isNotBlank()) {
                    Text(
                        text = event.note,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        maxLines = 2
                    )
                }

                // If players involved (substitutions or primary scorer)
                if (event.subInPlayer != null && event.subOutPlayer != null) {
                    val inStr = if (event.subInPlayer.name.isNotBlank()) "#${event.subInPlayer.number} ${event.subInPlayer.name}" else "#${event.subInPlayer.number}"
                    val outStr = if (event.subOutPlayer.name.isNotBlank()) "#${event.subOutPlayer.number} ${event.subOutPlayer.name}" else "#${event.subOutPlayer.number}"
                    Text(
                        text = "IN: $inStr  |  OUT: $outStr",
                        fontSize = 10.sp,
                        color = RefereeWhistle
                    )
                }
            }
        }
    }
}

/**
 * Calculates lead changes between teams in points
 */
private fun calculateLeadChanges(events: List<MatchEvent>, teamAId: String): Int {
    var leadChanges = 0
    var previousLeader: Int? = null // 1 for A, -1 for B, 0 for tied

    for (event in events) {
        if (event.eventType.category != "Point") continue
        val currentLeader = when {
            event.scoreTeamA > event.scoreTeamB -> 1
            event.scoreTeamB > event.scoreTeamA -> -1
            else -> 0
        }
        if (currentLeader != 0 && previousLeader != null && previousLeader != 0 && currentLeader != previousLeader) {
            leadChanges++
        }
        if (currentLeader != 0) {
            previousLeader = currentLeader
        }
    }
    return leadChanges
}

/**
 * Generates official formatted score history text for clipboard/sharing
 */
private fun generateScoresheetExport(state: MatchState): String {
    return buildString {
        appendLine("=========================================")
        appendLine("       VOLLEYREF FIVB SCORESHEET LOG     ")
        appendLine("=========================================")
        appendLine("Match: ${state.teamA.displayName} vs ${state.teamB.displayName}")
        appendLine("Sets Result: ${state.teamA.displayName} ${state.setsWonTeamA} - ${state.setsWonTeamB} ${state.teamB.displayName}")
        appendLine("Format: ${state.rules.format.displayName}")
        appendLine("Completed Sets: " + state.completedSets.joinToString(", ") { "Set ${it.setNumber}: ${it.scoreTeamA}-${it.scoreTeamB}" })
        if (state.officials.hasAnyOfficial) {
            appendLine("-----------------------------------------")
            appendLine("MATCH OFFICIALS (FIVB CORPS):")
            if (state.officials.firstReferee.isNotBlank()) appendLine("  1st Referee: ${state.officials.firstReferee}")
            if (state.officials.secondReferee.isNotBlank()) appendLine("  2nd Referee: ${state.officials.secondReferee}")
            val scorers = listOfNotNull(
                state.officials.scorer1.takeIf { it.isNotBlank() }?.let { "1. $it (Official Scorer)" },
                state.officials.scorer2.takeIf { it.isNotBlank() }?.let { "2. $it (Assistant Scorer)" },
                state.officials.scorer3.takeIf { it.isNotBlank() }?.let { "3. $it" }
            )
            if (scorers.isNotEmpty()) {
                appendLine("  Scorers:")
                scorers.forEach { appendLine("    $it") }
            }
            val lines = listOfNotNull(
                state.officials.lineReferee1.takeIf { it.isNotBlank() }?.let { "1. $it" },
                state.officials.lineReferee2.takeIf { it.isNotBlank() }?.let { "2. $it" }
            )
            if (lines.isNotEmpty()) {
                appendLine("  Line Referees:")
                lines.forEach { appendLine("    $it") }
            }
        }
        appendLine("-----------------------------------------")
        appendLine("REGISTERED STARTING LINE-UPS (FIVB 7.3):")
        state.setLineupHistory.toSortedMap().forEach { (setNum, record) ->
            val aStr = (1..6).joinToString(" ") { "P$it:#${record.teamALineup[it]?.number ?: "--"}" }
            val bStr = (1..6).joinToString(" ") { "P$it:#${record.teamBLineup[it]?.number ?: "--"}" }
            appendLine("Set $setNum:")
            appendLine("  ${state.teamA.displayCode}: $aStr")
            appendLine("  ${state.teamB.displayCode}: $bStr")
        }
        appendLine("-----------------------------------------")
        appendLine("CHRONOLOGICAL SCORE HISTORY LOG:")
        appendLine("-----------------------------------------")

        state.events.forEachIndexed { i, event ->
            val time = event.formattedTime
            val setStr = "S${event.setNumber}"
            val score = event.scoreDisplay
            val team = when (event.teamId) {
                state.teamA.id -> state.teamA.displayCode
                state.teamB.id -> state.teamB.displayCode
                else -> "---"
            }
            appendLine(String.format("%3d. [%s] %s | %-6s | %-4s | %s", i + 1, time, setStr, score, team, event.note.ifEmpty { event.eventType.label }))
        }

        appendLine("=========================================")
        appendLine("Generated by VolleyRef Android App")
    }
}
