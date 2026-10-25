package com.example.model

import java.util.UUID

enum class CourtSide {
    LEFT,
    RIGHT;

    fun opposite(): CourtSide = if (this == LEFT) RIGHT else LEFT
}

data class SetResult(
    val setNumber: Int,
    val scoreTeamA: Int,
    val scoreTeamB: Int,
    val winnerTeamId: String,
    val durationSeconds: Long
)

data class SetStartingLineupRecord(
    val setNumber: Int,
    val teamALineup: Map<Int, Player>,
    val teamBLineup: Map<Int, Player>,
    val teamALibero: Player? = null,
    val teamBLibero: Player? = null,
    val firstServingTeamId: String = ""
)

data class MatchSnapshot(
    val currentSet: Int,
    val scoreTeamA: Int,
    val scoreTeamB: Int,
    val setsWonTeamA: Int,
    val setsWonTeamB: Int,
    val servingTeamId: String,
    val teamASide: CourtSide,
    val teamBSide: CourtSide,
    val teamARotation: RotationState,
    val teamBRotation: RotationState,
    val teamATimeoutsUsed: Int,
    val teamBTimeoutsUsed: Int,
    val decidingSetSideSwitched: Boolean,
    val isSetFinished: Boolean,
    val isMatchFinished: Boolean,
    val winnerTeamId: String?,
    val eventCount: Int
)

data class MatchState(
    val id: String = UUID.randomUUID().toString(),
    val matchTitle: String = "FIVB Match",
    val rules: FivbRulesConfig = FivbRulesConfig.defaultFivb(),
    val teamA: Team,
    val teamB: Team,
    val teamASide: CourtSide = CourtSide.LEFT,
    val teamBSide: CourtSide = CourtSide.RIGHT,
    val currentSet: Int = 1,
    val scoreTeamA: Int = 0,
    val scoreTeamB: Int = 0,
    val setsWonTeamA: Int = 0,
    val setsWonTeamB: Int = 0,
    val completedSets: List<SetResult> = emptyList(),
    val servingTeamId: String = teamA.id,
    val teamARotation: RotationState,
    val teamBRotation: RotationState,
    val teamATimeoutsUsed: Int = 0,
    val teamBTimeoutsUsed: Int = 0,
    val technicalTimeoutsTriggered: Set<Int> = emptySet(),
    val isSetFinished: Boolean = false,
    val isMatchFinished: Boolean = false,
    val winnerTeamId: String? = null,
    val decidingSetSideSwitched: Boolean = false,
    val matchStartTime: Long = System.currentTimeMillis(),
    val currentSetStartTime: Long = System.currentTimeMillis(),
    val setLineupHistory: Map<Int, SetStartingLineupRecord> = emptyMap(),
    val events: List<MatchEvent> = emptyList()
) {
    val servingTeam: Team
        get() = if (servingTeamId == teamA.id) teamA else teamB

    val servingPlayer: Player?
        get() = if (servingTeamId == teamA.id) teamARotation.serverPlayer else teamBRotation.serverPlayer

    val isDecidingSet: Boolean
        get() = rules.isDecidingSet(currentSet)

    val targetPointsCurrentSet: Int
        get() = rules.getTargetPointsForSet(currentSet)

    val capPointsCurrentSet: Int?
        get() = rules.getCapForSet(currentSet)

    fun teamOnSide(side: CourtSide): Team = if (teamASide == side) teamA else teamB
    fun rotationOnSide(side: CourtSide): RotationState = if (teamASide == side) teamARotation else teamBRotation

    fun createSnapshot(): MatchSnapshot = MatchSnapshot(
        currentSet = currentSet,
        scoreTeamA = scoreTeamA,
        scoreTeamB = scoreTeamB,
        setsWonTeamA = setsWonTeamA,
        setsWonTeamB = setsWonTeamB,
        servingTeamId = servingTeamId,
        teamASide = teamASide,
        teamBSide = teamBSide,
        teamARotation = teamARotation,
        teamBRotation = teamBRotation,
        teamATimeoutsUsed = teamATimeoutsUsed,
        teamBTimeoutsUsed = teamBTimeoutsUsed,
        decidingSetSideSwitched = decidingSetSideSwitched,
        isSetFinished = isSetFinished,
        isMatchFinished = isMatchFinished,
        winnerTeamId = winnerTeamId,
        eventCount = events.size
    )

    fun restoreSnapshot(snapshot: MatchSnapshot): MatchState = copy(
        currentSet = snapshot.currentSet,
        scoreTeamA = snapshot.scoreTeamA,
        scoreTeamB = snapshot.scoreTeamB,
        setsWonTeamA = snapshot.setsWonTeamA,
        setsWonTeamB = snapshot.setsWonTeamB,
        servingTeamId = snapshot.servingTeamId,
        teamASide = snapshot.teamASide,
        teamBSide = snapshot.teamBSide,
        teamARotation = snapshot.teamARotation,
        teamBRotation = snapshot.teamBRotation,
        teamATimeoutsUsed = snapshot.teamATimeoutsUsed,
        teamBTimeoutsUsed = snapshot.teamBTimeoutsUsed,
        decidingSetSideSwitched = snapshot.decidingSetSideSwitched,
        isSetFinished = snapshot.isSetFinished,
        isMatchFinished = snapshot.isMatchFinished,
        winnerTeamId = snapshot.winnerTeamId,
        events = events.take(snapshot.eventCount)
    )

    /**
     * Checks if current set is won according to FIVB rules
     */
    fun checkSetWinner(): String? {
        val target = targetPointsCurrentSet
        val cap = capPointsCurrentSet
        val diff = Math.abs(scoreTeamA - scoreTeamB)

        // Point Cap reached
        if (cap != null) {
            if (scoreTeamA >= cap && scoreTeamA > scoreTeamB) return teamA.id
            if (scoreTeamB >= cap && scoreTeamB > scoreTeamA) return teamB.id
        }

        // Standard FIVB: Reach target AND lead by advantageRequired (default 2)
        if (scoreTeamA >= target && (scoreTeamA - scoreTeamB) >= rules.advantageRequired) {
            return teamA.id
        }
        if (scoreTeamB >= target && (scoreTeamB - scoreTeamA) >= rules.advantageRequired) {
            return teamB.id
        }

        return null
    }

    /**
     * Check if set point or match point
     */
    fun isSetPoint(): Boolean {
        if (isSetFinished || isMatchFinished) return false
        val target = targetPointsCurrentSet
        val maxScore = maxOf(scoreTeamA, scoreTeamB)
        val minScore = minOf(scoreTeamA, scoreTeamB)
        if (maxScore >= target - 1 && (maxScore - minScore) >= rules.advantageRequired - 1) {
            return true
        }
        return false
    }

    fun isMatchPoint(): Boolean {
        if (!isSetPoint()) return false
        val targetSets = rules.format.setsToWin
        if (scoreTeamA > scoreTeamB && setsWonTeamA == targetSets - 1) return true
        if (scoreTeamB > scoreTeamA && setsWonTeamB == targetSets - 1) return true
        return false
    }
}
