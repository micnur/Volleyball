package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class EventType(val category: String, val label: String) {
    // Points
    POINT_KILL("Point", "Attack / Kill"),
    POINT_ACE("Point", "Ace / Service Winner"),
    POINT_BLOCK("Point", "Block Point"),
    POINT_OPPONENT_ERROR("Point", "Opponent Error"),
    POINT_SERVICE_ERROR("Point", "Service Error"),
    POINT_NET_TOUCH("Point", "Net Violation"),
    POINT_ROTATION_FAULT("Point", "Positional / Rotation Fault"),
    POINT_BALL_OUT("Point", "Ball Out"),
    POINT_PENALTY("Point", "Sanction Penalty Point"),
    POINT_QUICK("Point", "Rally Won"),

    // Interventions
    TIMEOUT("Timeout", "Team Timeout"),
    TECHNICAL_TIMEOUT("Timeout", "Technical Timeout"),
    SUBSTITUTION("Substitution", "Substitution"),
    LIBERO_REPLACEMENT("Libero", "Libero Replacement"),

    // Sanctions
    SANCTION_WARNING("Sanction", "Verbal Warning (Stage 1)"),
    SANCTION_YELLOW_CARD("Sanction", "Yellow Card (Warning - Stage 2)"),
    SANCTION_RED_CARD("Sanction", "Red Card (Penalty Point)"),
    SANCTION_EXPULSION("Sanction", "Expulsion (Red + Yellow same hand)"),
    SANCTION_DISQUALIFICATION("Sanction", "Disqualification (Red + Yellow separate)"),

    // Game Flow
    SIDE_SWITCH("Flow", "Side Switch"),
    SET_START("Flow", "Set Started"),
    LINEUP_CONFIRMED("Flow", "Starting Line-up Registered"),
    SET_END("Flow", "Set Finished"),
    MATCH_END("Flow", "Match Finished")
}

data class MatchEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val setNumber: Int,
    val scoreTeamA: Int,
    val scoreTeamB: Int,
    val teamId: String?,
    val eventType: EventType,
    val primaryPlayer: Player? = null,
    val subInPlayer: Player? = null,
    val subOutPlayer: Player? = null,
    val note: String = ""
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val scoreDisplay: String
        get() = "$scoreTeamA - $scoreTeamB"
}
