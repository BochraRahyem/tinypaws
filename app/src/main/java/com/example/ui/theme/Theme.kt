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
    primary = BaseMauve,
    onPrimary = BaseWhite,
    primaryContainer = PastelLightPink,
    onPrimaryContainer = BaseDeepBurgundy,
    secondary = BaseWine,
    onSecondary = BaseWhite,
    secondaryContainer = BaseSoftGray,
    onSecondaryContainer = BaseDeepBurgundy,
    tertiary = BaseMauve,
    onTertiary = BaseWhite,
    background = BaseCream,
    onBackground = BaseInk,
    surface = BaseWhite,
    onSurface = BaseInk,
    surfaceVariant = PastelMint,
    onSurfaceVariant = BaseInk,
    outline = PastelLavender.copy(alpha = 0.7f)
)

private val DarkColorScheme = darkColorScheme(
    primary = BaseBlushPink,
    onPrimary = BaseDarkBgStart,
    primaryContainer = BaseWine,
    onPrimaryContainer = BaseBlushPink,
    secondary = BaseMauve,
    onSecondary = BaseDarkBgStart,
    secondaryContainer = BaseDeepBurgundy,
    onSecondaryContainer = BaseMauve,
    tertiary = BaseBlushPink,
    onTertiary = BaseDarkBgStart,
    background = BaseDarkBgStart,
    onBackground = BaseBlushPink,
    surface = BaseDarkBgStart,
    onSurface = BaseBlushPink,
    surfaceVariant = BaseDarkBgEnd,
    onSurfaceVariant = BaseMauve,
    outline = BaseMauve
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
