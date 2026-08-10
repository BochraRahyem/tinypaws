package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDarkMode = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = BaseDeepBurgundy,
    onPrimary = BaseWhite,
    primaryContainer = PastelLightPink,
    onPrimaryContainer = BaseDeepBurgundy,
    secondary = BaseWine,
    onSecondary = BaseWhite,
    secondaryContainer = BaseBlushPink,
    onSecondaryContainer = BaseDeepBurgundy,
    tertiary = BaseMauve,
    onTertiary = BaseWhite,
    background = BaseCream,
    onBackground = BaseInk,
    surface = BaseWhite,
    onSurface = BaseInk,
    surfaceVariant = PastelLightPink,
    onSurfaceVariant = BaseInk,
    outline = BaseMauve.copy(alpha = 0.7f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8A9E),
    onPrimary = BaseDarkBgStart,
    primaryContainer = Color(0xFF5D1E2A),
    onPrimaryContainer = Color(0xFFFFE5EC),
    secondary = Color(0xFFFFB3C1),
    onSecondary = BaseDarkBgStart,
    secondaryContainer = Color(0xFF3B1A22),
    onSecondaryContainer = Color(0xFFFFE5EC),
    tertiary = Color(0xFFE892A2),
    onTertiary = BaseDarkBgStart,
    background = BaseDarkBgStart,
    onBackground = Color(0xFFFFE5EC),
    surface = BaseDarkBgEnd,
    onSurface = Color(0xFFFFE5EC),
    surfaceVariant = Color(0xFF3B1A22),
    onSurfaceVariant = Color(0xFFFFB3C1),
    outline = Color(0xFFE892A2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalIsDarkMode provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
