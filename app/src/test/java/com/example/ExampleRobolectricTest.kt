package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VolleyRef", appName)
  }

  @Test
  fun `verify FIVB default rules configuration`() {
    val defaultRules = com.example.model.FivbRulesConfig.defaultFivb()
    assertEquals(25, defaultRules.pointsRegularSet)
    assertEquals(15, defaultRules.pointsDecidingSet)
    assertEquals(2, defaultRules.advantageRequired)
    assertEquals(6, defaultRules.maxSubstitutionsPerSet)
    assertEquals(2, defaultRules.timeoutsPerTeamPerSet)
  }

  @Test
  fun `verify set starting lineup storage and retrieval`() {
    val initialState = com.example.data.repository.MatchRepository.createInitialMatchState()
    org.junit.Assert.assertTrue(initialState.setLineupHistory.containsKey(1))
    val set1Lineup = initialState.setLineupHistory[1]!!
    assertEquals(6, set1Lineup.teamALineup.size)
    assertEquals(6, set1Lineup.teamBLineup.size)
    org.junit.Assert.assertNotNull(set1Lineup.teamALineup[1])
  }
}
