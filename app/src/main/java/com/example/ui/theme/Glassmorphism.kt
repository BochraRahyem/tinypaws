package com.example.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.glassyCard(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp
): Modifier {
    val isDark = LocalIsDarkMode.current
    val bgColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFFFDCE4).copy(alpha = 0.45f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Color(0xFF8A3B4C).copy(alpha = 0.15f)

    return this
        .shadow(
            elevation = elevation, 
            shape = shape, 
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .background(color = bgColor, shape = shape)
        .border(BorderStroke(1.dp, borderColor), shape = shape)
        .clip(shape)
}

@Composable
fun Modifier.glassyButton(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 2.dp,
        label = "ButtonLiftAnimation"
    )
    val isDark = LocalIsDarkMode.current
    val bgColor = if (isDark) Color.White.copy(alpha = 0.1f) else BlushPink.copy(alpha = 0.5f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.5f)
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Mauve.copy(alpha = 0.3f)

    val glossModifier = Modifier
        .background(
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
            )
        )

    return this
        .shadow(
            elevation = animatedElevation,
            shape = CircleShape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .background(color = bgColor, shape = CircleShape)
        .border(BorderStroke(1.dp, borderColor), shape = CircleShape)
        .clip(CircleShape)
        .then(glossModifier)
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            onClick = onClick
        )
}
