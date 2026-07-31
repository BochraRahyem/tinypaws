package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberHapticOnClick(onClick: () -> Unit): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return remember(onClick) {
        {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    }
}
