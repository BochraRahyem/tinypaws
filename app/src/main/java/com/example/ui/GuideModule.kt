package com.example.ui

import android.graphics.Bitmap
import com.example.R
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

// Helper Character Card Component
@Composable
fun StorybookCharacterBanner() {
    com.example.ui.PixelCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = BlushPink.copy(alpha = 0.2f),
        emblemType = "heart"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.PixelCatAnimated(pixelSize = 3.5.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.guide_banner_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FrauncesFontFamily
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.guide_banner_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 16.sp,
                        fontFamily = QuicksandFontFamily
                    )
                )
            }
        }
    }
}

@Composable
fun GuideModuleScreen(
    viewModel: TinyPawsViewModel,
    userName: String,
    onBack: () -> Unit,
    onNavigateToWeather: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // CurrentTab and selectedDiyProject are now managed by TinyPawsViewModel
    val currentTab by viewModel.activeGuideTab.collectAsStateWithLifecycle()
    val selectedDiyProject by viewModel.activeDiyProjectId.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    LaunchedEffect(currentTab) {
        if (currentTab != "menu") {
            viewModel.trackItemViewed("guide_$currentTab", sharedPrefs)
        }
    }

    LaunchedEffect(selectedDiyProject) {
        selectedDiyProject?.let {
            viewModel.trackItemViewed("diy_$it", sharedPrefs)
        }
    }

    val scrollState = rememberScrollState()

    // Handle back action gracefully at the top bar
    val handleBack = {
        if (selectedDiyProject != null) {
            viewModel.updateActiveDiyProject(null)
        } else if (currentTab != "menu") {
            viewModel.updateActiveGuideTab("menu")
        } else {
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (com.example.ui.theme.LocalIsDarkMode.current) com.example.ui.theme.OmbreGradientBrushDark else com.example.ui.theme.OmbreGradientBrushLight)
            .padding(horizontal = 16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { handleBack() },
                modifier = Modifier.testTag("back_to_dashboard")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_btn),
                    tint = DeepBurgundy
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.hi_user, userName),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = when {
                        selectedDiyProject != null -> stringResource(R.string.guide_screen_diy_storyboard)
                        currentTab == "diseases" -> stringResource(R.string.guide_diseases_title)
                        currentTab == "adoption" -> stringResource(R.string.guide_adoption_title)
                        currentTab == "diy" -> stringResource(R.string.guide_screen_diy)
                        currentTab == "stray" -> stringResource(R.string.guide_screen_stray)
                        currentTab == "favorites" -> stringResource(R.string.nav_favorites_title)
                        else -> stringResource(R.string.guide_title)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Sub-screen content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentTab == "menu") {
                StorybookCharacterBanner()

                Text(
                    text = stringResource(R.string.guide_welcome),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Static menu with clear buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GuideMenuCard(
                        title = stringResource(R.string.nav_favorites_title),
                        subtitle = stringResource(R.string.nav_favorites_subtitle),
                        tag = "menu_btn_favorites",
                        onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.updateActiveGuideTab("favorites") }
                    )
                    GuideMenuCard(
                        title = stringResource(R.string.guide_diseases_title),
                        subtitle = stringResource(R.string.guide_diseases_subtitle),
                        tag = "menu_btn_diseases",
                        onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.updateActiveGuideTab("diseases") }
                    )
                    GuideMenuCard(
                        title = stringResource(R.string.guide_adoption_title),
                        subtitle = stringResource(R.string.guide_adoption_subtitle),
                        tag = "menu_btn_adoption",
                        onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.updateActiveGuideTab("adoption") }
                    )
                    GuideMenuCard(
                        title = stringResource(R.string.guide_diy_title),
                        subtitle = stringResource(R.string.guide_diy_subtitle),
                        tag = "menu_btn_diy",
                        onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.updateActiveGuideTab("diy") }
                    )
                    GuideMenuCard(
                        title = stringResource(R.string.guide_stray_title),
                        subtitle = stringResource(R.string.guide_stray_subtitle),
                        tag = "menu_btn_stray",
                        onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.updateActiveGuideTab("stray") }
                    )
                }
            } else {
                // Secondary screens back buttons for easy navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { handleBack() },
                        colors = ButtonDefaults.textButtonColors(contentColor = PastelPurpleDark)
                    ) {
                        Text(stringResource(R.string.guide_back_menu), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                when (currentTab) {
                    "favorites" -> FavoritesSection(
                        viewModel = viewModel,
                        onSelectProject = { projId -> 
                            viewModel.updateActiveDiyProject(projId)
                            viewModel.updateActiveGuideTab("diy")
                        }
                    )
                    "diseases" -> DiseasesGuideSection()
                    "adoption" -> AdoptionGuideSection()
                    "diy" -> DiyProjectsSection(
                        viewModel = viewModel,
                        selectedProject = selectedDiyProject,
                        onSelectProject = { viewModel.updateActiveDiyProject(it) },
                        onNavigateToWeather = onNavigateToWeather
                    )
                    "stray" -> FoundStraySection()
                }
            }
        }
    }
}

@Composable
fun GuideMenuCard(
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    val emblem = remember(title) {
        when {
            title.contains("Diseases") || title.contains("Enfermedades") -> "paw"
            title.contains("Adoption") || title.contains("Adopción") -> "heart"
            title.contains("DIY") || title.contains("Proyectos") -> "star"
            title.contains("Cook") || title.contains("Cocinar") -> "food"
            title.contains("Stray") || title.contains("Callejero") -> "paw"
            else -> "paw"
        }
    }
    PixelCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { onClick() })
            .testTag(tag),
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        emblemType = emblem
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BlushPink.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        title.contains("Diseases") || title.contains("Enfermedades") -> "🦠"
                        title.contains("Adoption") || title.contains("Adopción") -> "🏡"
                        title.contains("DIY") || title.contains("Proyectos") -> "🛠️"
                        title.contains("Cook") || title.contains("Cocinar") -> "🍳"
                        title.contains("Stray") || title.contains("Callejero") -> "🐈"
                        title.contains("Favorite") || title.contains("Favoritos") -> "❤️"
                        else -> "🐾"
                    },
                    fontSize = 24.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
fun DiseasesGuideSection() {

    val diseases = listOf(
        DiseaseData(
            R.string.disease_rabies_name,
            R.string.disease_rabies_sub,
            R.string.disease_rabies_spread,
            R.string.disease_rabies_recog,
            R.string.disease_rabies_prev,
            stringResource(R.string.guide_badge_vaccine)
        ),
        DiseaseData(
            R.string.disease_toxo_name,
            R.string.disease_toxo_sub,
            R.string.disease_toxo_spread,
            R.string.disease_toxo_recog,
            R.string.disease_toxo_prev,
            stringResource(R.string.guide_badge_hygiene)
        ),
        DiseaseData(
            R.string.disease_csd_name,
            R.string.disease_csd_sub,
            R.string.disease_csd_spread,
            R.string.disease_csd_recog,
            R.string.disease_csd_prev,
            stringResource(R.string.guide_badge_flea)
        ),
        DiseaseData(
            R.string.disease_ringworm_name,
            R.string.disease_ringworm_sub,
            R.string.disease_ringworm_spread,
            R.string.disease_ringworm_recog,
            R.string.disease_ringworm_prev,
            stringResource(R.string.guide_badge_quarantine)
        ),
        DiseaseData(
            R.string.disease_pasteur_name,
            R.string.disease_pasteur_sub,
            R.string.disease_pasteur_spread,
            R.string.disease_pasteur_recog,
            R.string.disease_pasteur_prev,
            stringResource(R.string.guide_badge_emergency)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.diseases_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        Text(
            text = stringResource(R.string.diseases_desc),
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )

        var expandedDisease by remember { mutableStateOf<Int?>(null) }

        diseases.forEach { disease ->
            val isExpanded = expandedDisease == disease.nameResId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { expandedDisease = if (isExpanded) null else disease.nameResId },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) White else LightPurpleBg
                ),
                border = BorderStroke(1.dp, if (isExpanded) PastelPurplePrimary else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PastelPinkAccent.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(disease.badgeEmoji, fontSize = 14.sp)
                            }
                            Text(
                                text = stringResource(disease.nameResId),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPurpleDark
                                )
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.toggle),
                            tint = PastelPurpleDark
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(disease.subtitleResId),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDark, fontWeight = FontWeight.Medium)
                    )

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SoftGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        DiseaseDetailRow(stringResource(R.string.disease_spreads), stringResource(disease.spreadsResId))
                        Spacer(modifier = Modifier.height(8.dp))
                        DiseaseDetailRow(stringResource(R.string.disease_recognition), stringResource(disease.recognizeResId))
                        Spacer(modifier = Modifier.height(8.dp))
                        DiseaseDetailRow(stringResource(R.string.disease_prevention), stringResource(disease.preventionResId))
                    }
                }
            }
        }
    }
}

@Composable
fun DiseaseDetailRow(title: String, desc: String) {

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PastelPurpleDark)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
        )
    }
}

@Composable
fun FavoritesSection(
    viewModel: TinyPawsViewModel,
    onSelectProject: (String) -> Unit
) {
    val favoriteIds by viewModel.favoriteDiyIds.collectAsStateWithLifecycle()
    val allProjects = DiyProjectsData.projects
    val favoritedProjects = allProjects.filter { favoriteIds.contains(it.id) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.favorites_diy_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )

        if (favoritedProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💔", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.favorites_empty),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            favoritedProjects.forEach { project ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectProject(project.id) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PastelPinkAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("❤️", fontSize = 24.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(project.titleRes),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PastelPurpleDark
                                )
                            )
                            Text(
                                text = stringResource(project.categoryRes),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                        IconButton(
                            onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.toggleFavoriteDiy(project.id) },
                            modifier = Modifier.testTag("fav_remove_${project.id}")
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.guide_fav_remove), tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdoptionGuideSection() {

    val timelineStages = listOf(
        AdoptionStepData(
            R.string.adopt_stage_1_name,
            R.string.adopt_stage_1_sum,
            R.array.adopt_stage_1_bullets,
            "🐈"
        ),
        AdoptionStepData(
            R.string.adopt_stage_2_name,
            R.string.adopt_stage_2_sum,
            R.array.adopt_stage_2_bullets,
            "🏥"
        ),
        AdoptionStepData(
            R.string.adopt_stage_3_name,
            R.string.adopt_stage_3_sum,
            R.array.adopt_stage_3_bullets,
            "🛌"
        ),
        AdoptionStepData(
            R.string.adopt_stage_4_name,
            R.string.adopt_stage_4_sum,
            R.array.adopt_stage_4_bullets,
            "🧸"
        ),
        AdoptionStepData(
            R.string.adopt_stage_5_name,
            R.string.adopt_stage_5_sum,
            R.array.adopt_stage_5_bullets,
            "🥰"
        ),
        AdoptionStepData(
            R.string.adopt_stage_6_name,
            R.string.adopt_stage_6_sum,
            R.array.adopt_stage_6_bullets,
            "🎗️"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.adoption_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        Text(
            text = stringResource(R.string.adoption_desc),
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )

        timelineStages.forEachIndexed { idx, stage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(stage.titleResId),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PastelPurpleDark
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(PastelPinkAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stage.icon, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(stage.summaryResId),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = SoftGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    val bullets = stringArrayResource(stage.bulletsResId)
                    bullets.forEach { bullet ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🐾", fontSize = 11.sp, modifier = Modifier.padding(top = 1.dp))
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDark, lineHeight = 14.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisualNarrativePlaceholder(
    viewModel: TinyPawsViewModel,
    stepIndex: Int,
    altTextResId: Int, // Changed to ResId
    stepDetailResId: Int, // Changed to ResId
    modifier: Modifier = Modifier
) {

    val stepImageCache by viewModel.stepImageCache.collectAsStateWithLifecycle()
    val stepId = "diy_step_$stepIndex"
    val altText = stringResource(altTextResId)
    val stepDetail = stringResource(stepDetailResId)

    LaunchedEffect(stepIndex) {
        viewModel.generateStepImage(stepId, altText)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.5.dp, PastelPinkAccent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.diy_storyboard_step, stepIndex + 1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PastelPurpleDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            SideBySideVisualRow(
                imageFileName = "diy_step${stepIndex + 1}.jpg",
                altText = altText,
                imageBitmap = stepImageCache[stepId]
            ) {
                Text(
                    text = stepDetail,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun LilyAndPipIllustration(
    category: String,
    stepIndex: Int,
    modifier: Modifier = Modifier
) {

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                when (category) {
                    "shelter" -> Color(0xFFE8F5E9) // Soft Green
                    "game" -> Color(0xFFFFF3E0) // Soft Orange/Peach
                    else -> Color(0xFFECEFF1) // Soft Cozy Gray-Blue
                }
            )
            .padding(12.dp)
    ) {
        val w = size.width
        val h = size.height

        // Grid Background for design/blueprint look
        val gridSpacing = 40f
        for (x in 0..w.toInt() step gridSpacing.toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), h),
                strokeWidth = 1f
            )
        }
        for (y in 0..h.toInt() step gridSpacing.toInt()) {
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(0f, y.toFloat()),
                end = Offset(w, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Worktable Surface Line
        drawLine(
            color = Color(0xFFD7CCC8), // Wooden beige desk line
            start = Offset(20f, h * 0.82f),
            end = Offset(w - 20f, h * 0.82f),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )

        // DRAW LILY (The Girl) on the left (center X = w * 0.28f, Y = h * 0.52f)
        val lx = w * 0.28f
        val ly = h * 0.52f

        // Overalls / Dress body
        val bodyPath = Path().apply {
            moveTo(lx - 25f, ly + 65f)
            lineTo(lx + 25f, ly + 65f)
            lineTo(lx + 15f, ly + 15f)
            lineTo(lx - 15f, ly + 15f)
            close()
        }
        drawPath(
            path = bodyPath,
            color = Color(0xFF6A1B9A) // Regal Purple overalls
        )

        // Head
        drawCircle(
            color = Color(0xFFFFD1A9), // Soft peach skin
            radius = 26f,
            center = Offset(lx, ly - 15f)
        )

        // Hair (Light Brown, Bun hairstyle)
        // Back hair sweep
        drawArc(
            color = Color(0xFF5D4037),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(64f, 64f),
            topLeft = Offset(lx - 32f, ly - 45f)
        )
        // Two hair buns
        drawCircle(
            color = Color(0xFF5D4037),
            radius = 12f,
            center = Offset(lx - 30f, ly - 35f)
        )
        drawCircle(
            color = Color(0xFF5D4037),
            radius = 12f,
            center = Offset(lx + 30f, ly - 35f)
        )

        // Face details
        drawCircle(color = Color(0xFF212121), radius = 3f, center = Offset(lx - 8f, ly - 15f)) // Eye L
        drawCircle(color = Color(0xFF212121), radius = 3f, center = Offset(lx + 8f, ly - 15f)) // Eye R
        // Cheek Blush
        drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.7f), radius = 5f, center = Offset(lx - 14f, ly - 8f))
        drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.7f), radius = 5f, center = Offset(lx + 14f, ly - 8f))
        // Mouth Smile
        drawArc(
            color = Color(0xFFD32F2F),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            size = Size(10f, 8f),
            topLeft = Offset(lx - 5f, ly - 10f),
            style = Stroke(width = 2.5f)
        )

        // Arms reaching towards center
        drawLine(
            color = Color(0xFFFFD1A9),
            start = Offset(lx + 10f, ly + 25f),
            end = Offset(w * 0.45f, ly + 35f + (stepIndex * 5f)),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // DRAW PIP (The Orange Cat) on the right (center X = w * 0.74f, Y = h * 0.65f)
        val px = w * 0.74f
        val py = h * 0.65f

        // Body
        drawCircle(
            color = Color(0xFFFF9800), // Orange
            radius = 24f,
            center = Offset(px, py + 15f)
        )
        // Belly patch
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 12f,
            center = Offset(px - 5f, py + 18f)
        )
        // Head
        drawCircle(
            color = Color(0xFFFF9800),
            radius = 16f,
            center = Offset(px - 10f, py - 10f)
        )
        // Triangular ears
        val leftEar = Path().apply {
            moveTo(px - 22f, py - 22f)
            lineTo(px - 14f, py - 24f)
            lineTo(px - 18f, py - 10f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(px - 6f, py - 24f)
            lineTo(px + 2f, py - 20f)
            lineTo(px - 2f, py - 10f)
            close()
        }
        drawPath(path = leftEar, color = Color(0xFFFF9800))
        drawPath(path = rightEar, color = Color(0xFFFF9800))
        // Inner ears pink
        drawCircle(color = Color(0xFFFFCDD2), radius = 3f, center = Offset(px - 16f, py - 18f))
        drawCircle(color = Color(0xFFFFCDD2), radius = 3f, center = Offset(px - 3f, py - 17f))

        // Eyes (happy curved/sleeping style)
        if (category == "cozy" && stepIndex == 2) {
            drawArc(
                color = Color(0xFF3E2723),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                size = Size(6f, 4f),
                topLeft = Offset(px - 19f, py - 13f),
                style = Stroke(width = 2f)
            )
            drawArc(
                color = Color(0xFF3E2723),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                size = Size(6f, 4f),
                topLeft = Offset(px - 9f, py - 13f),
                style = Stroke(width = 2f)
            )
        } else {
            drawCircle(color = Color(0xFF3E2723), radius = 2.5f, center = Offset(px - 16f, py - 12f))
            drawCircle(color = Color(0xFF3E2723), radius = 2.5f, center = Offset(px - 8f, py - 12f))
        }
        // Nose & Whiskers
        drawCircle(color = Color(0xFFFF8A80), radius = 2f, center = Offset(px - 12f, py - 8f))
        drawLine(color = Color.White, start = Offset(px - 22f, py - 8f), end = Offset(px - 15f, py - 8f), strokeWidth = 1.5f)
        drawLine(color = Color.White, start = Offset(px - 7f, py - 8f), end = Offset(px - 1f, py - 8f), strokeWidth = 1.5f)

        // Tail
        val tailPath = Path().apply {
            moveTo(px + 20f, py + 10f)
            quadraticTo(px + 35f, py - 5f, px + 30f, py - 25f)
        }
        drawPath(
            path = tailPath,
            color = Color(0xFFFF9800),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // ACTIVE CRAFT PROJECT (Center X = w * 0.52f, Y = h * 0.65f)
        val ox = w * 0.52f
        val oy = h * 0.65f

        when (category) {
            "shelter" -> {
                if (stepIndex == 0) {
                    // Start stage: flat cardboard brown sheets & ruler
                    drawRoundRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(ox - 30f, oy + 10f),
                        size = Size(60f, 24f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRect(
                        color = Color(0xFFFFEB3B),
                        topLeft = Offset(ox - 20f, oy + 2f),
                        size = Size(40f, 6f)
                    )
                } else if (stepIndex == 1) {
                    // Middle stage: box being assembled with tape lines
                    drawRoundRect(
                        color = Color(0xFFB0DFDB),
                        topLeft = Offset(ox - 35f, oy - 15f),
                        size = Size(70f, 50f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawCircle(
                        color = Color(0xFF37474F),
                        radius = 12f,
                        center = Offset(ox - 10f, oy + 10f)
                    )
                    drawLine(
                        color = Color(0xFF78909C), // Tape
                        start = Offset(ox - 35f, oy - 10f),
                        end = Offset(ox + 35f, oy - 10f),
                        strokeWidth = 6f
                    )
                } else {
                    // Completed stage: cozy green insulated house, straw, and Pip
                    drawRoundRect(
                        color = Color(0xFF4DB6AC),
                        topLeft = Offset(ox - 40f, oy - 25f),
                        size = Size(80f, 60f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F), // Straw
                        radius = 16f,
                        center = Offset(ox, oy + 30f)
                    )
                    val roofPath = Path().apply {
                        moveTo(ox - 48f, oy - 23f)
                        lineTo(ox, oy - 42f)
                        lineTo(ox + 48f, oy - 23f)
                        close()
                    }
                    drawPath(path = roofPath, color = Color(0xFF00695C))
                    drawCircle(
                        color = Color(0xFF1F2937),
                        radius = 14f,
                        center = Offset(ox, oy + 12f)
                    )
                }
            }
            "game" -> {
                if (stepIndex == 0) {
                    // Start stage: stick, feathers
                    drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(ox - 30f, oy + 25f),
                        end = Offset(ox + 30f, oy + 5f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color(0xFFEC407A), radius = 8f, center = Offset(ox + 5f, oy + 22f))
                    drawCircle(color = Color(0xFFAB47BC), radius = 6f, center = Offset(ox - 15f, oy + 15f))
                } else if (stepIndex == 1) {
                    // Middle stage: feathers tied to wand
                    drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(ox - 35f, oy + 30f),
                        end = Offset(ox + 15f, oy - 10f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = Offset(ox + 15f, oy - 10f),
                        style = Stroke(width = 2f)
                    )
                    drawCircle(color = Color(0xFFEC407A), radius = 10f, center = Offset(ox + 22f, oy - 18f))
                } else {
                    // Completed stage: feather teaser with hanging string & bells
                    drawLine(
                        color = Color(0xFF8D6E63),
                        start = Offset(ox - 40f, oy + 35f),
                        end = Offset(ox + 10f, oy - 15f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    val hangingString = Path().apply {
                        moveTo(ox + 10f, oy - 15f)
                        quadraticTo(ox + 25f, oy, ox + 15f, oy + 20f)
                    }
                    drawPath(
                        path = hangingString,
                        color = Color.White,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(color = Color(0xFFFFEB3B), radius = 5f, center = Offset(ox + 15f, oy + 20f))
                    drawCircle(color = Color(0xFFEC407A), radius = 12f, center = Offset(ox + 15f, oy + 28f))
                    drawCircle(color = Color(0xFF26C6DA), radius = 9f, center = Offset(ox + 24f, oy + 24f))
                }
            }
            else -> { // cozy
                if (stepIndex == 0) {
                    // Start stage: hangers, base board
                    drawLine(
                        color = Color(0xFF78909C),
                        start = Offset(ox - 30f, oy + 10f),
                        end = Offset(ox + 30f, oy + 10f),
                        strokeWidth = 3f
                    )
                    drawRoundRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(ox - 25f, oy + 18f),
                        size = Size(50f, 12f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                } else if (stepIndex == 1) {
                    // Middle stage: bent arches on cardboard
                    val arch1 = Path().apply {
                        moveTo(ox - 30f, oy + 20f)
                        quadraticTo(ox, oy - 20f, ox + 30f, oy + 20f)
                    }
                    drawPath(
                        path = arch1,
                        color = Color(0xFF78909C),
                        style = Stroke(width = 3f)
                    )
                    drawRoundRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(ox - 35f, oy + 20f),
                        size = Size(70f, 10f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                } else {
                    // Completed stage: cozy fabric cave tent
                    drawCircle(
                        color = Color(0xFF42A5F5),
                        radius = 32f,
                        center = Offset(ox, oy)
                    )
                    drawRoundRect(
                        color = Color(0xFF1E88E5),
                        topLeft = Offset(ox - 35f, oy + 18f),
                        size = Size(70f, 12f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawCircle(
                        color = Color(0xFF0D47A1),
                        radius = 15f,
                        center = Offset(ox, oy + 5f)
                    )
                    drawCircle(
                        color = Color(0xFFF48FB1),
                        radius = 9f,
                        center = Offset(ox, oy + 14f)
                    )
                }
            }
        }

        // Sparkles and floating heart on completion
        if (stepIndex == 2) {
            drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(ox - 35f, oy - 35f))
            drawCircle(color = Color(0xFFFFD54F), radius = 5f, center = Offset(ox + 45f, oy - 20f))
            drawCircle(color = Color(0xFFFFD54F), radius = 3f, center = Offset(ox + 10f, oy - 48f))

            val hx = px - 8f
            val hy = py - 38f
            val heartPath = Path().apply {
                moveTo(hx, hy)
                cubicTo(hx - 8f, hy - 8f, hx - 16f, hy, hx, hy + 12f)
                cubicTo(hx + 16f, hy, hx + 8f, hy - 8f, hx, hy)
                close()
            }
            drawPath(path = heartPath, color = Color(0xFFEF5350))
        }
    }
}

@Composable
fun DiyProjectsSection(
    viewModel: TinyPawsViewModel,
    selectedProject: String?,
    onSelectProject: (String?) -> Unit,
    onNavigateToWeather: () -> Unit = {}
) {
    NewDiyProjectsSection(viewModel, selectedProject, onSelectProject, onNavigateToWeather)
}

@Composable
fun NewDiyProjectsSection(
    viewModel: TinyPawsViewModel,
    selectedProject: String?,
    onSelectProject: (String?) -> Unit,
    onNavigateToWeather: () -> Unit = {}
) {
    var activeCategory by remember { mutableStateOf("shelter") }
    val allProjects = DiyProjectsData.projects
    val stepImageCache by viewModel.stepImageCache.collectAsStateWithLifecycle()

    if (selectedProject == null) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.guide_diy_header),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )

            // Heatwave & Extreme Weather Alert Banner Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWeather() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = BorderStroke(1.dp, Color(0xFFFF8A80))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔥", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.diy_weather_tracker_title),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                            )
                            Text(
                                text = stringResource(R.string.diy_weather_tracker_desc),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D4037))
                            )
                        }
                    }
                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onNavigateToWeather() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(R.string.main_open), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            
            // Actual, High-Quality Concept Image for the DIY category
            PixelCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                emblemType = "paw"
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_diy_concept_fixed),
                    contentDescription = stringResource(R.string.guide_diy_header_alt),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Text(
                text = stringResource(R.string.guide_diy_header_desc),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "shelter" to stringResource(R.string.guide_diy_tab_shelter),
                    "game" to stringResource(R.string.guide_diy_tab_game),
                    "cozy" to stringResource(R.string.guide_diy_tab_cozy)
                ).forEach { (catId, catLabel) ->
                    val isSelected = activeCategory == catId
                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick {  activeCategory = catId },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) DeepBurgundy else White.copy(alpha = 0.5f),
                            contentColor = if (isSelected) White else DeepBurgundy
                        ),
                        border = BorderStroke(1.2.dp, DeepBurgundy),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("diy_tab_$catId")
                    ) {
                        Text(catLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            val filteredProjects = allProjects.filter { project ->
                when (activeCategory) {
                    "shelter" -> project.categoryRes == R.string.cat_shelter
                    "game" -> project.categoryRes == R.string.cat_game
                    "cozy" -> project.categoryRes == R.string.cat_cozy
                    else -> true
                }
            }

            filteredProjects.forEach { project ->
                val favoriteIds by viewModel.favoriteDiyIds.collectAsStateWithLifecycle()
                val isFavorite = favoriteIds.contains(project.id)

                PixelCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    emblemType = "star"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val categoryLabel = when(project.categoryRes) {
                                    R.string.diy_cat_shelter -> stringResource(R.string.guide_diy_tab_shelter)
                                    R.string.diy_cat_game -> stringResource(R.string.guide_diy_tab_game)
                                    else -> stringResource(R.string.guide_diy_tab_cozy)
                                }
                                Text(
                                    text = stringResource(project.titleRes),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                IconButton(
                                    onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.toggleFavoriteDiy(project.id) },
                                    modifier = Modifier.size(24.dp).testTag("fav_btn_${project.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = stringResource(R.string.diy_fav_desc),
                                        tint = if (isFavorite) Color.Red else Wine.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                              DiyBadge(
                                text = stringResource(project.difficultyRes),
                                containerColor = BlushPink.copy(alpha = 0.4f),
                                contentColor = DeepBurgundy
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DiyBadge(text = stringResource(project.costRes), containerColor = BlushPink.copy(alpha = 0.3f), contentColor = DeepBurgundy)
                            DiyBadge(text = stringResource(project.timeRes), containerColor = SoftGray.copy(alpha = 0.5f), contentColor = Ink)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(project.descriptionRes),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectProject(project.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("start_diy_${project.id}")
                        ) {
                            Text(stringResource(R.string.diy_start_storyboard), fontSize = 11.sp, color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        val project = allProjects.firstOrNull { it.id == selectedProject }
        if (project == null) {
            onSelectProject(null)
        } else {
            var currentStepIndex by remember { mutableIntStateOf(0) }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectProject(null) },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepBurgundy),
                    modifier = Modifier.testTag("back_to_diy_list")
                ) {
                    Text(stringResource(R.string.diy_back_to_list), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                PixelCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    emblemType = "star"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
    val favoriteIds by viewModel.favoriteDiyIds.collectAsStateWithLifecycle()
                        val isFavorite = favoriteIds.contains(project.id)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(project.titleRes),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.toggleFavoriteDiy(project.id) },
                                modifier = Modifier.size(28.dp).testTag("fav_detail_btn_${project.id}")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = stringResource(R.string.diy_fav_desc),
                                    tint = if (isFavorite) Color.Red else Wine.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DiyBadge(text = stringResource(project.difficultyRes), containerColor = BlushPink.copy(alpha = 0.4f), contentColor = DeepBurgundy)
                            DiyBadge(text = stringResource(project.costRes), containerColor = BlushPink.copy(alpha = 0.3f), contentColor = DeepBurgundy)
                            DiyBadge(text = stringResource(project.timeRes), containerColor = SoftGray.copy(alpha = 0.5f), contentColor = Ink)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(project.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground, lineHeight = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SoftGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.diy_storyboard_guide),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.diy_storyboard_sub),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val steps = project.steps

                        steps.forEachIndexed { index, step ->
                            val actionId = step.actionId
                            val richPrompt = DiyProjectsData.getActionPrompt(actionId)

                            LaunchedEffect(actionId) {
                                viewModel.generateStepImage(actionId, richPrompt)
                            }

                            PixelCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                emblemType = "star"
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val bitmap = stepImageCache[actionId]
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = stringResource(step.titleRes),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val fallbackResId = when (actionId) {
                                            "gather_materials" -> R.drawable.diy_gather_step_1785341141776
                                            "measure", "plan_sketch" -> R.drawable.diy_measure_step_1785341113912
                                            "cut_opening", "cut_to_size", "sand_edges" -> R.drawable.diy_cut_step_1785341128336
                                            "insulate_line", "add_straw", "waterproof_cover" -> R.drawable.diy_insulate_step_1785341157448
                                            "build_frame", "attach_join", "secure_lid", "elevate_place" -> R.drawable.diy_assemble_step_1785341172903
                                            "sew_edge", "stuff_fill", "tie_knot", "fold_shape" -> R.drawable.img_diy_craft_sew_1785340343515
                                            "paint_decorate", "glue_pieces", "quality_check" -> R.drawable.diy_measure_step_1785341113912
                                            "test_play", "final_placement" -> R.drawable.img_diy_cozy_bed_1785340326644
                                            else -> R.drawable.diy_gather_step_1785341141776
                                        }
                                        Image(
                                            painter = painterResource(id = fallbackResId),
                                            contentDescription = stringResource(step.titleRes),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = stringResource(R.string.diy_step_count, step.step, steps.size),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(step.titleRes),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onBackground,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Overview Paragraph without visuals & View More Full Guide
                        PixelCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            emblemType = "book"
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.diy_proj_overview),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(
                                        R.string.diy_overview,
                                        stringResource(project.titleRes),
                                        stringResource(project.difficultyRes).lowercase(),
                                        stringResource(project.costRes),
                                        stringResource(project.timeRes)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                var isExpanded by remember { mutableStateOf(false) }

                                Button(
                                    onClick = com.example.ui.theme.rememberHapticOnClick { isExpanded = !isExpanded },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (isExpanded) stringResource(R.string.diy_hide_guide) else stringResource(R.string.diy_view_more),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = stringResource(R.string.diy_full_guide_title),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = stringResource(project.guideParagraphRes),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 21.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoundStraySection() {

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.stray_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        Text(
            text = stringResource(R.string.stray_desc),
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )

        // What NOT to feed card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, RedError)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠️", fontSize = 24.sp)
                    Text(
                        text = stringResource(R.string.stray_critical_title),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.stray_critical_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val forbiddenFoods = listOf(
                    stringResource(R.string.stray_toxic_1_name) to stringResource(R.string.stray_toxic_1_desc),
                    stringResource(R.string.stray_toxic_2_name) to stringResource(R.string.stray_toxic_2_desc),
                    stringResource(R.string.stray_toxic_3_name) to stringResource(R.string.stray_toxic_3_desc),
                    stringResource(R.string.stray_toxic_4_name) to stringResource(R.string.stray_toxic_4_desc),
                    stringResource(R.string.stray_toxic_5_name) to stringResource(R.string.stray_toxic_5_desc),
                    stringResource(R.string.stray_toxic_6_name) to stringResource(R.string.stray_toxic_6_desc)
                )

                forbiddenFoods.forEach { (food, hazard) ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("❌", fontSize = 12.sp)
                        Column {
                            Text(
                                text = food,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            )
                            Text(
                                text = hazard,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDark, fontSize = 11.sp, lineHeight = 13.sp)
                            )
                        }
                    }
                }
            }
        }

        // Safe Engagement Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, GreenSuccess)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🛡️", fontSize = 24.sp)
                    Text(
                        text = stringResource(R.string.stray_protocol_title),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                val safeSteps = listOf(
                    stringResource(R.string.stray_protocol_step_1_name) to stringResource(R.string.stray_protocol_step_1_desc),
                    stringResource(R.string.stray_protocol_step_2_name) to stringResource(R.string.stray_protocol_step_2_desc),
                    stringResource(R.string.stray_protocol_step_3_name) to stringResource(R.string.stray_protocol_step_3_desc),
                    stringResource(R.string.stray_protocol_step_4_name) to stringResource(R.string.stray_protocol_step_4_desc),
                    stringResource(R.string.stray_protocol_step_5_name) to stringResource(R.string.stray_protocol_step_5_desc)
                )

                safeSteps.forEachIndexed { idx, (title, desc) ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(GreenSuccess.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            )
                        }
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextDark)
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom Badge for project stats
@Composable
fun DiyBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {

    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        )
    }
}

// Data models
data class DiseaseData(
    val nameResId: Int,
    val subtitleResId: Int,
    val spreadsResId: Int,
    val recognizeResId: Int,
    val preventionResId: Int,
    val badgeEmoji: String
)

data class AdoptionStepData(
    val titleResId: Int,
    val summaryResId: Int,
    val bulletsResId: Int, // string-array
    val icon: String
)

data class CuratedRecipeData(
    val titleRes: Int,
    val difficultyRes: Int,
    val timeRes: Int,
    val materialsRes: Int, // Refers to a string-array
    val descriptionRes: Int,
    val stepsRes: Int, // Refers to a string-array
    val altTextsRes: Int // Refers to a string-array
)

fun generateLocalRecipe(selected: Set<String>): CuratedRecipeData {
    // Selection contains stable language-independent KEYS ("fish", "chicken", …)
    // so recipe matching works identically in en/es/fr/ar.
    val hasFish = selected.any { it.equals("fish", ignoreCase = true) }
    val hasChicken = selected.any { it.equals("chicken", ignoreCase = true) }
    val hasPumpkin = selected.any { it.equals("pumpkin", ignoreCase = true) }
    val hasCarrot = selected.any { it.equals("carrot", ignoreCase = true) }
    val hasEgg = selected.any { it.equals("egg", ignoreCase = true) }
    val hasPotato = selected.any { it.equals("potato", ignoreCase = true) }
    val hasRice = selected.any { it.equals("rice", ignoreCase = true) }
    val hasBroccoli = selected.any { it.equals("broccoli", ignoreCase = true) }

    return when {
        hasFish && hasCarrot -> CuratedRecipeData(
            titleRes = R.string.recipe_1_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_15m,
            materialsRes = R.array.recipe_1_materials,
            descriptionRes = R.string.recipe_1_desc,
            stepsRes = R.array.recipe_1_steps,
            altTextsRes = R.array.recipe_1_alt
        )
        hasChicken && hasPumpkin -> CuratedRecipeData(
            titleRes = R.string.recipe_2_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_20m,
            materialsRes = R.array.recipe_2_materials,
            descriptionRes = R.string.recipe_2_desc,
            stepsRes = R.array.recipe_2_steps,
            altTextsRes = R.array.recipe_2_alt
        )
        hasChicken && hasRice && hasCarrot -> CuratedRecipeData(
            titleRes = R.string.recipe_3_title,
            difficultyRes = R.string.difficulty_medium,
            timeRes = R.string.time_25m,
            materialsRes = R.array.recipe_3_materials,
            descriptionRes = R.string.recipe_3_desc,
            stepsRes = R.array.recipe_3_steps,
            altTextsRes = R.array.recipe_3_alt
        )
        hasEgg && hasPotato -> CuratedRecipeData(
            titleRes = R.string.recipe_4_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_12m,
            materialsRes = R.array.recipe_4_materials,
            descriptionRes = R.string.recipe_4_desc,
            stepsRes = R.array.recipe_4_steps,
            altTextsRes = R.array.recipe_4_alt
        )
        hasFish -> CuratedRecipeData(
            titleRes = R.string.recipe_5_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_10m,
            materialsRes = R.array.recipe_5_materials,
            descriptionRes = R.string.recipe_5_desc,
            stepsRes = R.array.recipe_5_steps,
            altTextsRes = R.array.recipe_5_alt
        )
        hasChicken -> CuratedRecipeData(
            titleRes = R.string.recipe_6_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_15m,
            materialsRes = R.array.recipe_6_materials,
            descriptionRes = R.string.recipe_6_desc,
            stepsRes = R.array.recipe_6_steps,
            altTextsRes = R.array.recipe_6_alt
        )
        else -> CuratedRecipeData(
            titleRes = R.string.recipe_7_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_18m,
            materialsRes = R.array.recipe_7_materials,
            descriptionRes = R.string.recipe_7_desc,
            stepsRes = R.array.recipe_7_steps,
            altTextsRes = R.array.recipe_7_alt
        )
    }
}




@Composable
fun IngredientPickerSubSection(viewModel: TinyPawsViewModel) {

    val ingredients = listOf(
        Triple("fish", stringResource(R.string.mat_fish), stringResource(R.string.cat_protein)),
        Triple("chicken", stringResource(R.string.mat_chicken), stringResource(R.string.cat_protein)),
        Triple("pumpkin", stringResource(R.string.mat_pumpkin), stringResource(R.string.cat_fiber)),
        Triple("carrot", stringResource(R.string.mat_carrot), stringResource(R.string.cat_vitamins)),
        Triple("potato", stringResource(R.string.mat_potato), stringResource(R.string.cat_carbs)),
        Triple("egg", stringResource(R.string.mat_egg), stringResource(R.string.cat_fats)),
        Triple("rice", stringResource(R.string.mat_rice), stringResource(R.string.cat_digestive)),
        Triple("broccoli", stringResource(R.string.mat_broccoli), stringResource(R.string.cat_vitamins))
    )

    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.2.dp, PastelPurplePrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.guide_cook_picker_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PastelPurpleDark)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.guide_cook_picker_desc),
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ingredients.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (key, name, category) ->
                            val isSelected = selectedIngredients.contains(key)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedIngredients = if (isSelected) {
                                            selectedIngredients - key
                                        } else {
                                            selectedIngredients + key
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) LightPurpleBg else SoftGray.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (isSelected) PastelPurplePrimary else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                        Text(text = category, fontSize = 10.sp, color = TextMuted)
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = PastelPurplePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SoftGray)
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedIngredients.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.cook_select_2_ing),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelPinkDark,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val generatedRecipe = remember(selectedIngredients) {
                    generateLocalRecipe(selectedIngredients)
                }

                Text(
                    text = stringResource(R.string.cook_lilys_creation) + stringResource(generatedRecipe.titleRes),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PastelPurpleDark),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(generatedRecipe.descriptionRes),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                RecipeStoryboardCard(recipe = generatedRecipe, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RecipeLibrarySubSection(viewModel: TinyPawsViewModel) {

    val curatedRecipes = listOf(
        CuratedRecipeData(
            titleRes = R.string.recipe_1_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_10m,
            materialsRes = R.array.recipe_1_materials,
            descriptionRes = R.string.recipe_1_desc,
            stepsRes = R.array.recipe_1_steps,
            altTextsRes = R.array.recipe_1_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_2_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_20m,
            materialsRes = R.array.recipe_2_materials,
            descriptionRes = R.string.recipe_2_desc,
            stepsRes = R.array.recipe_2_steps,
            altTextsRes = R.array.recipe_2_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_3_title,
            difficultyRes = R.string.difficulty_medium,
            timeRes = R.string.time_15m,
            materialsRes = R.array.recipe_3_materials,
            descriptionRes = R.string.recipe_3_desc,
            stepsRes = R.array.recipe_3_steps,
            altTextsRes = R.array.recipe_3_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_4_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_12m,
            materialsRes = R.array.recipe_4_materials,
            descriptionRes = R.string.recipe_4_desc,
            stepsRes = R.array.recipe_4_steps,
            altTextsRes = R.array.recipe_4_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_5_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_10m,
            materialsRes = R.array.recipe_5_materials,
            descriptionRes = R.string.recipe_5_desc,
            stepsRes = R.array.recipe_5_steps,
            altTextsRes = R.array.recipe_5_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_6_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_15m,
            materialsRes = R.array.recipe_6_materials,
            descriptionRes = R.string.recipe_6_desc,
            stepsRes = R.array.recipe_6_steps,
            altTextsRes = R.array.recipe_6_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_7_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_20m,
            materialsRes = R.array.recipe_7_materials,
            descriptionRes = R.string.recipe_7_desc,
            stepsRes = R.array.recipe_7_steps,
            altTextsRes = R.array.recipe_7_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_8_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_15m,
            materialsRes = R.array.recipe_8_materials,
            descriptionRes = R.string.recipe_8_desc,
            stepsRes = R.array.recipe_8_steps,
            altTextsRes = R.array.recipe_8_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_9_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_10m,
            materialsRes = R.array.recipe_9_materials,
            descriptionRes = R.string.recipe_9_desc,
            stepsRes = R.array.recipe_9_steps,
            altTextsRes = R.array.recipe_9_alt
        ),
        CuratedRecipeData(
            titleRes = R.string.recipe_10_title,
            difficultyRes = R.string.difficulty_easy,
            timeRes = R.string.time_10m,
            materialsRes = R.array.recipe_10_materials,
            descriptionRes = R.string.recipe_10_desc,
            stepsRes = R.array.recipe_10_steps,
            altTextsRes = R.array.recipe_10_alt
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        curatedRecipes.forEach { recipe ->
            var expanded by remember { mutableStateOf(false) }
            val title = stringResource(recipe.titleRes)
            val difficulty = stringResource(recipe.difficultyRes)
            val time = stringResource(recipe.timeRes)
            val description = stringResource(recipe.descriptionRes)
            val materials = stringArrayResource(recipe.materialsRes)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PastelPurpleDark
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isEasy = difficulty == stringResource(R.string.difficulty_easy)
                            DiyBadge(
                                text = difficulty,
                                containerColor = if (isEasy) GreenSuccess.copy(alpha = 0.15f) else OrangeWarning.copy(alpha = 0.15f),
                                contentColor = if (isEasy) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                            )
                            DiyBadge(
                                text = time,
                                containerColor = SoftGray,
                                contentColor = TextDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
                    )

                    if (expanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SoftGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.guide_cook_ingredients_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PastelPurpleDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            materials.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .background(LightPurpleBg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = item, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.guide_cook_visual_guide),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PastelPurpleDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        RecipeStoryboardCard(viewModel = viewModel, recipe = recipe)
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.guide_cook_tap_open),
                            fontSize = 10.sp,
                            color = PastelPinkDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeStoryboardCard(viewModel: TinyPawsViewModel, recipe: CuratedRecipeData) {

    var stepIdx by remember { mutableStateOf(0) }
    val stepImageCache by viewModel.stepImageCache.collectAsStateWithLifecycle()
    val steps = stringArrayResource(recipe.stepsRes)
    val altTexts = stringArrayResource(recipe.altTextsRes)
    val recipeTitle = stringResource(recipe.titleRes)
    val stepId = "cook_${recipeTitle}_$stepIdx"

    LaunchedEffect(stepIdx) {
        viewModel.generateStepImage(stepId, altTexts[stepIdx])
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurpleBg.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.guide_cook_prep_step, stepIdx + 1, steps.size),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PastelPurpleDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SideBySideVisualRow(
                imageFileName = "cook_step${stepIdx + 1}.jpg",
                altText = altTexts[stepIdx],
                imageBitmap = stepImageCache[stepId]
            ) {
                Text(
                    text = steps[stepIdx],
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick {  if (stepIdx > 0) stepIdx-- },
                    enabled = stepIdx > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(R.string.main_prev), fontSize = 10.sp, color = White)
                }

                Text(
                    text = stringResource(R.string.cook_step, "${stepIdx + 1} / 10"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick {  if (stepIdx < 9) stepIdx++ },
                    enabled = stepIdx < 9,
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(R.string.guide_cook_next), fontSize = 10.sp, color = White)
                }
            }
        }
    }
}

@Composable
fun CookModuleScreen(
    viewModel: TinyPawsViewModel,
    userName: String = "Gamer",
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("kitchen") }
    var showDisclaimer by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightPurpleBg)
    ) {
        // Simple Top Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(LightPurpleBg, CircleShape)
                        .testTag("cook_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.cook_go_back),
                        tint = PastelPurpleDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.cook_hi_user, userName),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
                    )
                    Text(
                        text = stringResource(R.string.cook_guide_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }
        }

        // Module Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "kitchen" to stringResource(R.string.cook_tab_kitchen),
                "library" to stringResource(R.string.cook_tab_library)
            ).forEach { (tabId, label) ->
                val isSelected = activeSubTab == tabId
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick {  activeSubTab = tabId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PastelPurpleDark else LightPurpleBg,
                        contentColor = if (isSelected) White else PastelPurpleDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("cook_subtab_$tabId")
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tab Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeSubTab == "kitchen") {
                if (showDisclaimer) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nutrition_disclaimer_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3CD)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFC107)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = stringResource(R.string.cook_disclaimer_text),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF856404),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            IconButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick { showDisclaimer = false },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("dismiss_disclaimer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                IngredientPickerSubSection(viewModel = viewModel)
            } else {
                RecipeLibrarySubSection(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SideBySideVisualRow(
    imageFileName: String,
    altText: String,
    modifier: Modifier = Modifier,
    imageBitmap: Bitmap? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .border(1.dp, SoftGray, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Visual Placeholder or Real Image
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 90.dp)
                .background(LightPurpleBg.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .border(1.dp, PastelPurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = altText,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                val resId = when {
                    imageFileName.contains("diy_step") -> {
                        when {
                            imageFileName.contains("step1") -> R.drawable.img_diy_gather_1784487336795
                            imageFileName.contains("step2") -> R.drawable.img_diy_measure_1784487351424
                            imageFileName.contains("step3") -> R.drawable.img_diy_assemble_1784487371981
                            else -> R.drawable.img_diy_concept_fixed
                        }
                    }
                    imageFileName.contains("cook_step") -> R.drawable.img_recipe_concept_fixed
                    else -> null
                }

                if (resId != null) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = altText,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = stringResource(R.string.guide_lily_pip_alt),
                            tint = PastelPurpleDark.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = altText,
                            fontSize = 8.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Right Column: Instruction/Description content
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}


