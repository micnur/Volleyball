package com.example.model

data class SubstitutionRecord(
    val playerOut: Player,
    val playerIn: Player,
    val setScoreA: Int,
    val setScoreB: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class RotationState(
    val teamId: String,
    // Current players at positions 1..6
    val positions: Map<Int, Player> = emptyMap(),
    // Starting lineup at start of set (Pos 1..6)
    val startingLineup: Map<Int, Player> = emptyMap(),
    // Tracks Libero replacement: key = court position (1, 5, or 6), value = the original player replaced
    val liberoReplacements: Map<Int, Player> = emptyMap(),
    // List of standard substitutions made in current set
    val substitutions: List<SubstitutionRecord> = emptyList(),
    // Total rotations in this set
    val rotationNumber: Int = 1
) {
    /**
     * Get player currently at given position (1..6)
     */
    fun playerAt(pos: Int): Player? = positions[pos]

    /**
     * Current server is always the player at Position 1
     */
    val serverPlayer: Player?
        get() = positions[1]

    /**
     * Perform official FIVB clockwise rotation:
     * Player at Pos 2 moves to Pos 1 (new server)
     * Player at Pos 1 moves to Pos 6
     * Player at Pos 6 moves to Pos 5
     * Player at Pos 5 moves to Pos 4
     * Player at Pos 4 moves to Pos 3
     * Player at Pos 3 moves to Pos 2
     */
    fun rotateClockwise(): RotationState {
        if (positions.size < 6) return this

        val p1 = positions[1] ?: return this
        val p2 = positions[2] ?: return this
        val p3 = positions[3] ?: return this
        val p4 = positions[4] ?: return this
        val p5 = positions[5] ?: return this
        val p6 = positions[6] ?: return this

        val newPositions = mapOf(
            1 to p2,
            2 to p3,
            3 to p4,
            4 to p5,
            5 to p6,
            6 to p1
        )

        // Also rotate any active libero replacements tracking
        val newLiberoReplacements = mutableMapOf<Int, Player>()
        liberoReplacements[1]?.let { newLiberoReplacements[6] = it }
        liberoReplacements[6]?.let { newLiberoReplacements[5] = it }
        liberoReplacements[5]?.let { newLiberoReplacements[4] = it }
        liberoReplacements[4]?.let { newLiberoReplacements[3] = it }
        liberoReplacements[3]?.let { newLiberoReplacements[2] = it }
        liberoReplacements[2]?.let { newLiberoReplacements[1] = it }

        // If a Libero rotated into front row (positions 4, 3, or 2), FIVB rule requires Libero
        // to be replaced by the original regular player before moving to the front row!
        val resolvedPositions = newPositions.toMutableMap()
        for (frontPos in listOf(2, 3, 4)) {
            val player = resolvedPositions[frontPos]
            if (player?.isLibero == true) {
                val original = newLiberoReplacements.remove(frontPos)
                if (original != null) {
                    resolvedPositions[frontPos] = original
                }
            }
        }

        return copy(
            positions = resolvedPositions,
            liberoReplacements = newLiberoReplacements.filterKeys { it in listOf(1, 5, 6) },
            rotationNumber = rotationNumber + 1
        )
    }

    /**
     * Undo rotation (counter-clockwise)
     */
    fun rotateCounterClockwise(): RotationState {
        if (positions.size < 6) return this

        val p1 = positions[1] ?: return this
        val p2 = positions[2] ?: return this
        val p3 = positions[3] ?: return this
        val p4 = positions[4] ?: return this
        val p5 = positions[5] ?: return this
        val p6 = positions[6] ?: return this

        val newPositions = mapOf(
            1 to p6,
            2 to p1,
            3 to p2,
            4 to p3,
            5 to p4,
            6 to p5
        )

        return copy(
            positions = newPositions,
            rotationNumber = (rotationNumber - 1).coerceAtLeast(1)
        )
    }

    /**
     * Checks if a regular substitution is legal under FIVB rules:
     * - Total subs in set < maxSubstitutions
     * - If enforceFivbSubPairing:
     *     A starting player may only be replaced by a substitute, and that substitute may only be
     *     replaced by the original starting player.
     */
    fun canSubstitute(
        playerOut: Player,
        playerIn: Player,
        maxSubs: Int,
        enforcePairing: Boolean
    ): Pair<Boolean, String?> {
        if (substitutions.size >= maxSubs) {
            return false to "Maximum substitutions ($maxSubs) reached for this set"
        }
        if (playerIn.isLibero || playerOut.isLibero) {
            return false to "Liberos cannot use regular substitutions (use Libero replacement)"
        }
        if (!enforcePairing) {
            return true to null
        }

        // Check if playerIn already entered and left with someone else
        val previousInForThisPlayer = substitutions.find { it.playerIn.id == playerIn.id }
        if (previousInForThisPlayer != null && previousInForThisPlayer.playerOut.id != playerOut.id) {
            return false to "FIVB Rule 15.6: Player #${playerIn.number} already substituted for #${previousInForThisPlayer.playerOut.number}"
        }

        // Check if playerOut is returning: can only return for the player who replaced them
        val previousOutForThisPlayer = substitutions.find { it.playerOut.id == playerOut.id }
        if (previousOutForThisPlayer != null && previousOutForThisPlayer.playerIn.id != playerIn.id) {
            return false to "FIVB Rule 15.6: Player #${playerOut.number} can only return to replace #${previousOutForThisPlayer.playerIn.number}"
        }

        return true to null
    }

    /**
     * Execute a regular substitution
     */
    fun performSubstitution(
        playerOut: Player,
        playerIn: Player,
        scoreA: Int,
        scoreB: Int
    ): RotationState {
        val targetPos = positions.entries.find { it.value.id == playerOut.id }?.key ?: return this
        val newPositions = positions.toMutableMap()
        newPositions[targetPos] = playerIn

        val newSubs = substitutions + SubstitutionRecord(
            playerOut = playerOut,
            playerIn = playerIn,
            setScoreA = scoreA,
            setScoreB = scoreB
        )

        return copy(
            positions = newPositions,
            substitutions = newSubs
        )
    }

    /**
     * Replace back-row player with Libero (or return original back-row player)
     */
    fun performLiberoReplacement(pos: Int, libero: Player): RotationState {
        if (pos !in listOf(1, 5, 6)) return this
        val currentPlayer = positions[pos] ?: return this

        val newPositions = positions.toMutableMap()
        val newLiberoReplacements = liberoReplacements.toMutableMap()

        if (currentPlayer.isLibero) {
            // Libero is returning to bench, restore original player
            val original = liberoReplacements[pos]
            if (original != null) {
                newPositions[pos] = original
                newLiberoReplacements.remove(pos)
            }
        } else {
            // Libero enters for regular back-row player
            newLiberoReplacements[pos] = currentPlayer
            newPositions[pos] = libero
        }

        return copy(
            positions = newPositions,
            liberoReplacements = newLiberoReplacements
        )
    }

    companion object {
        fun createFromStartingLineup(teamId: String, lineup: List<Player>): RotationState {
            val posMap = mutableMapOf<Int, Player>()
            lineup.take(6).forEachIndexed { index, player ->
                posMap[index + 1] = player
            }
            return RotationState(
                teamId = teamId,
                positions = posMap,
                startingLineup = posMap
            )
        }
    }
}
