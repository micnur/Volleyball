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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.EventType
import com.example.model.Player
import com.example.model.Team
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.SanctionRed
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold

@Composable
fun SanctionDialog(
    teamA: Team,
    teamB: Team,
    preSelectedTeamId: String? = null,
    preSelectedPlayer: Player? = null,
    onDismiss: () -> Unit,
    onApplySanction: (teamId: String, sanctionType: EventType, player: Player?, note: String) -> Unit
) {
    var selectedTeamId by remember { mutableStateOf(preSelectedTeamId ?: teamA.id) }
    var selectedPlayer by remember { mutableStateOf(preSelectedPlayer) }
    var selectedSanction by remember { mutableStateOf(EventType.SANCTION_YELLOW_CARD) }
    var note by remember { mutableStateOf("") }

    val activeTeam = if (selectedTeamId == teamA.id) teamA else teamB

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("sanction_dialog"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null, tint = SanctionYellow, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FIVB Sanction Card",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Team Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(teamA, teamB).forEach { t ->
                        val isSelected = selectedTeamId == t.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CourtSurfaceHigh else Color(0xFF0F172A))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(t.colorHex) else Color(0xFF334155),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTeamId = t.id
                                    selectedPlayer = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sanction Type Cards
                Text("SANCTION SCALE (FIVB RULE 21):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(8.dp))

                val sanctions = listOf(
                    Triple(EventType.SANCTION_WARNING, "Verbal Warning", Color(0xFF64748B)),
                    Triple(EventType.SANCTION_YELLOW_CARD, "Yellow Card (Warning)", SanctionYellow),
                    Triple(EventType.SANCTION_RED_CARD, "Red Card (Point + Serve Penalty)", SanctionRed),
                    Triple(EventType.SANCTION_EXPULSION, "Expulsion (Yellow+Red same hand)", Color(0xFFDC2626)),
                    Triple(EventType.SANCTION_DISQUALIFICATION, "Disqualification (Yellow+Red separate)", Color(0xFF991B1B))
                )

                sanctions.forEach { (type, label, cardColor) ->
                    val isSelected = selectedSanction == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CourtSurfaceHigh else Color(0xFF0F172A))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) cardColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedSanction = type }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 22.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cardColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Reason (e.g. dissent, misconduct, delay)...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VolleyballGold,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Sanction Button
                Button(
                    onClick = {
                        onApplySanction(selectedTeamId, selectedSanction, selectedPlayer, note)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("apply_sanction_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSanction == EventType.SANCTION_RED_CARD) SanctionRed else VolleyballGold,
                        contentColor = if (selectedSanction == EventType.SANCTION_RED_CARD) Color.White else CourtDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply Official Sanction", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
