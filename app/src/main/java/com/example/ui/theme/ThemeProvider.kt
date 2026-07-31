package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.LocalLanguage

@Composable
fun ThemeProvider(
    currentLanguage: String,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (currentLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalLanguage provides currentLanguage,
        content = content
    )
}
