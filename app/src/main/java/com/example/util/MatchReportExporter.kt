package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.MatchEvent
import com.example.model.MatchState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MatchReportExporter {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(25)
    }

    /**
     * Generates a comprehensive CSV spreadsheet file containing:
     * - Match summary, format, and winner
     * - Match Officials & Referee Corps (1st ref, 2nd ref, 3 scorers, 2 line judges)
     * - Completed Sets breakdown
     * - Registered Starting Lineups (FIVB 7.3)
     * - Match statistics (points, kills, aces, blocks, errors, timeouts, substitutions)
     * - Full chronological point-by-point timeline
     */
    fun generateCsvFile(context: Context, state: MatchState): File {
        val teamAName = state.teamA.displayName
        val teamBName = state.teamB.displayName
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "MatchReport_${sanitizeFilename(teamAName)}_vs_${sanitizeFilename(teamBName)}_$timestamp.csv"
        val file = File(getReportsDir(context), filename)

        val totalKillsA = state.events.count { it.teamId == state.teamA.id && it.eventType.name.contains("KILL") }
        val totalKillsB = state.events.count { it.teamId == state.teamB.id && it.eventType.name.contains("KILL") }
        val totalAcesA = state.events.count { it.teamId == state.teamA.id && it.eventType.name.contains("ACE") }
        val totalAcesB = state.events.count { it.teamId == state.teamB.id && it.eventType.name.contains("ACE") }
        val totalBlocksA = state.events.count { it.teamId == state.teamA.id && it.eventType.name.contains("BLOCK") }
        val totalBlocksB = state.events.count { it.teamId == state.teamB.id && it.eventType.name.contains("BLOCK") }

        file.bufferedWriter().use { writer ->
            // Match Information Header
            writer.appendLine("\"VOLLEYREF FIVB OFFICIAL MATCH REPORT\"")
            writer.appendLine("\"Match Title\",\"${state.matchTitle}\"")
            writer.appendLine("\"Date\",\"${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(state.matchStartTime))}\"")
            writer.appendLine("\"Format\",\"${state.rules.format.displayName}\"")
            writer.appendLine("\"Team A\",\"$teamAName (${state.teamA.displayCode})\"")
            writer.appendLine("\"Team B\",\"$teamBName (${state.teamB.displayCode})\"")
            writer.appendLine("\"Final Sets Score\",\"$teamAName ${state.setsWonTeamA} - ${state.setsWonTeamB} $teamBName\"")
            val winnerName = when (state.winnerTeamId) {
                state.teamA.id -> teamAName
                state.teamB.id -> teamBName
                else -> "In Progress / Unfinished"
            }
            writer.appendLine("\"Match Winner\",\"$winnerName\"")
            writer.appendLine()

            // Match Officials & Referee Corps
            writer.appendLine("\"MATCH OFFICIALS & REFEREE CORPS (FIVB CHAPTER 8)\"")
            writer.appendLine("\"Role\",\"Official Name\"")
            writer.appendLine("\"1st Referee\",\"${state.officials.firstReferee.ifBlank { "Unassigned" }}\"")
            writer.appendLine("\"2nd Referee\",\"${state.officials.secondReferee.ifBlank { "Unassigned" }}\"")
            writer.appendLine("\"Scorer 1 (Official Scorer)\",\"${state.officials.scorer1.ifBlank { "Unassigned" }}\"")
            writer.appendLine("\"Scorer 2 (Assistant Scorer)\",\"${state.officials.scorer2.ifBlank { "Unassigned" }}\"")
            writer.appendLine("\"Scorer 3\",\"${state.officials.scorer3.ifBlank { "Unassigned" }}\"")
            writer.appendLine("\"Line Referee 1\",\"${state.officials.lineReferee1.ifBlank { "Unassigned" }}\"")
            writer.appendLine("\"Line Referee 2\",\"${state.officials.lineReferee2.ifBlank { "Unassigned" }}\"")
            writer.appendLine()

            // Set Breakdown
            writer.appendLine("\"SET RESULTS SUMMARY\"")
            writer.appendLine("\"Set Number\",\"Score $teamAName\",\"Score $teamBName\",\"Winner\",\"Duration (min)\"")
            state.completedSets.forEach { s ->
                val setWinner = if (s.winnerTeamId == state.teamA.id) teamAName else teamBName
                val durationMin = (s.durationSeconds / 60).coerceAtLeast(1)
                writer.appendLine("\"Set ${s.setNumber}\",\"${s.scoreTeamA}\",\"${s.scoreTeamB}\",\"$setWinner\",\"$durationMin min\"")
            }
            writer.appendLine()

            // Starting Lineups
            writer.appendLine("\"REGISTERED STARTING LINE-UPS (FIVB 7.3)\"")
            writer.appendLine("\"Set\",\"Team\",\"Pos I\",\"Pos II\",\"Pos III\",\"Pos IV\",\"Pos V\",\"Pos VI\",\"Libero\"")
            state.setLineupHistory.toSortedMap().forEach { (setNum, record) ->
                val aPos = (1..6).map { pos -> record.teamALineup[pos]?.let { "#${it.number} ${it.name}".trim() } ?: "--" }
                val aLib = record.teamALibero?.let { "#${it.number} ${it.name}".trim() } ?: "--"
                writer.appendLine("\"Set $setNum\",\"$teamAName\",\"${aPos[0]}\",\"${aPos[1]}\",\"${aPos[2]}\",\"${aPos[3]}\",\"${aPos[4]}\",\"${aPos[5]}\",\"$aLib\"")

                val bPos = (1..6).map { pos -> record.teamBLineup[pos]?.let { "#${it.number} ${it.name}".trim() } ?: "--" }
                val bLib = record.teamBLibero?.let { "#${it.number} ${it.name}".trim() } ?: "--"
                writer.appendLine("\"Set $setNum\",\"$teamBName\",\"${bPos[0]}\",\"${bPos[1]}\",\"${bPos[2]}\",\"${bPos[3]}\",\"${bPos[4]}\",\"${bPos[5]}\",\"$bLib\"")
            }
            writer.appendLine()

            // Team Statistics Comparison
            writer.appendLine("\"MATCH STATISTICS COMPARISON\"")
            writer.appendLine("\"Metric\",\"$teamAName\",\"$teamBName\"")
            val pointsA = state.completedSets.sumOf { it.scoreTeamA } + state.scoreTeamA
            val pointsB = state.completedSets.sumOf { it.scoreTeamB } + state.scoreTeamB
            writer.appendLine("\"Total Points Scored\",\"$pointsA\",\"$pointsB\"")
            writer.appendLine("\"Attacking Kills\",\"$totalKillsA\",\"$totalKillsB\"")
            writer.appendLine("\"Service Aces\",\"$totalAcesA\",\"$totalAcesB\"")
            writer.appendLine("\"Blocks\",\"$totalBlocksA\",\"$totalBlocksB\"")
            writer.appendLine("\"Sets Won\",\"${state.setsWonTeamA}\",\"${state.setsWonTeamB}\"")
            writer.appendLine()

            // Chronological Point Event Log
            writer.appendLine("\"CHRONOLOGICAL POINT-BY-POINT TIMELINE\"")
            writer.appendLine("\"#\",\"Set\",\"Time\",\"Score\",\"Benefiting Team\",\"Event Category\",\"Event Type\",\"Players / Details\",\"Note\"")
            state.events.forEachIndexed { index, event ->
                val teamStr = when (event.teamId) {
                    state.teamA.id -> state.teamA.displayCode
                    state.teamB.id -> state.teamB.displayCode
                    else -> "---"
                }
                val playerStr = buildString {
                    if (event.primaryPlayer != null) {
                        append("#${event.primaryPlayer.number} ${event.primaryPlayer.name}".trim())
                    }
                    if (event.subInPlayer != null && event.subOutPlayer != null) {
                        append("IN: #${event.subInPlayer.number} ${event.subInPlayer.name} | OUT: #${event.subOutPlayer.number} ${event.subOutPlayer.name}".trim())
                    }
                }
                val cleanNote = event.note.replace("\"", "\"\"")
                writer.appendLine("\"${index + 1}\",\"Set ${event.setNumber}\",\"${event.formattedTime}\",\"${event.scoreDisplay}\",\"$teamStr\",\"${event.eventType.category}\",\"${event.eventType.label}\",\"$playerStr\",\"$cleanNote\"")
            }
        }

        return file
    }

    /**
     * Generates a high quality formatted multi-page PDF document match report:
     * - Page 1: Official FIVB Scoresheet Summary, Officials Corps, Set Results, Starting Lineups, Team Stats, Signatures
     * - Page 2+: Detailed Chronological Rally Point-by-Point Timeline
     */
    fun generatePdfFile(context: Context, state: MatchState): File {
        val teamAName = state.teamA.displayName
        val teamBName = state.teamB.displayName
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "MatchReport_${sanitizeFilename(teamAName)}_vs_${sanitizeFilename(teamBName)}_$timestamp.pdf"
        val file = File(getReportsDir(context), filename)

        val pdfDocument = PdfDocument()

        // Page setup: Standard A4 is 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            color = Color.BLACK
        }

        // ================= PAGE 1: SCORESHEET SUMMARY =================
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // Header Background Banner
        paint.color = Color.rgb(15, 23, 42) // Dark Navy
        canvas1.drawRect(0f, 0f, pageWidth.toFloat(), 65f, paint)

        // Gold Accent Line
        paint.color = Color.rgb(245, 158, 11) // Gold
        canvas1.drawRect(0f, 65f, pageWidth.toFloat(), 68f, paint)

        // Title
        textPaint.color = Color.WHITE
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("VOLLEYREF — OFFICIAL FIVB MATCH REPORT", 20f, 32f, textPaint)

        textPaint.textSize = 9f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = Color.rgb(203, 213, 225)
        canvas1.drawText("Generated by VolleyRef Android Referee Companion  •  FIVB Rules Compliant", 20f, 50f, textPaint)

        var yPos = 85f

        // Match Info Block
        paint.color = Color.rgb(241, 245, 249)
        canvas1.drawRoundRect(20f, yPos, (pageWidth - 20).toFloat(), yPos + 60f, 6f, 6f, paint)

        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("$teamAName  vs  $teamBName", 32f, yPos + 22f, textPaint)

        textPaint.textSize = 14f
        textPaint.color = Color.rgb(217, 119, 6)
        val finalSetsText = "FINAL SETS: ${state.setsWonTeamA} - ${state.setsWonTeamB}"
        val textWidth = textPaint.measureText(finalSetsText)
        canvas1.drawText(finalSetsText, pageWidth - 32f - textWidth, yPos + 22f, textPaint)

        textPaint.textSize = 9f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = Color.rgb(71, 85, 105)
        val dateStr = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(state.matchStartTime))
        val winnerName = when (state.winnerTeamId) {
            state.teamA.id -> teamAName
            state.teamB.id -> teamBName
            else -> "In Progress"
        }
        canvas1.drawText("Date: $dateStr   |   Format: ${state.rules.format.displayName}   |   Winner: $winnerName", 32f, yPos + 44f, textPaint)

        yPos += 72f

        // Section: MATCH OFFICIALS & REFEREE CORPS
        paint.color = Color.rgb(226, 232, 240)
        canvas1.drawRect(20f, yPos, (pageWidth - 20).toFloat(), yPos + 18f, paint)
        textPaint.color = Color.rgb(15, 23, 42)
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("MATCH OFFICIALS & REFEREE CORPS (FIVB CHAPTER 8)", 26f, yPos + 13f, textPaint)

        yPos += 22f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 9f
        textPaint.color = Color.rgb(30, 41, 59)

        val officials = state.officials
        canvas1.drawText("1st Referee: ${officials.firstReferee.ifBlank { "—" }}", 30f, yPos + 10f, textPaint)
        canvas1.drawText("2nd Referee: ${officials.secondReferee.ifBlank { "—" }}", 300f, yPos + 10f, textPaint)
        yPos += 16f
        canvas1.drawText("Scorer 1: ${officials.scorer1.ifBlank { "—" }}", 30f, yPos + 10f, textPaint)
        canvas1.drawText("Scorer 2: ${officials.scorer2.ifBlank { "—" }}", 220f, yPos + 10f, textPaint)
        canvas1.drawText("Scorer 3: ${officials.scorer3.ifBlank { "—" }}", 400f, yPos + 10f, textPaint)
        yPos += 16f
        canvas1.drawText("Line Referee 1: ${officials.lineReferee1.ifBlank { "—" }}", 30f, yPos + 10f, textPaint)
        canvas1.drawText("Line Referee 2: ${officials.lineReferee2.ifBlank { "—" }}", 300f, yPos + 10f, textPaint)

        yPos += 24f

        // Section: SET RESULTS BREAKDOWN
        paint.color = Color.rgb(226, 232, 240)
        canvas1.drawRect(20f, yPos, (pageWidth - 20).toFloat(), yPos + 18f, paint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas1.drawText("COMPLETED SETS BREAKDOWN", 26f, yPos + 13f, textPaint)

        yPos += 22f
        textPaint.textSize = 8.5f
        textPaint.color = Color.rgb(100, 116, 139)
        canvas1.drawText("Set", 30f, yPos + 10f, textPaint)
        canvas1.drawText(teamAName, 80f, yPos + 10f, textPaint)
        canvas1.drawText(teamBName, 170f, yPos + 10f, textPaint)
        canvas1.drawText("Winner", 260f, yPos + 10f, textPaint)
        canvas1.drawText("Duration", 380f, yPos + 10f, textPaint)

        yPos += 14f
        textPaint.color = Color.rgb(15, 23, 42)
        if (state.completedSets.isEmpty()) {
            canvas1.drawText("Current Set 1: ${state.scoreTeamA} - ${state.scoreTeamB} (In progress)", 30f, yPos + 10f, textPaint)
            yPos += 16f
        } else {
            state.completedSets.forEach { s ->
                val setWinner = if (s.winnerTeamId == state.teamA.id) teamAName else teamBName
                val dur = (s.durationSeconds / 60).coerceAtLeast(1)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas1.drawText("Set ${s.setNumber}", 30f, yPos + 10f, textPaint)
                textPaint.typeface = Typeface.DEFAULT
                canvas1.drawText("${s.scoreTeamA}", 80f, yPos + 10f, textPaint)
                canvas1.drawText("${s.scoreTeamB}", 170f, yPos + 10f, textPaint)
                canvas1.drawText(setWinner, 260f, yPos + 10f, textPaint)
                canvas1.drawText("$dur min", 380f, yPos + 10f, textPaint)
                yPos += 15f
            }
        }

        yPos += 12f

        // Section: REGISTERED STARTING LINEUPS
        paint.color = Color.rgb(226, 232, 240)
        canvas1.drawRect(20f, yPos, (pageWidth - 20).toFloat(), yPos + 18f, paint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        textPaint.color = Color.rgb(15, 23, 42)
        canvas1.drawText("STARTING LINE-UPS (FIVB RULE 7.3)", 26f, yPos + 13f, textPaint)

        yPos += 22f
        textPaint.textSize = 8.5f
        if (state.setLineupHistory.isEmpty()) {
            textPaint.typeface = Typeface.DEFAULT
            canvas1.drawText("Default starting rosters recorded in match setup.", 30f, yPos + 10f, textPaint)
            yPos += 16f
        } else {
            state.setLineupHistory.toSortedMap().forEach { (setNum, record) ->
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas1.drawText("Set $setNum Lineups:", 30f, yPos + 10f, textPaint)
                yPos += 14f

                textPaint.typeface = Typeface.DEFAULT
                val aLine = (1..6).joinToString("  ") { "P$it:#${record.teamALineup[it]?.number ?: "--"}" }
                val aLib = record.teamALibero?.let { " [L:#${it.number}]" } ?: ""
                canvas1.drawText("${state.teamA.displayCode}: $aLine$aLib", 40f, yPos + 10f, textPaint)
                yPos += 13f

                val bLine = (1..6).joinToString("  ") { "P$it:#${record.teamBLineup[it]?.number ?: "--"}" }
                val bLib = record.teamBLibero?.let { " [L:#${it.number}]" } ?: ""
                canvas1.drawText("${state.teamB.displayCode}: $bLine$bLib", 40f, yPos + 10f, textPaint)
                yPos += 16f
            }
        }

        yPos += 10f

        // Section: MATCH STATISTICS TABLE
        paint.color = Color.rgb(226, 232, 240)
        canvas1.drawRect(20f, yPos, (pageWidth - 20).toFloat(), yPos + 18f, paint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        textPaint.color = Color.rgb(15, 23, 42)
        canvas1.drawText("MATCH STATISTICS COMPARISON", 26f, yPos + 13f, textPaint)

        yPos += 22f
        textPaint.textSize = 8.5f
        textPaint.color = Color.rgb(100, 116, 139)
        canvas1.drawText("Metric", 30f, yPos + 10f, textPaint)
        canvas1.drawText(teamAName, 200f, yPos + 10f, textPaint)
        canvas1.drawText(teamBName, 340f, yPos + 10f, textPaint)

        yPos += 14f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = Color.rgb(15, 23, 42)

        val totalKillsA = state.events.count { it.teamId == state.teamA.id && it.eventType.name.contains("KILL") }
        val totalKillsB = state.events.count { it.teamId == state.teamB.id && it.eventType.name.contains("KILL") }
        val totalAcesA = state.events.count { it.teamId == state.teamA.id && it.eventType.name.contains("ACE") }
        val totalAcesB = state.events.count { it.teamId == state.teamB.id && it.eventType.name.contains("ACE") }
        val totalBlocksA = state.events.count { it.teamId == state.teamA.id && it.eventType.name.contains("BLOCK") }
        val totalBlocksB = state.events.count { it.teamId == state.teamB.id && it.eventType.name.contains("BLOCK") }
        val pointsA = state.completedSets.sumOf { it.scoreTeamA } + state.scoreTeamA
        val pointsB = state.completedSets.sumOf { it.scoreTeamB } + state.scoreTeamB

        val stats = listOf(
            Triple("Total Points Scored", "$pointsA", "$pointsB"),
            Triple("Attack Kills", "$totalKillsA", "$totalKillsB"),
            Triple("Service Aces", "$totalAcesA", "$totalAcesB"),
            Triple("Block Points", "$totalBlocksA", "$totalBlocksB"),
            Triple("Sets Won", "${state.setsWonTeamA}", "${state.setsWonTeamB}")
        )

        stats.forEach { (metric, valA, valB) ->
            canvas1.drawText(metric, 30f, yPos + 10f, textPaint)
            canvas1.drawText(valA, 200f, yPos + 10f, textPaint)
            canvas1.drawText(valB, 340f, yPos + 10f, textPaint)
            yPos += 14f
        }

        // Official Signatures Block at bottom of Page 1
        yPos = pageHeight - 110f
        paint.color = Color.rgb(203, 213, 225)
        canvas1.drawLine(20f, yPos, (pageWidth - 20).toFloat(), yPos, paint)

        yPos += 18f
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.rgb(100, 116, 139)

        canvas1.drawText("1st Referee Signature", 30f, yPos, textPaint)
        canvas1.drawText("2nd Referee Signature", 160f, yPos, textPaint)
        canvas1.drawText("Scorer Signature", 290f, yPos, textPaint)
        canvas1.drawText("Captain $teamAName", 410f, yPos, textPaint)

        yPos += 30f
        paint.color = Color.rgb(148, 163, 184)
        canvas1.drawLine(30f, yPos, 130f, yPos, paint)
        canvas1.drawLine(160f, yPos, 260f, yPos, paint)
        canvas1.drawLine(290f, yPos, 380f, yPos, paint)
        canvas1.drawLine(410f, yPos, 510f, yPos, paint)

        // Page 1 footer
        textPaint.textSize = 7.5f
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.typeface = Typeface.DEFAULT
        canvas1.drawText("Page 1 of 2  •  Official Scoresheet Record", (pageWidth / 2f) - 60f, pageHeight - 20f, textPaint)

        pdfDocument.finishPage(page1)

        // ================= PAGE 2: CHRONOLOGICAL POINTS LOG =================
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        // Header Background
        paint.color = Color.rgb(15, 23, 42)
        canvas2.drawRect(0f, 0f, pageWidth.toFloat(), 45f, paint)
        paint.color = Color.rgb(245, 158, 11)
        canvas2.drawRect(0f, 45f, pageWidth.toFloat(), 47f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas2.drawText("VOLLEYREF — CHRONOLOGICAL POINT-BY-POINT TIMELINE", 20f, 28f, textPaint)

        yPos = 65f
        textPaint.textSize = 8f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.rgb(100, 116, 139)
        canvas2.drawText("#", 25f, yPos, textPaint)
        canvas2.drawText("Set", 45f, yPos, textPaint)
        canvas2.drawText("Time", 75f, yPos, textPaint)
        canvas2.drawText("Score", 125f, yPos, textPaint)
        canvas2.drawText("Team", 175f, yPos, textPaint)
        canvas2.drawText("Action / Event", 215f, yPos, textPaint)
        canvas2.drawText("Players / Note", 340f, yPos, textPaint)

        paint.color = Color.rgb(203, 213, 225)
        canvas2.drawLine(20f, yPos + 4f, (pageWidth - 20).toFloat(), yPos + 4f, paint)
        yPos += 16f

        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = Color.rgb(15, 23, 42)

        val maxEventsOnPage2 = 48
        state.events.take(maxEventsOnPage2).forEachIndexed { index, event ->
            val teamCode = when (event.teamId) {
                state.teamA.id -> state.teamA.displayCode
                state.teamB.id -> state.teamB.displayCode
                else -> "---"
            }
            canvas2.drawText("${index + 1}", 25f, yPos, textPaint)
            canvas2.drawText("S${event.setNumber}", 45f, yPos, textPaint)
            canvas2.drawText(event.formattedTime, 75f, yPos, textPaint)
            canvas2.drawText(event.scoreDisplay, 125f, yPos, textPaint)
            canvas2.drawText(teamCode, 175f, yPos, textPaint)
            canvas2.drawText(event.eventType.label.take(22), 215f, yPos, textPaint)

            val noteStr = if (event.note.isNotBlank()) event.note else event.primaryPlayer?.let { "#${it.number} ${it.name}" } ?: ""
            canvas2.drawText(noteStr.take(38), 340f, yPos, textPaint)

            yPos += 15f
            if (yPos > pageHeight - 45f) return@forEachIndexed
        }

        if (state.events.size > maxEventsOnPage2) {
            textPaint.color = Color.rgb(100, 116, 139)
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas2.drawText("... and ${state.events.size - maxEventsOnPage2} more events recorded in CSV export", 25f, yPos + 8f, textPaint)
        }

        // Page 2 footer
        textPaint.textSize = 7.5f
        textPaint.color = Color.rgb(148, 163, 184)
        textPaint.typeface = Typeface.DEFAULT
        canvas2.drawText("Page 2 of 2  •  Official Scoresheet Record", (pageWidth / 2f) - 60f, pageHeight - 20f, textPaint)

        pdfDocument.finishPage(page2)

        // Write to output file
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    /**
     * Shares a generated CSV or PDF report file via Android Intent chooser
     * (Gmail, Outlook, WhatsApp, Telegram, Drive, Bluetooth, etc.)
     */
    fun shareReportFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Attached is the official FIVB match report: $title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "Share Match Report ($mimeType)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
