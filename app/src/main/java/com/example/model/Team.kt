package com.example.model

import java.util.UUID

data class Team(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val shortCode: String,
    val colorHex: Long,
    val roster: List<Player> = emptyList()
) {
    fun getPlayerByNumber(number: Int): Player? = roster.find { it.number == number }
    fun getLiberos(): List<Player> = roster.filter { it.isLibero }
}
