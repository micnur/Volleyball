package com.example.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Real-time scoreboard data payload transmitted over Bluetooth RFCOMM socket.
 * Contains current scores, sets, timeouts taken and remaining, substitutions,
 * serving team, set timer, and match completion status.
 */
data class BluetoothScorePacket(
    val teamAName: String = "Team A",
    val teamBName: String = "Team B",
    val teamACode: String = "TMA",
    val teamBCode: String = "TMB",
    val teamAColorHex: Long = 0xFF2563EB,
    val teamBColorHex: Long = 0xFFDC2626,
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val setsWonA: Int = 0,
    val setsWonB: Int = 0,
    val currentSet: Int = 1,
    val isDecidingSet: Boolean = false,
    val servingTeamId: String = "team_a",
    val targetPoints: Int = 25,
    // Timeouts taken and remaining
    val timeoutsUsedA: Int = 0,
    val timeoutsRemainingA: Int = 2,
    val timeoutsUsedB: Int = 0,
    val timeoutsRemainingB: Int = 2,
    val maxTimeoutsPerSet: Int = 2,
    // Substitutions
    val subsUsedA: Int = 0,
    val subsRemainingA: Int = 6,
    val subsUsedB: Int = 0,
    val subsRemainingB: Int = 6,
    // Status
    val isMatchFinished: Boolean = false,
    val isSetFinished: Boolean = false,
    val winnerTeamId: String? = null,
    val activeTimeoutSeconds: Int? = null,
    val activeTimeoutTeamName: String? = null,
    val clockFormatted: String = "00:00",
    val clockIsRunning: Boolean = false,
    val completedSetsSummary: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJsonString(): String {
        val json = JSONObject().apply {
            put("teamAName", teamAName)
            put("teamBName", teamBName)
            put("teamACode", teamACode)
            put("teamBCode", teamBCode)
            put("teamAColorHex", teamAColorHex)
            put("teamBColorHex", teamBColorHex)
            put("scoreA", scoreA)
            put("scoreB", scoreB)
            put("setsWonA", setsWonA)
            put("setsWonB", setsWonB)
            put("currentSet", currentSet)
            put("isDecidingSet", isDecidingSet)
            put("servingTeamId", servingTeamId)
            put("targetPoints", targetPoints)
            put("timeoutsUsedA", timeoutsUsedA)
            put("timeoutsRemainingA", timeoutsRemainingA)
            put("timeoutsUsedB", timeoutsUsedB)
            put("timeoutsRemainingB", timeoutsRemainingB)
            put("maxTimeoutsPerSet", maxTimeoutsPerSet)
            put("subsUsedA", subsUsedA)
            put("subsRemainingA", subsRemainingA)
            put("subsUsedB", subsUsedB)
            put("subsRemainingB", subsRemainingB)
            put("isMatchFinished", isMatchFinished)
            put("isSetFinished", isSetFinished)
            put("winnerTeamId", winnerTeamId ?: "")
            put("activeTimeoutSeconds", activeTimeoutSeconds ?: -1)
            put("activeTimeoutTeamName", activeTimeoutTeamName ?: "")
            put("clockFormatted", clockFormatted)
            put("clockIsRunning", clockIsRunning)
            val arr = JSONArray()
            completedSetsSummary.forEach { arr.put(it) }
            put("completedSets", arr)
            put("timestamp", timestamp)
        }
        return json.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): BluetoothScorePacket? {
            return try {
                val json = JSONObject(jsonStr)
                val completedArr = json.optJSONArray("completedSets")
                val setsList = mutableListOf<String>()
                if (completedArr != null) {
                    for (i in 0 until completedArr.length()) {
                        setsList.add(completedArr.getString(i))
                    }
                }
                val toSec = json.optInt("activeTimeoutSeconds", -1)
                val toTeam = json.optString("activeTimeoutTeamName", "")
                val winner = json.optString("winnerTeamId", "")

                BluetoothScorePacket(
                    teamAName = json.optString("teamAName", "Team A"),
                    teamBName = json.optString("teamBName", "Team B"),
                    teamACode = json.optString("teamACode", "TMA"),
                    teamBCode = json.optString("teamBCode", "TMB"),
                    teamAColorHex = json.optLong("teamAColorHex", 0xFF2563EB),
                    teamBColorHex = json.optLong("teamBColorHex", 0xFFDC2626),
                    scoreA = json.optInt("scoreA", 0),
                    scoreB = json.optInt("scoreB", 0),
                    setsWonA = json.optInt("setsWonA", 0),
                    setsWonB = json.optInt("setsWonB", 0),
                    currentSet = json.optInt("currentSet", 1),
                    isDecidingSet = json.optBoolean("isDecidingSet", false),
                    servingTeamId = json.optString("servingTeamId", "team_a"),
                    targetPoints = json.optInt("targetPoints", 25),
                    timeoutsUsedA = json.optInt("timeoutsUsedA", 0),
                    timeoutsRemainingA = json.optInt("timeoutsRemainingA", 2),
                    timeoutsUsedB = json.optInt("timeoutsUsedB", 0),
                    timeoutsRemainingB = json.optInt("timeoutsRemainingB", 2),
                    maxTimeoutsPerSet = json.optInt("maxTimeoutsPerSet", 2),
                    subsUsedA = json.optInt("subsUsedA", 0),
                    subsRemainingA = json.optInt("subsRemainingA", 6),
                    subsUsedB = json.optInt("subsUsedB", 0),
                    subsRemainingB = json.optInt("subsRemainingB", 6),
                    isMatchFinished = json.optBoolean("isMatchFinished", false),
                    isSetFinished = json.optBoolean("isSetFinished", false),
                    winnerTeamId = if (winner.isNotBlank()) winner else null,
                    activeTimeoutSeconds = if (toSec >= 0) toSec else null,
                    activeTimeoutTeamName = if (toTeam.isNotBlank()) toTeam else null,
                    clockFormatted = json.optString("clockFormatted", "00:00"),
                    clockIsRunning = json.optBoolean("clockIsRunning", false),
                    completedSetsSummary = setsList,
                    timestamp = json.optLong("timestamp", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
