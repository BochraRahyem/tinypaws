package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

// Base Colors (Richer pastel/coral pink & warm brown aesthetic)
val BaseDeepBurgundy = Color(0xFF5D1E2A) // Rich Coral Burgundy
val BaseWine = Color(0xFF8B3A4A)         // Warm Wine
val BaseMauve = Color(0xFFC47B89)        // Rose Mauve
val BaseBlushPink = Color(0xFFFFB3C1)    // Rich Coral Pink
val BaseCream = Color(0xFFFFF0F3)        // Soft Warm Rose Cream
val BaseGold = Color(0xFFFFB703)         // Golden Warm Yellow
val BaseInk = Color(0xFF33131A)          // High contrast dark espresso text
val BaseDarkBgStart = Color(0xFF1E0E12)  // Deep rich velvet burgundy-black
val BaseDarkBgEnd = Color(0xFF2C131A)    // Warm dark coral-wine background

// New Rich Pastel Palette
val PastelPink = Color(0xFFFFC2D1)       // Vibrant Soft Coral Pink
val PastelLightPink = Color(0xFFFFE5EC)  // Warm Light Pink
val PastelBlue = Color(0xFFD0E1F9)       // Soft Sky Blue
val PastelBlueContainer = Color(0xFFE3F2FD)
val PastelPeach = Color(0xFFFFD1B3)      // Warm Peach
val PastelMint = Color(0xFFE0F2FE)       // Soft Fresh Mint
val PastelLavender = Color(0xFFF3E5F5)
val SoftCream = Color(0xFFFFF5D6)

val BaseWhite = Color(0xFFFFFFFF)
val BaseSoftGray = Color(0xFFFFF7F8)

// Dynamic Colors (High contrast, cohesive between Light & Dark modes)
val DeepBurgundy: Color
    @Composable get() = if (LocalIsDarkMode.current) Color(0xFFFF8A9E) else BaseDeepBurgundy

val Wine: Color
    @Composable get() = if (LocalIsDarkMode.current) Color(0xFFFFB3C1) else BaseWine

val Mauve: Color
    @Composable get() = if (LocalIsDarkMode.current) Color(0xFFE892A2) else BaseMauve

val BlushPink: Color
    @Composable get() = if (LocalIsDarkMode.current) Color(0xFF4A1A24) else BaseBlushPink

val Cream: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseDarkBgStart else BaseCream

val Gold = BaseGold
val Ink: Color
    @Composable get() = if (LocalIsDarkMode.current) Color(0xFFFFE5EC) else BaseInk

val DarkBgStart = BaseDarkBgStart
val DarkBgEnd = BaseDarkBgEnd

// Supporting Palette
val White: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseDarkBgEnd else BaseWhite

val SoftGray: Color
    @Composable get() = if (LocalIsDarkMode.current) Color(0xFF381820) else BaseSoftGray

val TextDark: Color @Composable get() = if (LocalIsDarkMode.current) Color(0xFFFFE5EC) else BaseInk
val TextMuted: Color @Composable get() = if (LocalIsDarkMode.current) Color(0xFFFFB3C1) else BaseWine
val GreenSuccess = Color(0xFF4CAF50)
val OrangeWarning = Color(0xFFFF9800)
val RedError = Color(0xFFE53935)

// Compatibility Mappings
val PastelBlueBg: Color @Composable get() = Cream
val PastelPurplePrimary: Color @Composable get() = DeepBurgundy
val PastelPurpleDark: Color @Composable get() = Wine
val PastelPinkAccent: Color @Composable get() = BlushPink
val PastelPinkDark: Color @Composable get() = Mauve
val LightPurpleBg: Color @Composable get() = Cream

val OmbreGradientBrushLight = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFF0F3), Color(0xFFFFE5EC), Color(0xFFFFC2D1), Color(0xFFE892A2), Color(0xFF5D1E2A))
)
val OmbreGradientBrushDark = Brush.verticalGradient(
    colors = listOf(BaseDarkBgStart, BaseDarkBgEnd, Color(0xFF3A1822))
)

