package com.example.model

enum class PositionRole(
    val code: String,
    val label: String
) {
    SETTER("S", "Setter"),
    OUTSIDE_HITTER("OH", "Outside Hitter"),
    MIDDLE_BLOCKER("MB", "Middle Blocker"),
    OPPOSITE("OPP", "Opposite"),
    LIBERO("L", "Libero"),
    UNIVERSAL("U", "Universal")
}
