package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import com.example.model.MatchClockState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CourtSide
import com.example.model.MatchState
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.CourtSurfaceHigh
import com.example.ui.theme.RefereeWhistle
import com.example.ui.theme.SanctionRed
import com.example.ui.theme.SanctionYellow
import com.example.ui.theme.VolleyballGold

@Composable
fun ScoreBoardHeader(
    matchState: MatchState,
    clockState: MatchClockState? = null,
    onSwitchSidesClick: () -> Unit,
    onTimerToggleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val teamLeft = matchState.teamOnSide(CourtSide.LEFT)
    val teamRight = matchState.teamOnSide(CourtSide.RIGHT)
    val isLeftTeamA = teamLeft.id == matchState.teamA.id

    val scoreLeft = if (isLeftTeamA) matchState.scoreTeamA else matchState.scoreTeamB
    val scoreRight = if (!isLeftTeamA) matchState.scoreTeamA else matchState.scoreTeamB

    val setsWonLeft = if (isLeftTeamA) matchState.setsWonTeamA else matchState.setsWonTeamB
    val setsWonRight = if (!isLeftTeamA) matchState.setsWonTeamA else matchState.setsWonTeamB

    val timeoutsUsedLeft = if (isLeftTeamA) matchState.teamATimeoutsUsed else matchState.teamBTimeoutsUsed
    val timeoutsUsedRight = if (!isLeftTeamA) matchState.teamATimeoutsUsed else matchState.teamBTimeoutsUsed

    val subsUsedLeft = if (isLeftTeamA) matchState.teamARotation.substitutions.size else matchState.teamBRotation.substitutions.size
    val subsUsedRight = if (!isLeftTeamA) matchState.teamARotation.substitutions.size else matchState.teamBRotation.substitutions.size

    val maxTimeouts = matchState.rules.timeoutsPerTeamPerSet
    val maxSubs = matchState.rules.maxSubstitutionsPerSet

    val isLeftServing = matchState.servingTeamId == teamLeft.id
    val isRightServing = matchState.servingTeamId == teamRight.id

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scoreboard_header"),
        color = CourtSurface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Set indicator, Set breakdown chips, Switch Sides button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Set Badge & Live Clock Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(VolleyballGold, RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SET ${matchState.currentSet}" + (if (matchState.isDecidingSet) " (DECIDING)" else ""),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CourtDark
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Alert badges: Set point / Match point
                    if (matchState.isMatchPoint()) {
                        Box(
                            modifier = Modifier
                                .background(SanctionRed, RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MATCH POINT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else if (matchState.isSetPoint()) {
                        Box(
                            modifier = Modifier
                                .background(SanctionYellow, RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SET POINT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CourtDark
                            )
                        }
                    }

                    // Compact Live Clock Indicator
                    if (clockState != null) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            onClick = { onTimerToggleClick?.invoke() },
                            shape = RoundedCornerShape(5.dp),
                            color = if (clockState.isRunning) Color(0xFF10B981).copy(alpha = 0.2f) else CourtSurfaceHigh,
                            border = BorderStroke(1.dp, if (clockState.isRunning) Color(0xFF10B981) else CourtLines),
                            modifier = Modifier.testTag("header_clock_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (clockState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (clockState.isRunning) "Pause Clock" else "Start Clock",
                                    tint = if (clockState.isRunning) Color(0xFF10B981) else VolleyballGold,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = clockState.formattedTime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (clockState.isRunning) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }

                // Completed sets chip breakdown (e.g. S1: 25-22, S2: 21-25)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    matchState.completedSets.forEach { res ->
                        val leftS = if (isLeftTeamA) res.scoreTeamA else res.scoreTeamB
                        val rightS = if (!isLeftTeamA) res.scoreTeamA else res.scoreTeamB
                        Box(
                            modifier = Modifier
                                .background(CourtSurfaceHigh, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "S${res.setNumber}: $leftS-$rightS",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onSwitchSidesClick,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("switch_sides_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Court Sides",
                            tint = RefereeWhistle,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Score Row: Left Team | Big Score | Sets | Big Score | Right Team
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Team Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(teamLeft.colorHex))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = teamLeft.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (isLeftServing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SportsVolleyball,
                                contentDescription = "Serving",
                                tint = VolleyballGold,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Left Indicators: Timeouts & Subs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Timeouts dots
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "TO:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            for (i in 1..maxTimeouts) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (i <= timeoutsUsedLeft) SanctionYellow else CourtLines
                                        )
                                )
                            }
                        }

                        // Substitutions count
                        Text(
                            text = "SUB: $subsUsedLeft/$maxSubs",
                            fontSize = 9.sp,
                            color = if (subsUsedLeft >= maxSubs) SanctionRed else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Middle Score Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left Score
                    Text(
                        text = scoreLeft.toString().padStart(2, '0'),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isLeftServing) VolleyballGold else Color.White,
                        modifier = Modifier.testTag("score_left_display")
                    )

                    // Sets won & Colon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(CourtDark, RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$setsWonLeft - $setsWonRight",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                        Text(
                            text = ":",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Right Score
                    Text(
                        text = scoreRight.toString().padStart(2, '0'),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isRightServing) VolleyballGold else Color.White,
                        modifier = Modifier.testTag("score_right_display")
                    )
                }

                // Right Team Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isRightServing) {
                            Icon(
                                imageVector = Icons.Default.SportsVolleyball,
                                contentDescription = "Serving",
                                tint = VolleyballGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = teamRight.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(teamRight.colorHex))
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Right Indicators: Timeouts & Subs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SUB: $subsUsedRight/$maxSubs",
                            fontSize = 9.sp,
                            color = if (subsUsedRight >= maxSubs) SanctionRed else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )

                        // Timeouts dots
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = "TO:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            for (i in 1..maxTimeouts) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (i <= timeoutsUsedRight) SanctionYellow else CourtLines
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Target Points Subtitle
            Text(
                text = "Target: ${matchState.targetPointsCurrentSet} pts (+${matchState.rules.advantageRequired}" +
                        (matchState.capPointsCurrentSet?.let { ", Cap: $it" } ?: "") + ")",
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
