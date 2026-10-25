package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RefereeColorScheme = darkColorScheme(
    primary = VolleyballGold,
    onPrimary = CourtDark,
    primaryContainer = CourtSurfaceHigh,
    onPrimaryContainer = VolleyballGold,
    secondary = VolleyballBlue,
    onSecondary = TextPrimaryDark,
    secondaryContainer = CourtSurface,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = RefereeWhistle,
    onTertiary = CourtDark,
    background = CourtDark,
    onBackground = TextPrimaryDark,
    surface = CourtSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CourtSurfaceHigh,
    onSurfaceVariant = TextSecondaryDark,
    error = SanctionRed,
    onError = TextPrimaryDark,
    outline = CourtLines
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force referee high-contrast dark theme by default for outdoor/indoor court visibility
    content: @Composable () -> Unit
) {
    val colorScheme = RefereeColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CourtDark.toArgb()
                window.navigationBarColor = CourtDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
