package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * A highly optimized, scale-independent, responsive Pixel Canvas.
 * Draws any grid of character pixel maps using a nearest-neighbor-like rect draw.
 */
@Composable
fun PixelCanvas(
    pixelGrid: List<String>,
    colorMap: Map<Char, Color>,
    pixelSize: Dp = 2.5.dp,
    modifier: Modifier = Modifier
) {
    val rows = pixelGrid.size
    val cols = if (rows > 0) pixelGrid[0].length else 0
    if (rows == 0 || cols == 0) return

    Box(
        modifier = modifier.size(
            width = pixelSize * cols,
            height = pixelSize * rows
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pxW = pixelSize.toPx()
            val pxH = pixelSize.toPx()
            for (r in 0 until rows) {
                val rowStr = pixelGrid[r]
                for (c in 0 until cols) {
                    if (c < rowStr.length) {
                        val char = rowStr[c]
                        val color = colorMap[char] ?: Color.Transparent
                        if (color != Color.Transparent) {
                            drawRect(
                                color = color,
                                topLeft = androidx.compose.ui.geometry.Offset(c * pxW, r * pxH),
                                size = androidx.compose.ui.geometry.Size(pxW + 0.2f, pxH + 0.2f) // slight bleed to avoid hairline cracks
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pixel Cats in multiple varieties (sitting, sleeping, walking).
 * Faithfully depicts the calico/torbie cat (orange, dark brown/black tabby, pure white blaze/chest/paws,
 * olive-green eyes, cute pink nose, and striped tail).
 */
object PixelCatModels {
    // 20x20 Sitting Calico / Torbie Cat
    val SittingCalico = listOf(
        "....................",
        "..oO........bB......",
        ".oONN......bBNN.....",
        ".oONNN....bBNNN.....",
        ".OOGNN....BBDNN.....",
        "..OOOOgWWdBBBb......",
        ".OoOOgWwwWdBBBBb....",
        ".oOOgWWwwWWdBBBb....",
        ".OOoEeWwwWeEBbdB....",
        ".gOOWWWNNWWWBbd.....",
        "..gWWWWNWWWWWdB.....",
        "...WwWWWWWWwWb......",
        "..oOWWWWWWWWWdBb....",
        ".oOOBWWWWWWWbBdB....",
        ".OoOBBWWWWWWBBdB....",
        ".oOoBBWWWWWWbBoB....",
        ".OoOoBWWWWWWbBoB....",
        ".oObBBWWWWWWBoBb....",
        "..WWWWWW..WWWWWW....",
        ".bBoOoBb..bBoOoB...."
    )

    // 20x20 Blinking Sitting Calico Cat
    val BlinkingCalico = listOf(
        "....................",
        "..oO........bB......",
        ".oONN......bBNN.....",
        ".oONNN....bBNNN.....",
        ".OOGNN....BBDNN.....",
        "..OOOOgWWdBBBb......",
        ".OoOOgWwwWdBBBBb....",
        ".oOOgWWwwWWdBBBb....",
        ".OOobbWwwWbbBbdB....",
        ".gOOWWWNNWWWBbd.....",
        "..gWWWWNWWWWWdB.....",
        "...WwWWWWWWwWb......",
        "..oOWWWWWWWWWdBb....",
        ".oOOBWWWWWWWbBdB....",
        ".OoOBBWWWWWWBBdB....",
        ".oOoBBWWWWWWbBoB....",
        ".OoOoBWWWWWWbBoB....",
        ".oObBBWWWWWWBoBb....",
        "..WWWWWW..WWWWWW....",
        ".bBoOoBb..bBoOoB...."
    )

    // 20x20 Tail-Flick Sitting Calico Cat
    val TailFlickCalico = listOf(
        "....................",
        "..oO........bB......",
        ".oONN......bBNN.....",
        ".oONNN....bBNNN.....",
        ".OOGNN....BBDNN.....",
        "..OOOOgWWdBBBb......",
        ".OoOOgWwwWdBBBBb....",
        ".oOOgWWwwWWdBBBb....",
        ".OOoEeWwwWeEBbdB....",
        ".gOOWWWNNWWWBbd.....",
        "..gWWWWNWWWWWdB.....",
        "...WwWWWWWWwWb......",
        "..oOWWWWWWWWWdBb....",
        ".oOOBWWWWWWWbBdB....",
        ".OoOBBWWWWWWBBdB....",
        ".oOoBBWWWWWWbBoB....",
        ".OoOoBWWWWWWbBoB....",
        ".oObBBWWWWWWBoBb....",
        "..WWWWWW..WWWWWW....",
        "..bBoOoBb..bBoOoBb.."
    )

    // 20x16 Sleeping Calico in Cozy Bed
    val SleepingCalicoBed = listOf(
        "....................",
        ".......oO...bB......",
        "......oONN.bBNN.....",
        ".....OoOgWWdBBBb....",
        "....OoOgWwwWdBBBb...",
        "...oOOgWWwwWWdBBb...",
        "...OOobbWwwWbbBdB...",
        "...gOOWWWNNWWWBb....",
        "..oOoBWWWWWWbBoB....",
        ".oObBBWWWWWWBoBbo...",
        ".bBoOoBbWWWWbBoOoB..",
        "SSSSSSSSSSSSSSSSSSSS",
        "SSSSSSSSSSSSSSSSSSSS",
        ".ssssssssssssssssss.",
        "..ssssssssssssssss..",
        "...................."
    )

    // Aliases for compatibility
    val SittingOrange = SittingCalico
    val BlinkingOrange = BlinkingCalico
    val SleepingGrey = SleepingCalicoBed

    val Paw = listOf(
        "................",
        "...PP......PP...",
        "..PPPP....PPPP..",
        "...PP......PP...",
        ".PP..........PP.",
        "PPPP........PPPP",
        ".PP..........PP.",
        ".....PPPPPP.....",
        "...PPPPPPPPPP...",
        "..PPPPPPPPPPPP..",
        "...PPPPPPPPPP...",
        ".....PPPPPP.....",
        "................"
    )

    val Sparkle = listOf(
        ".......Y.......",
        "......YYY......",
        "......YHY......",
        "..YYYYYHYYYYY..",
        "...YYYHHHYYY...",
        "....YHHHHHY....",
        "...YYYHHHYYY...",
        "..YYYYYHYYYYY..",
        "......YHY......",
        "......YYY......",
        ".......Y......."
    )

    val Heart = listOf(
        "................",
        "..RRRR....RRRR..",
        ".RRRRRR..RRRRRR.",
        "RRRHHHRRRRRRRRRR",
        "RRHHHHHRRRRRRRRR",
        "RRRHHHRRRRRRRRRR",
        ".RRRRRRRRRRRRRR.",
        "..RRRRRRRRRRRR..",
        "...RRRRRRRRRR...",
        "....RRRRRRRR....",
        ".....RRRRRR.....",
        "......RRRR......",
        ".......RR.......",
        "................"
    )

    val Star = listOf(
        ".......Y.......",
        "......YYY......",
        "......YYY......",
        "YYYYYYYYYYYYYYY",
        ".YYYYYYYYYYYYY.",
        "..YYYYYYYYYYY..",
        "...YYYYYYYYY...",
        "....YYYYYYY....",
        "...YYYY.YYYY...",
        "..YYYY...YYYY..",
        ".YYY.......YYY.",
        ".YY.........YY."
    )

    val Sun = listOf(
        ".......Y.......",
        "..Y...YYY...Y..",
        "...Y.YYYYY.Y...",
        "....YYYYYYY....",
        "YYYYYYYYYYYYYYY",
        "....YYYYYYY....",
        "...Y.YYYYY.Y...",
        "..Y...YYY...Y..",
        ".......Y......."
    )

    val Cloud = listOf(
        ".....WWWW.....",
        "...WWWWWWWW...",
        "..WWWWWWWWWW..",
        "WWWWWWWWWWWWWW",
        "WWWWWWWWWWWWWW",
        ".WWWWWWWWWWWW."
    )

    val Rain = listOf(
        ".....WWWW.....",
        "...WWWWWWWW...",
        "..WWWWWWWWWW..",
        "WWWWWWWWWWWWWW",
        "WWWWWWWWWWWWWW",
        ".c.c.c.c.c.c..",
        "..c.c.c.c.c...",
        "...c.c.c.c...."
    )

    val MapMarker = listOf(
        "....RRRRRR....",
        "..RRRRRRRRRR..",
        ".RRRRWWWWRRRR.",
        ".RRRRWWWWRRRR.",
        ".RRRRWWWWRRRR.",
        "..RRRRRRRRRR..",
        "...RRRRRRRR...",
        "....RRRRRR....",
        ".....RRRR.....",
        "......RR......",
        "......RR......"
    )

    val FoodBowl = listOf(
        "....OOOOOO....",
        "..OOOOOOOOOO..",
        ".OOOOOOOOOOOO.",
        "SSSSSSSSSSSSSS",
        "SCCCCCCCCCCCCS",
        "SCCCCCCCCCCCCS",
        ".SSSSSSSSSSSS."
    )

    val Book = listOf(
        "..GGGG..GGGG..",
        ".GGWWGGGGWWGG.",
        "GGWWWWGGWWWWGG",
        "GGWWWWGGWWWWGG",
        "GGWWWWGGWWWWGG",
        "GGWWWWGGWWWWGG",
        ".GGGGGGGGGGGG."
    )

    val Trophy = listOf(
        "YYYYYYYYYYYYYYY",
        "YY.YYYYYYYYY.YY",
        "YY.YYYYYYYYY.YY",
        ".Y.YYYYYYYYY.Y.",
        "...YYYYYYYYY...",
        "....YYYYYYY....",
        ".....YYYYY.....",
        "......YYY......",
        ".....YYYYY.....",
        "...YYYYYYYYY..."
    )

    val AIHead = listOf(
        "....MM....MM....",
        "...MMMM..MMMM...",
        "..MMMMMMMMMMMM..",
        ".MMMMMMMMMMMMMM.",
        ".MMMWeMMMMWeMMM.",
        ".MMMWWWMMMWWWMM.",
        "..MMMMMMMMMMMM..",
        "...MMMMppMMMM...",
        "....MMMMMMMM....",
        ".....MMMMMM....."
    )
}

/**
 * Standard Cozy Soft Pixel Art Colors (Rich Torbie/Calico palette & Pastel accents)
 */
val PixelColorMap = mapOf(
    // Torbie/Calico Cat Fur Colors (matching user's cat)
    'W' to Color(0xFFFFFFFF), // Pure White (Chest, Blaze, Muzzle, Paws)
    'w' to Color(0xFFFFF6EE), // Soft Warm Cream (White fur highlight / soft tone)
    'q' to Color(0xFFE8DBD9), // Soft White Shadow
    'O' to Color(0xFFE68A48), // Warm Ginger / Orange Tabby fur
    'o' to Color(0xFFC46828), // Deep Amber / Ginger Tabby Stripe
    'g' to Color(0xFFF7AB6D), // Light Peach / Ginger Highlight
    'B' to Color(0xFF382E2B), // Dark Brown / Black Tabby Stripe
    'b' to Color(0xFF221A18), // Deep Charcoal / Black Outline & Details
    'd' to Color(0xFF5D4A44), // Muted Dark Tabby Fur
    'E' to Color(0xFF869E4A), // Soft Olive Green Eye Iris
    'e' to Color(0xFF1E280C), // Dark Pupil
    'N' to Color(0xFFFFB0BA), // Cute Soft Pink Nose & Inner Ear
    'n' to Color(0xFFE27B88), // Deep Rose Inner Ear Shadow
    'P' to Color(0xFFFFD1DC), // Soft Pastel Blush / Paw Pad
    'H' to Color(0xFFFFFFFF), // Glint / Sparkle
    'p' to Color(0xFFFFD1DC), // Peach Blush
    'G' to Color(0xFFD0E1F9), // Soft Sky Blue
    'z' to Color(0xFF8B3A4A), // Muted Wine
    'R' to Color(0xFFD64A62), // Cozy Soft Red / Heart
    'r' to Color(0xFFFFA6B6), // Light Pink Heart Highlight
    'Y' to Color(0xFFFFB703), // Gold Sun / Star / Sparkle
    'y' to Color(0xFFFFD166), // Soft Warm Gold
    'C' to Color(0xFFD0E6F9), // Soft Sky / Cloud
    'c' to Color(0xFF90C2EB), // Deep Sky Blue
    'M' to Color(0xFF5D1E2A), // Mauve / Dark Marker
    'S' to Color(0xFFFDE8E9), // Cozy Bed Cushion Pink
    's' to Color(0xFFE8BAC0), // Bed Cushion Shadow
    'L' to Color(0xFFEAE2D8), // Cozy Blanket Cream
    '.' to Color.Transparent
)

/**
 * Animated Sitting Calico Cat Composable with Blinking Eyes, Tail Wag, and Tap Purr Interaction
 */
@Composable
fun PixelCatAnimated(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 2.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkingCalicoCat")
    
    // Smooth natural blink keyframes
    val blinkValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                0f at 0
                0f at 3100
                1f at 3250
                0f at 3400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blinkState"
    )

    // Subtle gentle tail flick motion
    val tailFlickValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 1800
                1f at 2200
                0f at 2600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "tailFlickState"
    )

    // Breathing / idle vertical motion
    val breathOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "catBreath"
    )

    val isBlinking = blinkValue > 0.5f
    val isTailFlicking = tailFlickValue > 0.5f

    val currentGrid = when {
        isBlinking -> PixelCatModels.BlinkingCalico
        isTailFlicking -> PixelCatModels.TailFlickCalico
        else -> PixelCatModels.SittingCalico
    }

    Box(
        modifier = modifier
            .offset(y = (breathOffset * 0.8f).dp),
        contentAlignment = Alignment.Center
    ) {
        PixelCanvas(
            pixelGrid = currentGrid,
            colorMap = PixelColorMap,
            pixelSize = pixelSize
        )
    }
}

/**
 * Animated Sleeping Calico Cat nestled in a cozy pastel bed with gentle breathing
 */
@Composable
fun PixelCatSleeping(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 2.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SleepingCalicoCat")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sleepingBreath"
    )

    Box(modifier = modifier.offset(y = yOffset.dp), contentAlignment = Alignment.Center) {
        PixelCanvas(
            pixelGrid = PixelCatModels.SleepingCalicoBed,
            colorMap = PixelColorMap,
            pixelSize = pixelSize
        )
    }
}

/**
 * Cozily styled Pixel Art Cards with slight elevation, tactile highlight, and a cute pixel emblem.
 */
@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    emblemType: String? = null, // "heart", "star", "paw", "trophy", "book", "food"
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // Outer card content
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }

        // Emblem in top right corner
        if (emblemType != null) {
            val grid = when (emblemType) {
                "heart" -> PixelCatModels.Heart
                "star" -> PixelCatModels.Star
                "paw" -> PixelCatModels.Paw
                "trophy" -> PixelCatModels.Trophy
                "book" -> PixelCatModels.Book
                "food" -> PixelCatModels.FoodBowl
                else -> null
            }
            if (grid != null) {
                PixelCanvas(
                    pixelGrid = grid,
                    colorMap = PixelColorMap,
                    pixelSize = 1.2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }
        }
    }
}

/**
 * A highly responsive Tactile Button that responds with springy scale & downwards translation on press.
 */
@Composable
fun TactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = DeepBurgundy,
    contentColor: Color = Cream,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ButtonScale"
    )

    val yOffset by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ButtonYOffset"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .scale(scale)
            .offset(y = yOffset),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * A custom progress bar styled with a playful, tactile pixel look.
 */
@Composable
fun PixelProgressBar(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    trackColor: Color = SoftGray,
    progressColor: Color = BaseBlushPink
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
    ) {
        // Track
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = trackColor
        ) {}

        // Progress Fill
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress),
            color = progressColor,
            shape = RoundedCornerShape(7.dp)
        ) {}
    }
}

/**
 * Renders stateful weather icons in a gorgeous, pixel-art style.
 */
@Composable
fun PixelWeatherIcon(
    weatherState: String, // "sunny", "cloudy", "rainy", "hot", "cold"
    modifier: Modifier = Modifier,
    pixelSize: Dp = 3.dp
) {
    val grid = when (weatherState.lowercase()) {
        "sunny", "hot" -> PixelCatModels.Sun
        "cloudy" -> PixelCatModels.Cloud
        "rainy" -> PixelCatModels.Rain
        else -> PixelCatModels.Cloud
    }

    PixelCanvas(
        pixelGrid = grid,
        colorMap = PixelColorMap,
        pixelSize = pixelSize,
        modifier = modifier
    )
}

/**
 * Renders custom pixel map markers.
 */
@Composable
fun PixelMapMarker(
    modifier: Modifier = Modifier,
    pixelSize: Dp = 3.dp
) {
    PixelCanvas(
        pixelGrid = PixelCatModels.MapMarker,
        colorMap = PixelColorMap,
        pixelSize = pixelSize,
        modifier = modifier
    )
}
