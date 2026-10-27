package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.BigScoreboardScreen
import com.example.ui.screens.FivbRulesConfigScreen
import com.example.ui.screens.MatchHistoryScreen
import com.example.ui.screens.MatchSetupScreen
import com.example.ui.screens.RefereeMatchScreen
import com.example.ui.screens.ScoreHistoryScreen
import com.example.ui.theme.CourtDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RefereeScreen
import com.example.ui.viewmodel.RefereeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RefereeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CourtDark
                ) {
                    RefereeApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RefereeApp(viewModel: RefereeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            RefereeScreen.MATCH -> {
                RefereeMatchScreen(viewModel = viewModel)
            }
            RefereeScreen.MATCH_LOG -> {
                ScoreHistoryScreen(viewModel = viewModel)
            }
            RefereeScreen.RULES_CONFIG -> {
                FivbRulesConfigScreen(viewModel = viewModel)
            }
            RefereeScreen.MATCH_SETUP -> {
                MatchSetupScreen(viewModel = viewModel)
            }
            RefereeScreen.MATCH_HISTORY -> {
                MatchHistoryScreen(viewModel = viewModel)
            }
            RefereeScreen.BIG_SCOREBOARD -> {
                BigScoreboardScreen(viewModel = viewModel)
            }
        }
    }
}
