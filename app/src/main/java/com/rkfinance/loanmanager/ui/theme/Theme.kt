package com.rkfinance.loanmanager.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkLuxuryColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = OnPrimaryGold,
    secondary = SecondaryGold,
    onSecondary = Color.Black, // Or a very dark grey
    tertiary = AccentGold,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorRed,
    onError = Color.Black,
    surfaceVariant = Color(0xFF2C2C2C), // Slightly different surface variant
    onSurfaceVariant = TextSecondaryDark,
    outline = SecondaryGold.copy(alpha = 0.5f)
)

@Composable
fun LoanmanagerTheme(
    // Dynamic color is available on Android 12+ but we want our luxury theme
    dynamicColor: Boolean = false, // Set to false to enforce luxury theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkLuxuryColorScheme // Always use our luxury theme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Or SurfaceDark
            window.navigationBarColor = colorScheme.background.toArgb() // Or SurfaceDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Use our custom typography
        shapes = Shapes,
        content = content
    )
}
    