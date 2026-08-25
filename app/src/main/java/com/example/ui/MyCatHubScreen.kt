package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCatHubScreen(
    viewModel: TinyPawsViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val allProfiles by viewModel.allCatProfiles.collectAsState()
    val activeCatId by viewModel.selectedCatId.collectAsState()
    val currentCat by viewModel.catProfile.collectAsState()

    var showAddCatDialog by remember { mutableStateOf(false) }
    var catToDelete by remember { mutableStateOf<com.example.data.CatProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.hub_title),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy
                        )
                        Text("✨🪐", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("hub_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.hub_back),
                            tint = DeepBurgundy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Introductory decorative banner to unify the "Cat Universe"
            PixelCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BlushPink.copy(alpha = 0.25f),
                emblemType = "star"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PixelCatAnimated(pixelSize = 3.2.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.step_into_cat_universe),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepBurgundy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.step_into_cat_universe_subtitle),
                            fontFamily = QuicksandFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Ink.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Daily Cat Fact Card with Gemini API & Refresh button
            DailyCatFactCard(viewModel = viewModel)

            // Multi-Cat Switcher Header
            MultiCatSwitcherCard(
                catProfiles = allProfiles,
                activeCatId = activeCatId,
                onSelectCat = { id -> viewModel.selectCat(id) },
                onAddNewCat = { showAddCatDialog = true },
                onDeleteCat = { cat -> catToDelete = cat }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Cat Profile Button
            HubButton(
                title = stringResource(R.string.hub_cat_profile_title),
                subtitle = stringResource(R.string.hub_cat_profile_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🐱", fontSize = 24.sp)
                        Text("🎀", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp))
                    }
                },
                tag = "hub_cat_profile_btn",
                watermark = "💝",
                animationIndex = 0,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("my_cat_profile") }
            )

            // Growth & Weight
            HubButton(
                title = stringResource(R.string.hub_growth_weight_title),
                subtitle = stringResource(R.string.hub_growth_weight_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📈", fontSize = 22.sp)
                        Text("🐾", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 1.dp, y = 1.dp))
                    }
                },
                tag = "hub_weight_tracker_btn",
                watermark = "⚖️",
                animationIndex = 1,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("weight_tracker") }
            )

            // Vet Reminders
            HubButton(
                title = stringResource(R.string.hub_vet_reminders_title),
                subtitle = stringResource(R.string.hub_vet_reminders_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🩺", fontSize = 24.sp)
                        Text("🩹", fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 1.dp))
                    }
                },
                tag = "hub_reminders_btn",
                watermark = "🌡️",
                animationIndex = 2,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("vet_reminders") }
            )

            // Daily Care Checklist
            HubButton(
                title = stringResource(R.string.hub_daily_care_title),
                subtitle = stringResource(R.string.hub_daily_care_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📅", fontSize = 22.sp)
                        Text("✅", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp))
                    }
                },
                tag = "hub_daily_checklist_btn",
                watermark = "🐟",
                animationIndex = 3,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("daily_checklist") }
            )

            // Cat Care Diary
            HubButton(
                title = stringResource(R.string.hub_cat_care_diary_title),
                subtitle = stringResource(R.string.hub_cat_care_diary_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📓", fontSize = 24.sp)
                        Text("🐈", fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 1.dp))
                    }
                },
                tag = "hub_diary_feed_btn",
                watermark = "✨",
                animationIndex = 4,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("diary_feed") }
            )

            // Care Reminders
            HubButton(
                title = stringResource(R.string.hub_care_reminders_title),
                subtitle = stringResource(R.string.hub_care_reminders_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⏰", fontSize = 24.sp)
                        Text("🔔", fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 1.dp))
                    }
                },
                tag = "hub_care_reminders_btn",
                watermark = "💖",
                animationIndex = 5,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("care_reminders") }
            )

            // Data Export & Cloud Sync
            HubButton(
                title = stringResource(R.string.hub_data_sync_title),
                subtitle = stringResource(R.string.hub_data_sync_desc),
                iconContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Text("☁️", fontSize = 24.sp)
                        Text("💾", fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 1.dp))
                    }
                },
                tag = "hub_data_sync_btn",
                watermark = "📡",
                animationIndex = 6,
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("data_sync") }
            )
        }
    }

    if (showAddCatDialog) {
        AddCatDialog(
            onDismiss = { showAddCatDialog = false },
            onAddCat = { name, coat, ageY ->
                viewModel.addNewCatProfile(
                    name = name,
                    ageYears = ageY,
                    coatColor = coat
                )
                showAddCatDialog = false
            }
        )
    }

    catToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { catToDelete = null },
            title = {
                Text(
                    stringResource(R.string.multi_cat_confirm_delete_title),
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepBurgundy
                )
            },
            text = {
                Text(
                    stringResource(R.string.multi_cat_confirm_delete_msg, cat.name),
                    fontFamily = QuicksandFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick {
                        viewModel.deleteCatProfile(cat.id)
                        catToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text(stringResource(R.string.chat_delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { catToDelete = null }) {
                    Text(stringResource(R.string.triage_cancel), color = Ink)
                }
            }
        )
    }
}

@Composable
fun MultiCatSwitcherCard(
    catProfiles: List<com.example.data.CatProfile>,
    activeCatId: Int,
    onSelectCat: (Int) -> Unit,
    onAddNewCat: () -> Unit,
    onDeleteCat: (com.example.data.CatProfile) -> Unit
) {
    val isDark = LocalIsDarkMode.current

    PixelCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("multi_cat_switcher_card"),
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        emblemType = "paw"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PixelCatSleeping(pixelSize = 1.8.dp)
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.multi_cat_switch),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DeepBurgundy
                        )
                        Text(
                            text = stringResource(R.string.multi_cat_count, catProfiles.size),
                            fontFamily = QuicksandFontFamily,
                            fontSize = 11.sp,
                            color = DeepBurgundy.copy(alpha = 0.7f)
                        )
                    }
                }

                if (catProfiles.size >= 7) {
                    Text(
                        text = stringResource(R.string.multi_cat_limit_reached),
                        fontFamily = QuicksandFontFamily,
                        fontSize = 11.sp,
                        color = DeepBurgundy,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    Surface(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onAddNewCat() },
                        shape = RoundedCornerShape(20.dp),
                        color = BlushPink.copy(alpha = 0.5f),
                        modifier = Modifier.testTag("add_cat_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = DeepBurgundy,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.multi_cat_add_new),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DeepBurgundy
                            )
                        }
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(catProfiles, key = { it.id }) { cat ->
                    val isSelected = cat.id == activeCatId

                    Surface(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onSelectCat(cat.id) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) {
                            if (isDark) Color(0xFF6B2D3A) else Color(0xFFFFD6E0)
                        } else {
                            if (isDark) Color(0xFF2D1822) else Color.White.copy(alpha = 0.7f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Mauve else Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("cat_pill_${cat.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when (cat.coatColor.lowercase()) {
                                    "orange tabby 🍊", "orange", "orange tabby" -> "🍊"
                                    "black 🖤", "black" -> "🖤"
                                    "white 🤍", "white" -> "🤍"
                                    "calico 🎨", "calico" -> "🎨"
                                    "caliby", "caliby (calico + tabby) 🌸🐯" -> "🌸"
                                    "siamese 🐱", "siamese" -> "🐱"
                                    else -> "😺"
                                },
                                fontSize = 18.sp
                            )
                            Column {
                            Text(
                                text = cat.name.ifBlank { "Cat #${cat.id}" },
                                fontFamily = QuicksandFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) DeepBurgundy else Ink
                            )
                                Text(
                                    text = "${cat.ageYears}y • ${cat.coatColor.take(10)}",
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 10.sp,
                                    color = Ink.copy(alpha = 0.7f)
                                )
                            }

                            if (catProfiles.size > 1 && isSelected) {
                                IconButton(
                                    onClick = com.example.ui.theme.rememberHapticOnClick { onDeleteCat(cat) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("delete_cat_btn_${cat.id}")
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.my_cat_delete_desc),
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(16.dp)
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

@Composable
fun AddCatDialog(
    onDismiss: () -> Unit,
    onAddCat: (name: String, coat: String, ageYears: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCoat by remember { mutableStateOf("Calico") }
    var ageYearsText by remember { mutableStateOf("1") }

    val coatOptions = listOf(
        "Calico",
        "Caliby (Calico + Tabby)",
        "Orange Tabby",
        "Tuxedo / Black & White",
        "Gray / Blue",
        "White",
        "Black",
        "Tabby",
        "Tortoiseshell",
        "Siamese",
        "Persian",
        "Other / Mixed"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.multi_cat_new_cat_title),
                fontFamily = FrauncesFontFamily,
                fontWeight = FontWeight.Bold,
                color = DeepBurgundy
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.multi_cat_new_cat_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_cat_name_input")
                )

                OutlinedTextField(
                    value = ageYearsText,
                    onValueChange = { ageYearsText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.hub_age_years_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.hub_breed_coat_label), fontFamily = QuicksandFontFamily, fontSize = 12.sp, color = Ink)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(coatOptions) { coat ->
                        FilterChip(
                            selected = selectedCoat == coat,
                            onClick = { selectedCoat = coat },
                            label = { Text(coat, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val ageY = ageYearsText.toIntOrNull() ?: 1
                        onAddCat(name.trim(), selectedCoat, ageY)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlushPink),
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_cat_btn")
            ) {
                Text(stringResource(R.string.hub_add_cat_confirm), color = DeepBurgundy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.triage_cancel), color = Ink)
            }
        }
    )
}

@Composable
fun HubButton(
    title: String,
    subtitle: String,
    iconContent: @Composable () -> Unit,
    tag: String,
    watermark: String,
    animationIndex: Int = 0,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkMode.current

    val animProgress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationIndex * 50L)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 320,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }

    // Gorgeous soft pink + lavender gradient finish
    val cardGradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color(0xFF3D151D).copy(alpha = 0.9f), // Rich dark pinkish burgundy
                Color(0xFF23112E).copy(alpha = 0.9f)  // Rich dark lavender/purple
            )
        } else {
            listOf(
                Color(0xFFFFE5EC).copy(alpha = 0.85f), // Warm pastel pink
                Color(0xFFF3E5F5).copy(alpha = 0.85f)  // Warm pastel lavender
            )
        }
    )

    // Glowing border with a subtle gradient matching the theme
    val borderGradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color(0xFFFFB3C1).copy(alpha = 0.25f),
                Color(0xFFD7AEDF).copy(alpha = 0.2f)
            )
        } else {
            listOf(
                Color(0xFFFFB3C1).copy(alpha = 0.6f),
                Color(0xFFD7AEDF).copy(alpha = 0.5f)
            )
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .graphicsLayer {
                alpha = animProgress.value
                translationY = (1f - animProgress.value) * 20.dp.toPx()
                scaleX = 0.96f + (0.04f * animProgress.value)
                scaleY = 0.96f + (0.04f * animProgress.value)
            }
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .border(
                BorderStroke(1.dp, borderGradient),
                RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Elegant watermark element in the background of the card (subtle, low-opacity decoration)
            Text(
                text = watermark,
                fontSize = 44.sp,
                color = (if (isDark) Color.White else Ink).copy(alpha = if (isDark) 0.05f else 0.08f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = 5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Highly polished icon container
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF5D1E2A).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.65f),
                    border = BorderStroke(
                        1.5.dp,
                        if (isDark) Color(0xFFFFB3C1).copy(alpha = 0.25f) else Color(0xFFFFC2D1)
                    ),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        iconContent()
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DeepBurgundy,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontFamily = QuicksandFontFamily,
                        fontSize = 11.5.sp,
                        color = Ink.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DailyCatFactCard(viewModel: TinyPawsViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var catFact by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val loadFact = {
        isLoading = true
        coroutineScope.launch {
            val fact = viewModel.fetchDailyCatFact(currentLang)
            catFact = fact
            isLoading = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(currentLang) {
        loadFact()
    }

    PixelCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PastelLightPink.copy(alpha = 0.25f),
        emblemType = "paw"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PixelCatSleeping(pixelSize = 1.8.dp)
                    }
                    Text(
                        text = stringResource(R.string.daily_cat_fact_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DeepBurgundy
                    )
                }
                IconButton(
                    onClick = rememberHapticOnClick { loadFact() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("refresh_cat_fact_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DeepBurgundy,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh fact",
                            tint = DeepBurgundy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Text(
                text = catFact,
                fontFamily = QuicksandFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Ink.copy(alpha = 0.85f)
            )
        }
    }
}
