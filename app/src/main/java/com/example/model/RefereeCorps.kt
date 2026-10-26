package com.example.model

/**
 * FIVB Match Officials & Referee Corps:
 * - 1st Referee (Head referee on referee stand)
 * - 2nd Referee (Floor referee across the court)
 * - Scorers: 3 scorers (e.g. Scorer, Assistant Scorer, 3rd Scorer / electronic sheet)
 * - Line Referees: 2 line judges (corners)
 */
data class RefereeCorps(
    val firstReferee: String = "",
    val secondReferee: String = "",
    val scorer1: String = "",
    val scorer2: String = "",
    val scorer3: String = "",
    val lineReferee1: String = "",
    val lineReferee2: String = ""
) {
    val hasAnyOfficial: Boolean
        get() = firstReferee.isNotBlank() || secondReferee.isNotBlank() ||
                scorer1.isNotBlank() || scorer2.isNotBlank() || scorer3.isNotBlank() ||
                lineReferee1.isNotBlank() || lineReferee2.isNotBlank()
}
