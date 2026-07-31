package com.example.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE) }
    val scrollState = rememberScrollState()

    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isExtremeWeatherNotify by viewModel.isExtremeWeatherNotify.collectAsStateWithLifecycle()

    var userName by remember { mutableStateOf(sharedPrefs.getString("user_name", "Cat Guardian") ?: "Cat Guardian") }
    var soundEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("sound_enabled", true)) }
    var dailyReminder by remember { mutableStateOf(sharedPrefs.getBoolean("daily_reminder", true)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepBurgundy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BlushPink.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = DeepBurgundy,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = FrauncesFontFamily,
                            color = DeepBurgundy
                        )
                        Text(
                            text = stringResource(R.string.settings_header_desc),
                            fontSize = 12.sp,
                            fontFamily = QuicksandFontFamily,
                            color = Ink.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Section 1: Language
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("settings_language_section")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = DeepBurgundy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.menu_language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepBurgundy,
                                fontFamily = FrauncesFontFamily
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languages = listOf(
                            Triple("en", "English", "🇺🇸"),
                            Triple("ar", "العربية", "🇸🇦"),
                            Triple("fr", "Français", "🇫🇷"),
                            Triple("es", "Español", "🇪🇸")
                        )

                        languages.forEach { (code, name, flag) ->
                            val isSelected = currentLang == code
                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    viewModel.setLanguage(code)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) DeepBurgundy else Cream,
                                    contentColor = if (isSelected) Cream else DeepBurgundy
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (!isSelected) BorderStroke(1.dp, Mauve.copy(alpha = 0.5f)) else null,
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    text = "$flag $name",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Theme & Notifications
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_preferences_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy,
                            fontFamily = FrauncesFontFamily
                        )
                    )

                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = DeepBurgundy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.main_dark_mode),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { dark ->
                                sharedPrefs.edit().putBoolean("dark_mode", dark).apply()
                                viewModel.setDarkMode(dark)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Cream,
                                checkedTrackColor = Wine
                            )
                        )
                    }

                    HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

                    // Weather Alerts Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = DeepBurgundy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_weather_alerts),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                )
                            }
                            Text(
                                text = stringResource(R.string.settings_weather_alerts_desc),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Ink.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.padding(start = 32.dp, top = 2.dp)
                            )
                        }
                        Switch(
                            checked = isExtremeWeatherNotify,
                            onCheckedChange = { notify ->
                                sharedPrefs.edit().putBoolean("extreme_weather_notifications", notify).apply()
                                viewModel.setExtremeWeatherNotify(notify)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Cream,
                                checkedTrackColor = Wine
                            )
                        )
                    }

                    HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

                    // Sound Effects Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DeepBurgundy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_sound_effects),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { active ->
                                soundEnabled = active
                                sharedPrefs.edit().putBoolean("sound_enabled", active).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Cream,
                                checkedTrackColor = Wine
                            )
                        )
                    }
                }
            }

            // Section 3: Privacy & Security
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = DeepBurgundy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_privacy_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepBurgundy,
                                fontFamily = FrauncesFontFamily
                            )
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_privacy_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Ink.copy(alpha = 0.8f),
                            fontFamily = QuicksandFontFamily,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Section 4: App Information
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TinyPaws Diary 🐾",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy,
                            fontFamily = FrauncesFontFamily
                        )
                    )
                    Text(
                        text = stringResource(R.string.settings_app_version, "1.2.0"),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                    Text(
                        text = stringResource(R.string.settings_mission_statement),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Ink.copy(alpha = 0.7f),
                            fontFamily = QuicksandFontFamily,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}


