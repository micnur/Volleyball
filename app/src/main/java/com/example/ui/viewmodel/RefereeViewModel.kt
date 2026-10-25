package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MatchEntity
import com.example.data.repository.MatchRepository
import com.example.model.CourtSide
import com.example.model.EventType
import com.example.model.FivbRulesConfig
import com.example.model.MatchEvent
import com.example.model.MatchSnapshot
import com.example.model.MatchState
import com.example.model.Player
import com.example.model.RotationState
import com.example.model.SetResult
import com.example.model.SetStartingLineupRecord
import com.example.model.Team
import com.example.util.RefereeSoundFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RefereeScreen {
    MATCH,          // Main live referee console with scoreboard and 6x6 court
    MATCH_SETUP,    // Setup teams, rosters, initial server
    RULES_CONFIG,   // Full FIVB rules customizer
    MATCH_LOG,      // Live referee event timeline and export scoresheet
    MATCH_HISTORY   // Saved matches list
}

class RefereeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MatchRepository(application)
    private val soundFeedback = RefereeSoundFeedback(application)

    // Current screen navigation state
    private val _currentScreen = MutableStateFlow(RefereeScreen.MATCH)
    val currentScreen: StateFlow<RefereeScreen> = _currentScreen.asStateFlow()

    // Active match state
    private val _matchState = MutableStateFlow(MatchRepository.createInitialMatchState())
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    // Undo stack
    private val undoStack = mutableListOf<MatchSnapshot>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    // Active Timeout Timer state (in seconds, null if no timeout active)
    private val _activeTimeoutSeconds = MutableStateFlow<Int?>(null)
    val activeTimeoutSeconds: StateFlow<Int?> = _activeTimeoutSeconds.asStateFlow()
    private val _activeTimeoutTeam = MutableStateFlow<Team?>(null)
    val activeTimeoutTeam: StateFlow<Team?> = _activeTimeoutTeam.asStateFlow()

    // Side switch alert modal trigger
    private val _pendingSideSwitchMessage = MutableStateFlow<String?>(null)
    val pendingSideSwitchMessage: StateFlow<String?> = _pendingSideSwitchMessage.asStateFlow()

    // Technical timeout triggered message
    private val _technicalTimeoutAlert = MutableStateFlow<String?>(null)
    val technicalTimeoutAlert: StateFlow<String?> = _technicalTimeoutAlert.asStateFlow()

    // Starting Lineup Entry Dialog state
    private val _showLineupDialog = MutableStateFlow(false)
    val showLineupDialog: StateFlow<Boolean> = _showLineupDialog.asStateFlow()

    // Saved matches from Room
    val savedMatches: StateFlow<List<MatchEntity>> = repository.allMatches.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun navigateTo(screen: RefereeScreen) {
        _currentScreen.value = screen
    }

    /**
     * Score a point for either Team A or Team B
     */
    fun scorePoint(
        benefitingTeamId: String,
        eventType: EventType = EventType.POINT_QUICK,
        playerInvolved: Player? = null,
        note: String = ""
    ) {
        val currentState = _matchState.value
        if (currentState.isSetFinished || currentState.isMatchFinished) return

        // Push snapshot for undo
        undoStack.add(currentState.createSnapshot())
        _canUndo.value = true

        val isTeamA = benefitingTeamId == currentState.teamA.id
        val newScoreA = if (isTeamA) currentState.scoreTeamA + 1 else currentState.scoreTeamA
        val newScoreB = if (!isTeamA) currentState.scoreTeamB + 1 else currentState.scoreTeamB

        val previousServingTeamId = currentState.servingTeamId
        val sideOutOccurred = benefitingTeamId != previousServingTeamId

        // FIVB Rule 7.6.1: When the receiving team has gained the right to serve, its players rotate one position clockwise
        var newRotationA = currentState.teamARotation
        var newRotationB = currentState.teamBRotation

        if (sideOutOccurred) {
            if (isTeamA) {
                newRotationA = newRotationA.rotateClockwise()
            } else {
                newRotationB = newRotationB.rotateClockwise()
            }
        }

        val eventDescription = buildString {
            append(if (isTeamA) currentState.teamA.name else currentState.teamB.name)
            append(" Point ($newScoreA - $newScoreB)")
            if (playerInvolved != null) {
                append(" by ${playerInvolved.displayLabel}")
            }
            if (sideOutOccurred) {
                append(" [Side-out: Rotate]")
            }
            if (note.isNotBlank()) {
                append(" ($note)")
            }
        }

        val pointEvent = MatchEvent(
            setNumber = currentState.currentSet,
            scoreTeamA = newScoreA,
            scoreTeamB = newScoreB,
            teamId = benefitingTeamId,
            eventType = eventType,
            primaryPlayer = playerInvolved,
            note = eventDescription
        )

        val newEvents = currentState.events + pointEvent

        var updatedState = currentState.copy(
            scoreTeamA = newScoreA,
            scoreTeamB = newScoreB,
            servingTeamId = benefitingTeamId,
            teamARotation = newRotationA,
            teamBRotation = newRotationB,
            events = newEvents
        )

        // Check FIVB Deciding Set Side Switch (e.g. 8 points reached in Set 5)
        if (updatedState.isDecidingSet && !updatedState.decidingSetSideSwitched) {
            val switchThreshold = updatedState.rules.decidingSetSwitchPoints
            if (newScoreA >= switchThreshold || newScoreB >= switchThreshold) {
                updatedState = performSideSwitchInternal(updatedState, isAutomatic = true)
                _pendingSideSwitchMessage.value = "Deciding set 8-point side switch reached!"
            }
        }

        // Check Technical Timeouts (FIVB: Leading team reaches 8 or 16 in sets 1-4)
        if (updatedState.rules.technicalTimeoutsEnabled && !updatedState.isDecidingSet) {
            val maxScore = maxOf(newScoreA, newScoreB)
            for (ttoPoint in updatedState.rules.technicalTimeoutPoints) {
                if (maxScore >= ttoPoint && !updatedState.technicalTimeoutsTriggered.contains(ttoPoint)) {
                    val newTtoSet = updatedState.technicalTimeoutsTriggered + ttoPoint
                    updatedState = updatedState.copy(technicalTimeoutsTriggered = newTtoSet)
                    _technicalTimeoutAlert.value = "Technical Timeout ($ttoPoint points reached)"
                    soundFeedback.playDoubleWhistle(
                        enabled = updatedState.rules.whistleAudioEnabled,
                        haptic = updatedState.rules.hapticFeedbackEnabled
                    )
                    break
                }
            }
        }

        // Check if set is won
        val setWinnerId = updatedState.checkSetWinner()
        if (setWinnerId != null) {
            handleSetWin(updatedState, setWinnerId)
        } else {
            // Check for Set Point / Match Point
            if (updatedState.isMatchPoint()) {
                soundFeedback.playWhistle(
                    enabled = updatedState.rules.whistleAudioEnabled,
                    haptic = updatedState.rules.hapticFeedbackEnabled
                )
            } else if (updatedState.isSetPoint()) {
                soundFeedback.playWhistle(
                    enabled = updatedState.rules.whistleAudioEnabled,
                    haptic = updatedState.rules.hapticFeedbackEnabled
                )
            } else {
                soundFeedback.playWhistle(
                    enabled = updatedState.rules.whistleAudioEnabled,
                    haptic = updatedState.rules.hapticFeedbackEnabled
                )
            }
            _matchState.value = updatedState
            persistCurrentMatch()
        }
    }

    private fun handleSetWin(state: MatchState, setWinnerId: String) {
        val isTeamAWinner = setWinnerId == state.teamA.id
        val newSetsWonA = if (isTeamAWinner) state.setsWonTeamA + 1 else state.setsWonTeamA
        val newSetsWonB = if (!isTeamAWinner) state.setsWonTeamB + 1 else state.setsWonTeamB
        val winnerTeam = if (isTeamAWinner) state.teamA else state.teamB

        val setResult = SetResult(
            setNumber = state.currentSet,
            scoreTeamA = state.scoreTeamA,
            scoreTeamB = state.scoreTeamB,
            winnerTeamId = setWinnerId,
            durationSeconds = (System.currentTimeMillis() - state.currentSetStartTime) / 1000
        )

        val setEndEvent = MatchEvent(
            setNumber = state.currentSet,
            scoreTeamA = state.scoreTeamA,
            scoreTeamB = state.scoreTeamB,
            teamId = setWinnerId,
            eventType = EventType.SET_END,
            note = "Set ${state.currentSet} won by ${winnerTeam.name} (${state.scoreTeamA}-${state.scoreTeamB})"
        )

        val matchSetsToWin = state.rules.format.setsToWin
        val isMatchWon = newSetsWonA >= matchSetsToWin || newSetsWonB >= matchSetsToWin

        val matchEvents = if (isMatchWon) {
            state.events + setEndEvent + MatchEvent(
                setNumber = state.currentSet,
                scoreTeamA = state.scoreTeamA,
                scoreTeamB = state.scoreTeamB,
                teamId = setWinnerId,
                eventType = EventType.MATCH_END,
                note = "MATCH WON by ${winnerTeam.name}! Final sets: $newSetsWonA - $newSetsWonB"
            )
        } else {
            state.events + setEndEvent
        }

        val finalState = state.copy(
            setsWonTeamA = newSetsWonA,
            setsWonTeamB = newSetsWonB,
            completedSets = state.completedSets + setResult,
            isSetFinished = true,
            isMatchFinished = isMatchWon,
            winnerTeamId = if (isMatchWon) setWinnerId else null,
            events = matchEvents
        )

        soundFeedback.playLongWhistle(
            enabled = state.rules.whistleAudioEnabled,
            haptic = state.rules.hapticFeedbackEnabled
        )

        _matchState.value = finalState
        persistCurrentMatch()
    }

    /**
     * Start the next set
     */
    fun startNextSet() {
        val current = _matchState.value
        if (!current.isSetFinished || current.isMatchFinished) return

        undoStack.clear()
        _canUndo.value = false

        val nextSetNumber = current.currentSet + 1

        // FIVB Rule: Switch sides between sets if enabled
        val (nextSideA, nextSideB) = if (current.rules.switchSidesBetweenSets) {
            current.teamASide.opposite() to current.teamBSide.opposite()
        } else {
            current.teamASide to current.teamBSide
        }

        // Alternate serving team for next set
        val initialServerNextSet = if (current.currentSet % 2 == 1) current.teamB.id else current.teamA.id

        // Reset rotation to initial starting lineups
        val resetRotationA = RotationState.createFromStartingLineup(
            current.teamA.id,
            current.teamARotation.startingLineup.values.toList()
        )
        val resetRotationB = RotationState.createFromStartingLineup(
            current.teamB.id,
            current.teamBRotation.startingLineup.values.toList()
        )

        val setStartEvent = MatchEvent(
            setNumber = nextSetNumber,
            scoreTeamA = 0,
            scoreTeamB = 0,
            teamId = null,
            eventType = EventType.SET_START,
            note = "Set $nextSetNumber started. Serving: ${if (initialServerNextSet == current.teamA.id) current.teamA.name else current.teamB.name}"
        )

        val nextState = current.copy(
            currentSet = nextSetNumber,
            scoreTeamA = 0,
            scoreTeamB = 0,
            teamASide = nextSideA,
            teamBSide = nextSideB,
            teamARotation = resetRotationA,
            teamBRotation = resetRotationB,
            teamATimeoutsUsed = 0,
            teamBTimeoutsUsed = 0,
            technicalTimeoutsTriggered = emptySet(),
            isSetFinished = false,
            decidingSetSideSwitched = false,
            currentSetStartTime = System.currentTimeMillis(),
            servingTeamId = initialServerNextSet,
            events = current.events + setStartEvent
        )

        soundFeedback.playWhistle(
            enabled = current.rules.whistleAudioEnabled,
            haptic = current.rules.hapticFeedbackEnabled
        )

        _matchState.value = nextState
        _showLineupDialog.value = true
        persistCurrentMatch()
    }

    fun openLineupDialog() {
        _showLineupDialog.value = true
    }

    fun closeLineupDialog() {
        _showLineupDialog.value = false
    }

    /**
     * Stores and applies starting rotation/lineup for both teams at the beginning of each set
     */
    fun applyStartingLineups(
        setNumber: Int,
        teamALineup: Map<Int, Player>,
        teamBLineup: Map<Int, Player>,
        teamALibero: Player? = null,
        teamBLibero: Player? = null,
        firstServingTeamId: String = ""
    ) {
        val current = _matchState.value
        undoStack.add(current.createSnapshot())
        _canUndo.value = true

        val newRotationA = RotationState(
            teamId = current.teamA.id,
            positions = teamALineup,
            startingLineup = teamALineup,
            rotationNumber = 1
        )
        val newRotationB = RotationState(
            teamId = current.teamB.id,
            positions = teamBLineup,
            startingLineup = teamBLineup,
            rotationNumber = 1
        )

        val serverId = if (firstServingTeamId.isNotBlank()) firstServingTeamId else current.servingTeamId

        val lineupRecord = SetStartingLineupRecord(
            setNumber = setNumber,
            teamALineup = teamALineup,
            teamBLineup = teamBLineup,
            teamALibero = teamALibero,
            teamBLibero = teamBLibero,
            firstServingTeamId = serverId
        )

        val newHistory = current.setLineupHistory + (setNumber to lineupRecord)

        val lineupEvent = MatchEvent(
            setNumber = setNumber,
            scoreTeamA = current.scoreTeamA,
            scoreTeamB = current.scoreTeamB,
            teamId = null,
            eventType = EventType.LINEUP_CONFIRMED,
            note = "Official starting line-up registered for Set $setNumber for both teams. Server: ${if (serverId == current.teamA.id) current.teamA.name else current.teamB.name}"
        )

        soundFeedback.playWhistle(
            enabled = current.rules.whistleAudioEnabled,
            haptic = current.rules.hapticFeedbackEnabled
        )

        _matchState.value = current.copy(
            teamARotation = newRotationA,
            teamBRotation = newRotationB,
            servingTeamId = serverId,
            setLineupHistory = newHistory,
            events = current.events + lineupEvent
        )
        _showLineupDialog.value = false
        persistCurrentMatch()
    }

    /**
     * Undo last action
     */
    fun undo() {
        if (undoStack.isEmpty()) return
        val lastSnapshot = undoStack.removeAt(undoStack.lastIndex)
        _canUndo.value = undoStack.isNotEmpty()

        val restored = _matchState.value.restoreSnapshot(lastSnapshot)
        soundFeedback.playWhistle(enabled = false, haptic = true)
        _matchState.value = restored
        persistCurrentMatch()
    }

    /**
     * Request a Team Timeout
     */
    fun requestTimeout(teamId: String) {
        val current = _matchState.value
        if (current.isSetFinished || current.isMatchFinished) return

        val isTeamA = teamId == current.teamA.id
        val currentUsed = if (isTeamA) current.teamATimeoutsUsed else current.teamBTimeoutsUsed
        val maxAllowed = current.rules.timeoutsPerTeamPerSet

        if (currentUsed >= maxAllowed) return

        undoStack.add(current.createSnapshot())
        _canUndo.value = true

        val newUsedA = if (isTeamA) current.teamATimeoutsUsed + 1 else current.teamATimeoutsUsed
        val newUsedB = if (!isTeamA) current.teamBTimeoutsUsed + 1 else current.teamBTimeoutsUsed
        val team = if (isTeamA) current.teamA else current.teamB

        val timeoutEvent = MatchEvent(
            setNumber = current.currentSet,
            scoreTeamA = current.scoreTeamA,
            scoreTeamB = current.scoreTeamB,
            teamId = teamId,
            eventType = EventType.TIMEOUT,
            note = "${team.name} Timeout called (${if (isTeamA) newUsedA else newUsedB}/$maxAllowed)"
        )

        soundFeedback.playDoubleWhistle(
            enabled = current.rules.whistleAudioEnabled,
            haptic = current.rules.hapticFeedbackEnabled
        )

        _matchState.value = current.copy(
            teamATimeoutsUsed = newUsedA,
            teamBTimeoutsUsed = newUsedB,
            events = current.events + timeoutEvent
        )

        // Start active countdown timer in UI
        _activeTimeoutTeam.value = team
        _activeTimeoutSeconds.value = current.rules.timeoutDurationSeconds

        persistCurrentMatch()
    }

    fun dismissTimeoutTimer() {
        _activeTimeoutSeconds.value = null
        _activeTimeoutTeam.value = null
    }

    fun onTimeoutTimerFinished() {
        soundFeedback.playLongWhistle(
            enabled = _matchState.value.rules.whistleAudioEnabled,
            haptic = _matchState.value.rules.hapticFeedbackEnabled
        )
        dismissTimeoutTimer()
    }

    fun dismissSideSwitchAlert() {
        _pendingSideSwitchMessage.value = null
    }

    fun dismissTechnicalTimeoutAlert() {
        _technicalTimeoutAlert.value = null
    }

    /**
     * Execute a player substitution
     */
    fun performSubstitution(teamId: String, playerOut: Player, playerIn: Player) {
        val current = _matchState.value
        val isTeamA = teamId == current.teamA.id
        val rotation = if (isTeamA) current.teamARotation else current.teamBRotation

        val (canSub, reason) = rotation.canSubstitute(
            playerOut = playerOut,
            playerIn = playerIn,
            maxSubs = current.rules.maxSubstitutionsPerSet,
            enforcePairing = current.rules.enforceFivbSubPairing
        )

        if (!canSub) return

        undoStack.add(current.createSnapshot())
        _canUndo.value = true

        val newRotation = rotation.performSubstitution(
            playerOut = playerOut,
            playerIn = playerIn,
            scoreA = current.scoreTeamA,
            scoreB = current.scoreTeamB
        )

        val team = if (isTeamA) current.teamA else current.teamB
        val subEvent = MatchEvent(
            setNumber = current.currentSet,
            scoreTeamA = current.scoreTeamA,
            scoreTeamB = current.scoreTeamB,
            teamId = teamId,
            eventType = EventType.SUBSTITUTION,
            subInPlayer = playerIn,
            subOutPlayer = playerOut,
            note = "${team.name} Sub: In #${playerIn.number} ${playerIn.name} for #${playerOut.number} ${playerOut.name}"
        )

        soundFeedback.playDoubleWhistle(
            enabled = current.rules.whistleAudioEnabled,
            haptic = current.rules.hapticFeedbackEnabled
        )

        _matchState.value = current.copy(
            teamARotation = if (isTeamA) newRotation else current.teamARotation,
            teamBRotation = if (!isTeamA) newRotation else current.teamBRotation,
            events = current.events + subEvent
        )
        persistCurrentMatch()
    }

    /**
     * Libero replacement for back-row positions (P1, P5, P6)
     */
    fun performLiberoReplacement(teamId: String, pos: Int, libero: Player) {
        val current = _matchState.value
        val isTeamA = teamId == current.teamA.id
        val rotation = if (isTeamA) current.teamARotation else current.teamBRotation

        undoStack.add(current.createSnapshot())
        _canUndo.value = true

        val newRotation = rotation.performLiberoReplacement(pos, libero)
        val team = if (isTeamA) current.teamA else current.teamB

        val targetPlayer = rotation.positions[pos]
        val isEntering = targetPlayer?.isLibero != true

        val event = MatchEvent(
            setNumber = current.currentSet,
            scoreTeamA = current.scoreTeamA,
            scoreTeamB = current.scoreTeamB,
            teamId = teamId,
            eventType = EventType.LIBERO_REPLACEMENT,
            note = if (isEntering) {
                "${team.name} Libero [L] #${libero.number} entered at P$pos"
            } else {
                "${team.name} Libero [L] #${libero.number} replaced at P$pos"
            }
        )

        _matchState.value = current.copy(
            teamARotation = if (isTeamA) newRotation else current.teamARotation,
            teamBRotation = if (!isTeamA) newRotation else current.teamBRotation,
            events = current.events + event
        )
        persistCurrentMatch()
    }

    /**
     * Manual rotation for referee adjustment
     */
    fun manualRotate(teamId: String, clockwise: Boolean) {
        val current = _matchState.value
        undoStack.add(current.createSnapshot())
        _canUndo.value = true

        val isTeamA = teamId == current.teamA.id
        val targetRotation = if (isTeamA) current.teamARotation else current.teamBRotation
        val updatedRotation = if (clockwise) targetRotation.rotateClockwise() else targetRotation.rotateCounterClockwise()

        val team = if (isTeamA) current.teamA else current.teamB
        val event = MatchEvent(
            setNumber = current.currentSet,
            scoreTeamA = current.scoreTeamA,
            scoreTeamB = current.scoreTeamB,
            teamId = teamId,
            eventType = EventType.POINT_ROTATION_FAULT,
            note = "Referee manual rotation adjustment for ${team.name} (${if (clockwise) "CW" else "CCW"})"
        )

        _matchState.value = current.copy(
            teamARotation = if (isTeamA) updatedRotation else current.teamARotation,
            teamBRotation = if (!isTeamA) updatedRotation else current.teamBRotation,
            events = current.events + event
        )
        persistCurrentMatch()
    }

    /**
     * Switch court sides manually or during deciding set
     */
    fun switchCourtSides() {
        val current = _matchState.value
        _matchState.value = performSideSwitchInternal(current, isAutomatic = false)
        persistCurrentMatch()
    }

    private fun performSideSwitchInternal(state: MatchState, isAutomatic: Boolean): MatchState {
        val event = MatchEvent(
            setNumber = state.currentSet,
            scoreTeamA = state.scoreTeamA,
            scoreTeamB = state.scoreTeamB,
            teamId = null,
            eventType = EventType.SIDE_SWITCH,
            note = if (isAutomatic) "Official side switch (deciding set threshold reached)" else "Referee manual court side switch"
        )

        return state.copy(
            teamASide = state.teamASide.opposite(),
            teamBSide = state.teamBSide.opposite(),
            decidingSetSideSwitched = if (isAutomatic) true else state.decidingSetSideSwitched,
            events = state.events + event
        )
    }

    /**
     * Apply official FIVB sanction card
     */
    fun applySanction(teamId: String, sanctionType: EventType, player: Player?, note: String) {
        val current = _matchState.value
        val isTeamA = teamId == current.teamA.id
        val penalizedTeam = if (isTeamA) current.teamA else current.teamB
        val opponentTeam = if (isTeamA) current.teamB else current.teamA

        undoStack.add(current.createSnapshot())
        _canUndo.value = true

        val sanctionEvent = MatchEvent(
            setNumber = current.currentSet,
            scoreTeamA = current.scoreTeamA,
            scoreTeamB = current.scoreTeamB,
            teamId = teamId,
            eventType = sanctionType,
            primaryPlayer = player,
            note = buildString {
                append("${sanctionType.label} to ${penalizedTeam.name}")
                if (player != null) append(" (${player.displayLabel})")
                if (note.isNotBlank()) append(" - $note")
            }
        )

        soundFeedback.playSanctionAlert(
            enabled = current.rules.whistleAudioEnabled,
            haptic = current.rules.hapticFeedbackEnabled
        )

        // Under FIVB rules, a RED CARD carries a point and service penalty to opponent!
        if (sanctionType == EventType.SANCTION_RED_CARD) {
            _matchState.value = current.copy(events = current.events + sanctionEvent)
            scorePoint(
                benefitingTeamId = opponentTeam.id,
                eventType = EventType.POINT_PENALTY,
                note = "Penalty from Red Card on ${penalizedTeam.name}"
            )
        } else {
            _matchState.value = current.copy(events = current.events + sanctionEvent)
            persistCurrentMatch()
        }
    }

    /**
     * Update FIVB rules configuration
     */
    fun updateRules(newRules: FivbRulesConfig) {
        _matchState.update { it.copy(rules = newRules) }
        persistCurrentMatch()
    }

    /**
     * Start a brand new match with custom teams and settings
     */
    fun startNewMatch(
        teamA: Team,
        teamB: Team,
        rules: FivbRulesConfig,
        initialServerTeamId: String
    ) {
        undoStack.clear()
        _canUndo.value = false
        _activeTimeoutSeconds.value = null
        _activeTimeoutTeam.value = null
        _pendingSideSwitchMessage.value = null
        _technicalTimeoutAlert.value = null

        val newState = MatchRepository.createInitialMatchState(
            rules = rules,
            teamA = teamA,
            teamB = teamB,
            serverFirstId = initialServerTeamId
        )

        _matchState.value = newState
        _currentScreen.value = RefereeScreen.MATCH
        persistCurrentMatch()
    }

    /**
     * Update or add a player's name and shirt number
     */
    fun updatePlayer(teamId: String, updatedPlayer: Player) {
        val current = _matchState.value
        val isTeamA = teamId == current.teamA.id
        val targetTeam = if (isTeamA) current.teamA else current.teamB

        val existingIndex = targetTeam.roster.indexOfFirst { it.id == updatedPlayer.id }
        val newRoster = if (existingIndex >= 0) {
            targetTeam.roster.toMutableList().apply { set(existingIndex, updatedPlayer) }
        } else {
            targetTeam.roster + updatedPlayer
        }

        val newTeam = targetTeam.copy(roster = newRoster)

        // Update active rotations if player is on court
        val targetRotation = if (isTeamA) current.teamARotation else current.teamBRotation
        val newPositions = targetRotation.positions.mapValues { (_, p) ->
            if (p.id == updatedPlayer.id) updatedPlayer else p
        }
        val newStartingLineup = targetRotation.startingLineup.mapValues { (_, p) ->
            if (p.id == updatedPlayer.id) updatedPlayer else p
        }
        val updatedRotation = targetRotation.copy(
            positions = newPositions,
            startingLineup = newStartingLineup
        )

        // Update lineup history
        val newHistory = current.setLineupHistory.mapValues { (_, record) ->
            if (isTeamA) {
                record.copy(
                    teamALineup = record.teamALineup.mapValues { if (it.value.id == updatedPlayer.id) updatedPlayer else it.value },
                    teamALibero = if (record.teamALibero?.id == updatedPlayer.id) updatedPlayer else record.teamALibero
                )
            } else {
                record.copy(
                    teamBLineup = record.teamBLineup.mapValues { if (it.value.id == updatedPlayer.id) updatedPlayer else it.value },
                    teamBLibero = if (record.teamBLibero?.id == updatedPlayer.id) updatedPlayer else record.teamBLibero
                )
            }
        }

        _matchState.value = current.copy(
            teamA = if (isTeamA) newTeam else current.teamA,
            teamB = if (!isTeamA) newTeam else current.teamB,
            teamARotation = if (isTeamA) updatedRotation else current.teamARotation,
            teamBRotation = if (!isTeamA) updatedRotation else current.teamBRotation,
            setLineupHistory = newHistory
        )
        persistCurrentMatch()
    }

    private fun persistCurrentMatch() {
        viewModelScope.launch {
            repository.saveMatch(_matchState.value)
        }
    }

    fun deleteMatchRecord(matchId: String) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
        }
    }
}
