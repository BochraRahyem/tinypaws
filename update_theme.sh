cat << 'INNER_EOF' > app/src/main/java/com/example/ui/theme/Theme.kt
package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDarkMode = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = DeepBurgundy,
    onPrimary = White,
    primaryContainer = BlushPink,
    onPrimaryContainer = DeepBurgundy,
    secondary = Wine,
    onSecondary = White,
    secondaryContainer = BlushPink,
    onSecondaryContainer = Wine,
    tertiary = Mauve,
    onTertiary = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = SoftGray,
    onSurfaceVariant = Wine,
    outline = Mauve
)

private val DarkColorScheme = darkColorScheme(
    primary = BlushPink,
    onPrimary = DarkBgStart,
    primaryContainer = Wine,
    onPrimaryContainer = BlushPink,
    secondary = Mauve,
    onSecondary = DarkBgStart,
    secondaryContainer = DeepBurgundy,
    onSecondaryContainer = Mauve,
    tertiary = BlushPink,
    onTertiary = DarkBgStart,
    background = DarkBgStart,
    onBackground = BlushPink,
    surface = DarkBgStart,
    onSurface = BlushPink,
    surfaceVariant = DarkBgEnd,
    onSurfaceVariant = Mauve,
    outline = Mauve
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
INNER_EOF
