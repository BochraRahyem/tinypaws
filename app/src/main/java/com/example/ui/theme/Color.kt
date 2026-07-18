package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable

// Base Colors
val BaseDeepBurgundy = Color(0xFF5C1F2E)
val BaseWine = Color(0xFF8A3B4C)
val BaseMauve = Color(0xFFC97B8B)
val BaseBlushPink = Color(0xFFF0C4C9)
val BaseCream = Color(0xFFFBEEE9)
val BaseGold = Color(0xFFD9A860)
val BaseInk = Color(0xFF3A1620)
val BaseDarkBgStart = Color(0xFF2C0F17)
val BaseDarkBgEnd = Color(0xFF3A1620)

val BaseWhite = Color(0xFFFFFFFF)
val BaseSoftGray = Color(0xFFFDECE9)

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
