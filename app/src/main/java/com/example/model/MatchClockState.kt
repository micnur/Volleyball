package com.example.model

/**
 * Real-time Match & Set Clock state for volleyball referees.
 * Allows tracking real-time elapsed seconds, starting, pausing, and resetting.
 */
data class MatchClockState(
    val isRunning: Boolean = false,
    val accumulatedSeconds: Long = 0L,
    val lastStartTimestamp: Long = 0L,
    val tickCounter: Long = 0L
) {
    /**
     * Current elapsed seconds for active set clock
     */
    val currentSeconds: Long
        get() = if (isRunning && lastStartTimestamp > 0L) {
            accumulatedSeconds + ((System.currentTimeMillis() - lastStartTimestamp) / 1000).coerceAtLeast(0L)
        } else {
            accumulatedSeconds
        }

    /**
     * Formats current elapsed time as mm:ss (or hh:mm:ss if >= 1 hour)
     */
    val formattedTime: String
        get() {
            val total = currentSeconds.coerceAtLeast(0L)
            val hours = total / 3600
            val minutes = (total % 3600) / 60
            val seconds = total % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    companion object {
        fun formatDuration(totalSeconds: Long): String {
            val s = totalSeconds.coerceAtLeast(0L)
            val hours = s / 3600
            val minutes = (s % 3600) / 60
            val seconds = s % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}
