package com.example.model

enum class MatchFormat(val setsToWin: Int, val maxSets: Int, val displayName: String) {
    BEST_OF_5(setsToWin = 3, maxSets = 5, displayName = "Best of 5 (First to 3 sets)"),
    BEST_OF_3(setsToWin = 2, maxSets = 3, displayName = "Best of 3 (First to 2 sets)"),
    SINGLE_SET(setsToWin = 1, maxSets = 1, displayName = "Single Set (First to 1 set)")
}

data class FivbRulesConfig(
    val format: MatchFormat = MatchFormat.BEST_OF_5,
    val pointsRegularSet: Int = 25,
    val pointsDecidingSet: Int = 15,
    val advantageRequired: Int = 2,
    val pointCapRegular: Int? = null, // null = uncapped (FIVB official)
    val pointCapDeciding: Int? = null,
    val timeoutsPerTeamPerSet: Int = 2,
    val timeoutDurationSeconds: Int = 30,
    val technicalTimeoutsEnabled: Boolean = false,
    val technicalTimeoutPoints: List<Int> = listOf(8, 16),
    val technicalTimeoutDurationSeconds: Int = 60,
    val maxSubstitutionsPerSet: Int = 6,
    val enforceFivbSubPairing: Boolean = true,
    val allowLibero: Boolean = true,
    val maxLiberosPerTeam: Int = 2,
    val switchSidesBetweenSets: Boolean = true,
    val decidingSetSwitchPoints: Int = 8,
    val hapticFeedbackEnabled: Boolean = true,
    val whistleAudioEnabled: Boolean = true
) {
    fun isDecidingSet(currentSetNumber: Int): Boolean {
        return currentSetNumber == format.maxSets
    }

    fun getTargetPointsForSet(setNumber: Int): Int {
        return if (isDecidingSet(setNumber)) pointsDecidingSet else pointsRegularSet
    }

    fun getCapForSet(setNumber: Int): Int? {
        return if (isDecidingSet(setNumber)) pointCapDeciding else pointCapRegular
    }

    companion object {
        fun defaultFivb(): FivbRulesConfig = FivbRulesConfig()

        fun recreationalFast(): FivbRulesConfig = FivbRulesConfig(
            format = MatchFormat.BEST_OF_3,
            pointsRegularSet = 21,
            pointsDecidingSet = 15,
            advantageRequired = 2,
            pointCapRegular = 25,
            pointCapDeciding = 17,
            maxSubstitutionsPerSet = 12,
            enforceFivbSubPairing = false
        )

        fun youthLeague(): FivbRulesConfig = FivbRulesConfig(
            format = MatchFormat.BEST_OF_3,
            pointsRegularSet = 25,
            pointsDecidingSet = 15,
            maxSubstitutionsPerSet = 12,
            enforceFivbSubPairing = false
        )
    }
}
