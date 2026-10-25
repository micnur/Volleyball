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
            if (winnerId == state.teamA.id) state.teamA.name else state.teamB.name
        }

        val durationMinutes = ((System.currentTimeMillis() - state.matchStartTime) / 60000).toInt()

        val entity = MatchEntity(
            id = state.id,
            title = state.matchTitle,
            date = state.matchStartTime,
            teamAName = state.teamA.name,
            teamBName = state.teamB.name,
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
                Player(number = 1, name = "Bruno R.", role = PositionRole.SETTER, isCaptain = true),
                Player(number = 2, name = "Lucarelli", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 3, name = "Lucas S.", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 4, name = "Alan S.", role = PositionRole.OPPOSITE),
                Player(number = 5, name = "Leal Y.", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 6, name = "Flavio G.", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 10, name = "Thales H.", role = PositionRole.LIBERO, isLibero = true),
                Player(number = 12, name = "Cachopa", role = PositionRole.SETTER),
                Player(number = 14, name = "Honorato", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 16, name = "Judson", role = PositionRole.MIDDLE_BLOCKER)
            )
            return Team(
                id = "team_bra",
                name = "Brazil",
                shortCode = "BRA",
                colorHex = 0xFFF59E0B, // Gold / Yellow
                roster = players
            )
        }

        fun createDefaultTeamB(): Team {
            val players = listOf(
                Player(number = 1, name = "Leon W.", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 2, name = "Kurek B.", role = PositionRole.OPPOSITE, isCaptain = true),
                Player(number = 3, name = "Bieniek M.", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 4, name = "Janusz M.", role = PositionRole.SETTER),
                Player(number = 5, name = "Semeniuk K.", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 6, name = "Huber N.", role = PositionRole.MIDDLE_BLOCKER),
                Player(number = 17, name = "Zatorski P.", role = PositionRole.LIBERO, isLibero = true),
                Player(number = 7, name = "Lomacz G.", role = PositionRole.SETTER),
                Player(number = 9, name = "Fornal T.", role = PositionRole.OUTSIDE_HITTER),
                Player(number = 11, name = "Klos K.", role = PositionRole.MIDDLE_BLOCKER)
            )
            return Team(
                id = "team_pol",
                name = "Poland",
                shortCode = "POL",
                colorHex = 0xFFEF4444, // Red / White
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
                note = "Set 1 started. ${teamA.name} vs ${teamB.name}. Server: ${if (serverFirstId == teamA.id) teamA.name else teamB.name}"
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
                matchTitle = "${teamA.name} vs ${teamB.name}",
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
