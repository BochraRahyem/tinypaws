package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class IntroPage(
    val title: String,
    val description: String,
    val emoji: String
)

@Composable
fun TinyPawsIntroSequence(
    onComplete: () -> Unit
) {
    var currentScreenIndex by remember { mutableStateOf(0) }

    val screens = listOf(
        IntroPage(
            title = "Welcome to TinyPaws",
            description = "Our mission is to protect, nourish, and stand up for every street cat and kitten. Together, we can be their voice when they need it most.",
            emoji = "🐾🐈"
        ),
        IntroPage(
            title = "Profiles & Health Tracking",
            description = "Track stray cat profiles, keep detailed care logs, record feeding schedules, medical notes, and monitor health progress over time.",
            emoji = "🐱🩺"
        ),
        IntroPage(
            title = "Community & Feeding Stations",
            description = "Locate stray cat reports, add local feeding stations, receive extreme weather shelter alerts, and coordinate with nearby caregivers.",
            emoji = "🗺️🍽️"
        ),
        IntroPage(
            title = "Care Guides, Quizzes & AI Helper",
            description = "Learn winter survival tips, build DIY cat shelters, take educational quizzes to earn certificates, and ask the TinyPaws AI Helper for rescue advice 24/7.",
            emoji = "📚🤖"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OmbreGradientBrushLight)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount < -50f) { // Swipe left -> Next
                        if (currentScreenIndex < 3) {
                            currentScreenIndex++
                        }
                    } else if (dragAmount > 50f) { // Swipe right -> Prev
                        if (currentScreenIndex > 0) {
                            currentScreenIndex--
                        }
                    }
                }
            }
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Skip button (Top Right)
        Text(
            text = "Skip",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                color = BaseCream
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { onComplete() }
                .padding(8.dp)
        )

        // Screen content in the center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glassmorphic welcome card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .glassyCard(shape = RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = screens[currentScreenIndex].emoji,
                        fontSize = 80.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = screens[currentScreenIndex].title,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = BaseCream,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = screens[currentScreenIndex].description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = QuicksandFontFamily,
                            color = BaseCream.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }

        // Bottom navigation (indicators + buttons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    val isSelected = currentScreenIndex == i
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                color = if (isSelected) BaseCream else BaseCream.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Next / Get Started button (Oval, shape=CircleShape, padded)
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    if (currentScreenIndex == 3) {
                        onComplete()
                    } else {
                        currentScreenIndex++
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BaseCream,
                    contentColor = BaseDeepBurgundy
                ),
                shape = CircleShape
            ) {
                Text(
                    text = if (currentScreenIndex == 3) "Get started" else "Next",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
