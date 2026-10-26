package com.example.model

import java.util.UUID

data class Team(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val shortCode: String = "",
    val colorHex: Long,
    val roster: List<Player> = emptyList()
) {
    val displayName: String
        get() = name.ifBlank {
            if (id.endsWith("b", ignoreCase = true) || id.contains("team_b", ignoreCase = true)) "Team B" else "Team A"
        }

    val displayCode: String
        get() = shortCode.ifBlank {
            name.take(3).uppercase().ifBlank {
                if (id.endsWith("b", ignoreCase = true) || id.contains("team_b", ignoreCase = true)) "TMB" else "TMA"
            }
        }

    fun getPlayerByNumber(number: Int): Player? = roster.find { it.number == number }
    fun getLiberos(): List<Player> = roster.filter { it.isLibero }
}
