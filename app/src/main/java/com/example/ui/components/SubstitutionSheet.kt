package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.model.Player
import com.example.model.RotationState
import com.example.model.Team
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.LiberoBadge
import com.example.ui.theme.SanctionRed
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold

@Composable
fun SubstitutionSheet(
    team: Team,
    rotation: RotationState,
    maxSubs: Int,
    enforcePairing: Boolean,
    preSelectedPlayerOut: Player? = null,
    onDismiss: () -> Unit,
    onConfirmSubstitution: (playerOut: Player, playerIn: Player) -> Unit,
    onLiberoReplacement: (pos: Int, libero: Player) -> Unit
) {
    val courtPlayers = rotation.positions.values.toList()
    val benchPlayers = team.roster.filter { rosterPlayer ->
        courtPlayers.none { it.id == rosterPlayer.id }
    }

    var selectedOut by remember {
        mutableStateOf(preSelectedPlayerOut ?: courtPlayers.firstOrNull())
    }
    var selectedIn by remember {
        mutableStateOf(benchPlayers.firstOrNull { !it.isLibero })
    }

    val selectedOutPos = rotation.positions.entries.find { it.value.id == selectedOut?.id }?.key ?: 1
    val isBackRow = selectedOutPos in listOf(1, 5, 6)
    val liberos = team.getLiberos()

    // Check legality of substitution
    val legality = if (selectedOut != null && selectedIn != null) {
        rotation.canSubstitute(
            playerOut = selectedOut!!,
            playerIn = selectedIn!!,
            maxSubs = maxSubs,
            enforcePairing = enforcePairing
        )
    } else {
        false to "Select both players"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("substitution_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CourtSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapVert, null, tint = VolleyballGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${team.name} Substitution",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Subs used: ${rotation.substitutions.size} / $maxSubs",
                            fontSize = 12.sp,
                            color = if (rotation.substitutions.size >= maxSubs) SanctionRed else Color(0xFF94A3B8)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Libero Quick Action (if back-row)
                if (isBackRow && liberos.isNotEmpty()) {
                    val activeLibero = liberos.first()
                    val isCurrentlyLibero = selectedOut?.isLibero == true

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2E1065))
                            .border(1.dp, LiberoBadge, RoundedCornerShape(10.dp))
                            .clickable {
                                onLiberoReplacement(selectedOutPos, activeLibero)
                                onDismiss()
                            }
                            .padding(10.dp)
                            .testTag("libero_quick_replace_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (isCurrentlyLibero) "Restore Regular Player at P$selectedOutPos" else "Replace with Libero [L] #${activeLibero.number}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Unlimited replacements (does not count as substitution)",
                                    fontSize = 10.sp,
                                    color = Color(0xFFD8B4FE)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(LiberoBadge, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("APPLY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Regular Substitution: OUT Selection vs IN Selection
                Text(
                    text = "REGULAR FIVB SUBSTITUTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // OUT (Court Players)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PLAYER OUT (COURT):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SanctionRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        courtPlayers.filter { !it.isLibero }.forEach { p ->
                            val isSelected = selectedOut?.id == p.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF7F1D1D) else CourtSurfaceHigh)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) SanctionRed else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedOut = p }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "#${p.number} ${p.name}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // IN (Bench Players)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PLAYER IN (BENCH):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VolleyballGold)
                        Spacer(modifier = Modifier.height(6.dp))
                        val regularBench = benchPlayers.filter { !it.isLibero }
                        if (regularBench.isEmpty()) {
                            Text("No bench substitutes available", fontSize = 11.sp, color = Color(0xFF64748B))
                        } else {
                            regularBench.forEach { p ->
                                val isSelected = selectedIn?.id == p.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFF78350F) else CourtSurfaceHigh)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) VolleyballGold else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedIn = p }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "#${p.number} ${p.name}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Legality Feedback Banner
                if (!legality.first) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF450A0A), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = SanctionRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = legality.second ?: "Illegal substitution",
                            fontSize = 11.sp,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Confirm Substitution Button
                Button(
                    onClick = {
                        if (selectedOut != null && selectedIn != null && legality.first) {
                            onConfirmSubstitution(selectedOut!!, selectedIn!!)
                            onDismiss()
                        }
                    },
                    enabled = legality.first,
                    modifier = Modifier.fillMaxWidth().testTag("confirm_sub_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = VolleyballGold, contentColor = CourtDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Substitution", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
