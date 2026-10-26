package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CourtSide
import com.example.model.MatchState
import com.example.model.Player
import com.example.model.RotationState
import com.example.model.Team
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.LiberoBadge
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold

@Composable
fun Court6x6View(
    matchState: MatchState,
    onPlayerClick: (teamId: String, pos: Int, player: Player) -> Unit,
    onManualRotateClick: (teamId: String, clockwise: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val teamLeft = matchState.teamOnSide(CourtSide.LEFT)
    val teamRight = matchState.teamOnSide(CourtSide.RIGHT)
    val rotationLeft = matchState.rotationOnSide(CourtSide.LEFT)
    val rotationRight = matchState.rotationOnSide(CourtSide.RIGHT)

    val isLeftServing = matchState.servingTeamId == teamLeft.id
    val isRightServing = matchState.servingTeamId == teamRight.id

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("court_6x6_view"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, CourtLines)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Court Team Headers & Rotation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Team Header & Rotation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onManualRotateClick(teamLeft.id, false) },
                        modifier = Modifier.size(28.dp).testTag("rotate_left_ccw")
                    ) {
                        Icon(Icons.Default.RotateLeft, "Rotate Left CCW", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onManualRotateClick(teamLeft.id, true) },
                        modifier = Modifier.size(28.dp).testTag("rotate_left_cw")
                    ) {
                        Icon(Icons.Default.RotateRight, "Rotate Left CW", tint = VolleyballGold, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${teamLeft.displayCode} (Rot #${rotationLeft.rotationNumber})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(teamLeft.colorHex)
                    )
                }

                // Center Referee Vantage Label
                Text(
                    text = "STAND PERSPECTIVE (NET CENTER)",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.SemiBold
                )

                // Right Team Header & Rotation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${teamRight.displayCode} (Rot #${rotationRight.rotationNumber})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(teamRight.colorHex)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onManualRotateClick(teamRight.id, true) },
                        modifier = Modifier.size(28.dp).testTag("rotate_right_cw")
                    ) {
                        Icon(Icons.Default.RotateRight, "Rotate Right CW", tint = VolleyballGold, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onManualRotateClick(teamRight.id, false) },
                        modifier = Modifier.size(28.dp).testTag("rotate_right_ccw")
                    ) {
                        Icon(Icons.Default.RotateLeft, "Rotate Right CCW", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // The Court Canvas: Left Half | Center Net | Right Half
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, CourtLines, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // LEFT HALF COURT (Net on the RIGHT side of this half)
                    HalfCourtView(
                        team = teamLeft,
                        rotation = rotationLeft,
                        isServing = isLeftServing,
                        isLeftCourt = true,
                        onPlayerClick = { pos, player -> onPlayerClick(teamLeft.id, pos, player) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // CENTER NET
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFEF4444), // Antenna top red
                                        Color.White,       // Antenna white
                                        Color(0xFFE2E8F0), // Net tape white
                                        Color(0xFF475569), // Mesh
                                        Color(0xFFE2E8F0),
                                        Color(0xFFEF4444)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NET",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = CourtDark
                        )
                    }

                    // RIGHT HALF COURT (Net on the LEFT side of this half)
                    HalfCourtView(
                        team = teamRight,
                        rotation = rotationRight,
                        isServing = isRightServing,
                        isLeftCourt = false,
                        onPlayerClick = { pos, player -> onPlayerClick(teamRight.id, pos, player) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VolleyballGold))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("P1: Active Server", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LiberoBadge))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("[L]: Libero", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("--- Attack Line (3m)", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun HalfCourtView(
    team: Team,
    rotation: RotationState,
    isServing: Boolean,
    isLeftCourt: Boolean,
    onPlayerClick: (pos: Int, player: Player) -> Unit,
    modifier: Modifier = Modifier
) {
    // If Left court:
    // Left zone is Back-row (P5, P6, P1). Right zone is Front-row (P4, P3, P2) adjacent to Net!
    // If Right court:
    // Left zone is Front-row (P2, P3, P4) adjacent to Net. Right zone is Back-row (P1, P6, P5)!

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        if (isLeftCourt) {
            // BACK ZONE (P5 top, P6 middle, P1 bottom)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerCard(5, rotation.playerAt(5), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(6, rotation.playerAt(6), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(1, rotation.playerAt(1), isServer = isServing, team = team, onPlayerClick = onPlayerClick)
            }

            // ATTACK LINE (3m)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF334155))
            )

            // FRONT ZONE (P4 top, P3 middle, P2 bottom)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerCard(4, rotation.playerAt(4), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(3, rotation.playerAt(3), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(2, rotation.playerAt(2), isServer = false, team = team, onPlayerClick = onPlayerClick)
            }
        } else {
            // FRONT ZONE (P2 top, P3 middle, P4 bottom)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerCard(2, rotation.playerAt(2), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(3, rotation.playerAt(3), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(4, rotation.playerAt(4), isServer = false, team = team, onPlayerClick = onPlayerClick)
            }

            // ATTACK LINE (3m)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF334155))
            )

            // BACK ZONE (P1 top, P6 middle, P5 bottom)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerCard(1, rotation.playerAt(1), isServer = isServing, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(6, rotation.playerAt(6), isServer = false, team = team, onPlayerClick = onPlayerClick)
                PlayerCard(5, rotation.playerAt(5), isServer = false, team = team, onPlayerClick = onPlayerClick)
            }
        }
    }
}

@Composable
private fun PlayerCard(
    pos: Int,
    player: Player?,
    isServer: Boolean,
    team: Team,
    onPlayerClick: (pos: Int, player: Player) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLibero = player?.isLibero == true

    val backgroundColor = when {
        isServer -> Color(0xFF1E293B)
        isLibero -> Color(0xFF2E1065) // Dark violet for Libero
        else -> Color(0xFF1E293B)
    }

    val borderColor = when {
        isServer -> VolleyballGold
        isLibero -> LiberoBadge
        else -> CourtLines
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(
                width = if (isServer) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = player != null) {
                if (player != null) {
                    onPlayerClick(pos, player)
                }
            }
            .padding(2.dp)
            .testTag("player_pos_${team.id}_$pos")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Position badge (P1..P6) and Server ball / Libero badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = "P$pos",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                }

                if (isServer) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(VolleyballGold, RoundedCornerShape(2.dp))
                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsVolleyball,
                            contentDescription = "Server",
                            tint = CourtDark,
                            modifier = Modifier.size(8.dp)
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = "SERVE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = CourtDark
                        )
                    }
                } else if (isLibero) {
                    Box(
                        modifier = Modifier
                            .background(LiberoBadge, RoundedCornerShape(2.dp))
                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "L",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Player Number & Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player?.let { "#${it.number}" } ?: "--",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isServer) VolleyballGold else Color.White
                )

                if (player != null) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "(${player.role.code})",
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Player Name & Captain flag
            Text(
                text = player?.let {
                    if (it.name.isNotBlank()) it.name + (if (it.isCaptain) " (C)" else "")
                    else if (it.isCaptain) "(C)" else ""
                } ?: "Empty",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
