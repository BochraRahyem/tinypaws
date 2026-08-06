package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

// Base Colors (Updated for coziness)
val BaseDeepBurgundy = Color(0xFF8D6E63) // Warm Brown-Burgundy
val BaseWine = Color(0xFFBCAAA4)
val BaseMauve = Color(0xFFD7CCC8)
val BaseBlushPink = Color(0xFFFFEBEE)
val BaseCream = Color(0xFFFFF9F5)
val BaseGold = Color(0xFFFFD54F)
val BaseInk = Color(0xFF4E342E)
val BaseDarkBgStart = Color(0xFF2D2422)
val BaseDarkBgEnd = Color(0xFF3E2723)

// New Pastel Palette
val PastelPink = Color(0xFFFFE1E9) // Softer Pink
val PastelLightPink = Color(0xFFFFF0F5)
val PastelBlue = Color(0xFFD0E1F9) // Soft Blue
val PastelBlueContainer = Color(0xFFE3F2FD)
val PastelPeach = Color(0xFFFFE0B2)
val PastelMint = Color(0xFFE8F5E9)
val PastelLavender = Color(0xFFF3E5F5)
val SoftCream = Color(0xFFFFF9C4)

val BaseWhite = Color(0xFFFFFFFF)
val BaseSoftGray = Color(0xFFFAFAFA)

// Dynamic Colors
val DeepBurgundy: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseBlushPink else BaseDeepBurgundy

val Wine: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseMauve else BaseWine

val Mauve: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseWine else BaseMauve

val BlushPink: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseDeepBurgundy else BaseBlushPink

val Cream: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseDarkBgStart else BaseCream

val Gold = BaseGold
val Ink: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseCream else BaseInk

val DarkBgStart = BaseDarkBgStart
val DarkBgEnd = BaseDarkBgEnd

// Supporting Palette
val White: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseDarkBgEnd else BaseWhite

val SoftGray: Color
    @Composable get() = if (LocalIsDarkMode.current) BaseInk else BaseSoftGray

val TextDark: Color @Composable get() = if (LocalIsDarkMode.current) BaseMauve else BaseInk
val TextMuted: Color @Composable get() = if (LocalIsDarkMode.current) BaseMauve.copy(alpha = 0.8f) else BaseWine
val GreenSuccess = Color(0xFF6B8E23)
val OrangeWarning = BaseGold
val RedError = Color(0xFFB22222)

// Compatibility Mappings
val PastelBlueBg: Color @Composable get() = Cream
val PastelPurplePrimary: Color @Composable get() = DeepBurgundy
val PastelPurpleDark: Color @Composable get() = Wine
val PastelPinkAccent: Color @Composable get() = BlushPink
val PastelPinkDark: Color @Composable get() = Mauve
val LightPurpleBg: Color @Composable get() = Cream

val OmbreGradientBrushLight = Brush.verticalGradient(
    colors = listOf(BaseCream, BaseBlushPink, BaseMauve, BaseWine, BaseDeepBurgundy)
)
val OmbreGradientBrushDark = Brush.verticalGradient(
    colors = listOf(BaseDarkBgStart, BaseDarkBgEnd)
)
