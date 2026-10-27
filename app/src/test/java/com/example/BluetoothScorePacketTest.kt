package com.example

import com.example.model.BluetoothScorePacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BluetoothScorePacketTest {

    @Test
    fun testPacketSerializationAndDeserialization() {
        val originalPacket = BluetoothScorePacket(
            teamAName = "Volley Masters",
            teamBName = "Spike Legends",
            teamACode = "VM",
            teamBCode = "SL",
            teamAColorHex = 0xFF2563EB,
            teamBColorHex = 0xFFDC2626,
            scoreA = 24,
            scoreB = 22,
            setsWonA = 1,
            setsWonB = 0,
            currentSet = 2,
            isDecidingSet = false,
            servingTeamId = "team_a",
            targetPoints = 25,
            timeoutsUsedA = 1,
            timeoutsRemainingA = 1,
            timeoutsUsedB = 2,
            timeoutsRemainingB = 0,
            maxTimeoutsPerSet = 2,
            subsUsedA = 3,
            subsRemainingA = 3,
            subsUsedB = 5,
            subsRemainingB = 1,
            isMatchFinished = false,
            isSetFinished = false,
            activeTimeoutSeconds = 18,
            activeTimeoutTeamName = "Volley Masters",
            clockFormatted = "18:42",
            clockIsRunning = true,
            completedSetsSummary = listOf("S1: 25-21")
        )

        val jsonString = originalPacket.toJsonString()
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("Volley Masters"))
        assertTrue(jsonString.contains("Spike Legends"))

        val parsedPacket = BluetoothScorePacket.fromJsonString(jsonString)
        assertNotNull(parsedPacket)
        assertEquals("Volley Masters", parsedPacket?.teamAName)
        assertEquals("Spike Legends", parsedPacket?.teamBName)
        assertEquals(24, parsedPacket?.scoreA)
        assertEquals(22, parsedPacket?.scoreB)
        assertEquals(1, parsedPacket?.setsWonA)
        assertEquals(0, parsedPacket?.setsWonB)
        assertEquals(2, parsedPacket?.currentSet)
        assertEquals(1, parsedPacket?.timeoutsUsedA)
        assertEquals(1, parsedPacket?.timeoutsRemainingA)
        assertEquals(2, parsedPacket?.timeoutsUsedB)
        assertEquals(0, parsedPacket?.timeoutsRemainingB)
        assertEquals(18, parsedPacket?.activeTimeoutSeconds)
        assertEquals("Volley Masters", parsedPacket?.activeTimeoutTeamName)
        assertEquals("18:42", parsedPacket?.clockFormatted)
        assertEquals(true, parsedPacket?.clockIsRunning)
        assertEquals(1, parsedPacket?.completedSetsSummary?.size)
        assertEquals("S1: 25-21", parsedPacket?.completedSetsSummary?.first())
    }
}
