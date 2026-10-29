package com.example.model

import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val name: String,
    val role: PositionRole = PositionRole.OUTSIDE_HITTER,
    val isCaptain: Boolean = false,
    val isLibero: Boolean = false,
    val colorHex: Long? = null
) {
    val displayLabel: String
        get() = buildString {
            append("#$number")
            if (name.isNotBlank()) append(" $name")
            if (isCaptain) append(" (C)")
            if (isLibero) append(" [L]")
        }
}
