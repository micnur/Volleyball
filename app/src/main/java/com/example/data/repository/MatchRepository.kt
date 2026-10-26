package com.example.data.repository

import android.content.Context
import com.example.data.local.MatchDao
import com.example.data.local.MatchEntity
import com.example.data.local.RefereeDatabase
import com.example.model.CourtSide
import com.example.model.EventType
import com.example.model.FivbRulesConfig
import com.example.model.MatchEvent
import com.example.model.MatchFormat
import com.example.model.MatchState
import com.example.model.Player
import com.example.model.PositionRole
import com.example.model.RotationState
import com.example.model.Team
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MatchRepository(context: Context) {
    private val database = RefereeDatabase.getInstance(context)
    private val matchDao: MatchDao = database.matchDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()

    suspend fun saveMatch(state: MatchState) = withContext(Dispatchers.IO) {
        val scoreSummary = state.completedSets.joinToString(", ") {
            "S${it.setNumber}: ${it.scoreTeamA}-${it.scoreTeamB}"
        }.ifEmpty { "S${state.currentSet}: ${state.scoreTeamA}-${state.scoreTeamB}" }

        val winnerName = state.winnerTeamId?.let { winnerId ->
            if (winnerId == state.teamA.id) state.teamA.displayName else state.teamB.displayName
        }

        val durationMinutes = ((System.currentTimeMillis() - state.matchStartTime) / 60000).toInt()

        val entity = MatchEntity(
            id = state.id,
            title = state.matchTitle,
            date = state.matchStartTime,
            teamAName = state.teamA.displayName,
            teamBName = state.teamB.displayName,
            teamAColor = state.teamA.colorHex,
            teamBColor = state.teamB.colorHex,
            formatName = state.rules.format.displayName,
            setsWonA = state.setsWonTeamA,
            setsWonB = state.setsWonTeamB,
            scoreSummary = scoreSummary,
            winnerName = winnerName,
            isCompleted = state.isMatchFinished,
            totalRallies = state.events.count { it.eventType.category == "Point" },
            durationMinutes = durationMinutes,
            lastUpdated = System.currentTimeMillis(),
            matchDataJson = "" // In-memory active match handles full state
        )
        matchDao.insertOrUpdateMatch(entity)
    }

    suspend fun deleteMatch(matchId: String) = withContext(Dispatchers.IO) {
        matchDao.deleteMatchById(matchId)
    }

    companion object {
        fun createDefaultTeamA(): Team {
            val players = listOf(
                Player(number = 1, name = "", role = PositionRole.SETTER, isCaptain = true),
                Player(number = 2, name = "", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 3, name = "", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 4, name = "", role = PositionRole.OPPOSITE),
                Player(number = 5, name = "", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 6, name = "", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 7, name = "", role = PositionRole.LIBERO, isLibero = true),
                Player(number = 8, name = "", role = PositionRole.SETTER),
                Player(number = 9, name = "", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 10, name = "", role = PositionRole.MIDDLE_BLOCKER)
            )
            return Team(
                id = "team_a",
                name = "",
                shortCode = "",
                colorHex = 0xFF2563EB, // Blue
                roster = players
            )
        }

        fun createDefaultTeamB(): Team {
            val players = listOf(
                Player(number = 1, name = "", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 2, name = "", role = PositionRole.OPPOSITE, isCaptain = true),
                Player(number = 3, name = "", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 4, name = "", role = PositionRole.SETTER),
                Player(number = 5, name = "", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 6, name = "", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 7, name = "", role = PositionRole.LIBERO, isLibero = true),
                Player(number = 8, name = "", role = PositionRole.SETTER),
                Player(number = 9, name = "", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 10, name = "", role = PositionRole.MIDDLE_BLOCKER)
            )
            return Team(
                id = "team_b",
                name = "",
                shortCode = "",
                colorHex = 0xFFDC2626, // Red
                roster = players
            )
        }

        fun createInitialMatchState(
            rules: FivbRulesConfig = FivbRulesConfig.defaultFivb(),
            teamA: Team = createDefaultTeamA(),
            teamB: Team = createDefaultTeamB(),
            serverFirstId: String = teamA.id
        ): MatchState {
            val teamARotation = RotationState.createFromStartingLineup(teamA.id, teamA.roster.filter { !it.isLibero }.take(6))
            val teamBRotation = RotationState.createFromStartingLineup(teamB.id, teamB.roster.filter { !it.isLibero }.take(6))

            val initialEvent = MatchEvent(
                setNumber = 1,
                scoreTeamA = 0,
                scoreTeamB = 0,
                teamId = null,
                eventType = EventType.SET_START,
                note = "Set 1 started. ${teamA.displayName} vs ${teamB.displayName}. Server: ${if (serverFirstId == teamA.id) teamA.displayName else teamB.displayName}"
            )

            val set1LineupRecord = com.example.model.SetStartingLineupRecord(
                setNumber = 1,
                teamALineup = teamARotation.startingLineup,
                teamBLineup = teamBRotation.startingLineup,
                teamALibero = teamA.getLiberos().firstOrNull(),
                teamBLibero = teamB.getLiberos().firstOrNull(),
                firstServingTeamId = serverFirstId
            )

            return MatchState(
                matchTitle = "${teamA.displayName} vs ${teamB.displayName}",
                rules = rules,
                teamA = teamA,
                teamB = teamB,
                teamASide = CourtSide.LEFT,
                teamBSide = CourtSide.RIGHT,
                currentSet = 1,
                scoreTeamA = 0,
                scoreTeamB = 0,
                setsWonTeamA = 0,
                setsWonTeamB = 0,
                servingTeamId = serverFirstId,
                teamARotation = teamARotation,
                teamBRotation = teamBRotation,
                setLineupHistory = mapOf(1 to set1LineupRecord),
                events = listOf(initialEvent)
            )
        }
    }
}
