package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: Long,
    val teamAName: String,
    val teamBName: String,
    val teamAColor: Long,
    val teamBColor: Long,
    val formatName: String,
    val setsWonA: Int,
    val setsWonB: Int,
    val scoreSummary: String,
    val winnerName: String?,
    val isCompleted: Boolean,
    val totalRallies: Int,
    val durationMinutes: Int,
    val lastUpdated: Long,
    val matchDataJson: String // Serialized full match data for resume/inspection
)
