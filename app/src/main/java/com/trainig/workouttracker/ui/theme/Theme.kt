package com.trainig.workouttracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC8BBFF),
    onPrimary = Color(0xFF2A1B7A),
    primaryContainer = Color(0xFF4A35C0),
    onPrimaryContainer = Color(0xFFE6DEFF),
    secondary = Color(0xFFFFB0D3),
    onSecondary = Color(0xFF5E1138),
    secondaryContainer = Color(0xFF8C1A55),
    onSecondaryContainer = Color(0xFFFFD8E8),
    tertiary = Color(0xFFFFB68A),
    onTertiary = Color(0xFF552100),
    tertiaryContainer = Color(0xFF8A3B00),
    onTertiaryContainer = Color(0xFFFFDCC6),
    background = Color(0xFF120E1F),
    onBackground = Color(0xFFEDE9F7),
    surface = Color(0xFF1C1730),
    onSurface = Color(0xFFEDE9F7),
    surfaceVariant = Color(0xFF2A2342),
    onSurfaceVariant = Color(0xFFCBC3DE),
    surfaceContainerLowest = Color(0xFF0E0B18),
    surfaceContainerLow = Color(0xFF181329),
    surfaceContainer = Color(0xFF1E1832),
    surfaceContainerHigh = Color(0xFF261F3D),
    surfaceContainerHighest = Color(0xFF2F2849)
)

private val LightColorScheme = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF21005E),
    secondary = Magenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8E8),
    onSecondaryContainer = Color(0xFF3E0021),
    tertiary = Orange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC6),
    onTertiaryContainer = Color(0xFF3A1600),
    background = Color(0xFFF7F5FF),
    onBackground = Color(0xFF1B1530),
    surface = Color.White,
    onSurface = Color(0xFF1B1530),
    surfaceVariant = Color(0xFFEDE8FA),
    onSurfaceVariant = Color(0xFF5B5470),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF9FF),
    surfaceContainer = Color(0xFFF4F0FF),
    surfaceContainerHigh = Color(0xFFEFEAFD),
    surfaceContainerHighest = Color(0xFFE9E3FA)
)

@Composable
fun WorkoutTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but it replaces the brand palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
