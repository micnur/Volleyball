package com.example.model

import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val name: String,
    val role: PositionRole = PositionRole.OUTSIDE_HITTER,
    val isCaptain: Boolean = false,
    val isLibero: Boolean = false
) {
    val displayLabel: String
        get() = "#$number $name" + (if (isCaptain) " (C)" else "") + (if (isLibero) " [L]" else "")
}
