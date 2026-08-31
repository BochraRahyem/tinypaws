package com.example.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.BuildConfig
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
    var nearbyAlertsEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("nearby_report_alerts", true)) }

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

                    // App Theme Selector
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("settings_theme_section")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = DeepBurgundy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_theme_title),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBurgundy
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val themes = listOf(
                                false to stringResource(R.string.theme_light_coral),
                                true to stringResource(R.string.theme_dark_velvet)
                            )

                            themes.forEach { (darkVal, label) ->
                                val isSel = isDarkMode == darkVal
                                Button(
                                    onClick = com.example.ui.theme.rememberHapticOnClick {
                                        sharedPrefs.edit()
                                            .putBoolean("user_dark_mode", darkVal)
                                            .putBoolean("dark_mode", darkVal)
                                            .apply()
                                        viewModel.setDarkMode(darkVal)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) DeepBurgundy else Cream,
                                        contentColor = if (isSel) Cream else DeepBurgundy
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    border = if (!isSel) BorderStroke(1.dp, Mauve.copy(alpha = 0.5f)) else null,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
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

                    // Nearby Community Reports Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = DeepBurgundy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_nearby_alerts),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                )
                            }
                            Text(
                                text = stringResource(R.string.settings_nearby_alerts_desc),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Ink.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.padding(start = 32.dp, top = 2.dp)
                            )
                        }
                        Switch(
                            checked = nearbyAlertsEnabled,
                            onCheckedChange = { enabled ->
                                nearbyAlertsEnabled = enabled
                                sharedPrefs.edit().putBoolean("nearby_report_alerts", enabled).apply()
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
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = DeepBurgundy)
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
                                if (active) {
                                    SunsetSoundscapePlayer.start(context)
                                } else {
                                    SunsetSoundscapePlayer.stop()
                                }
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

            // Section 4: Feedback & Support
            FeedbackSupportCard()

            // Section 5: App Information
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
                        text = stringResource(R.string.settings_app_version, BuildConfig.VERSION_NAME),
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

@Composable
fun FeedbackSupportCard() {
    val context = LocalContext.current
    val authUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    
    var selectedCategory by remember { mutableStateOf("Feedback") }
    var feedbackMessage by remember { mutableStateOf("") }
    var senderEmail by remember { mutableStateOf(authUser?.email ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionStatus by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val categories = listOf("Feedback", "Bug Report", "Feature Request", "Cat Care")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().testTag("feedback_support_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = DeepBurgundy)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💌 Feedback & Support",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy,
                        fontFamily = FrauncesFontFamily
                    )
                )
            }

            Text(
                text = "Have suggestions or need help? Send your thoughts directly to the TinyPaws care team.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Ink.copy(alpha = 0.8f),
                    fontFamily = QuicksandFontFamily
                )
            )

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = com.example.ui.theme.rememberHapticOnClick { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepBurgundy,
                            selectedLabelColor = Cream,
                            containerColor = Cream.copy(alpha = 0.7f),
                            labelColor = DeepBurgundy
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) DeepBurgundy else Mauve.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Message Field
            OutlinedTextField(
                value = feedbackMessage,
                onValueChange = { if (it.length <= 500) feedbackMessage = it },
                label = { Text("Your Message", fontFamily = QuicksandFontFamily, fontSize = 13.sp) },
                placeholder = { Text("Share your ideas or issues with us...", fontSize = 12.sp, color = Ink.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).testTag("feedback_message_input"),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepBurgundy,
                    unfocusedBorderColor = Mauve.copy(alpha = 0.6f),
                    focusedContainerColor = Cream.copy(alpha = 0.4f),
                    unfocusedContainerColor = Cream.copy(alpha = 0.3f),
                    focusedLabelColor = DeepBurgundy,
                    unfocusedLabelColor = Ink.copy(alpha = 0.7f),
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${feedbackMessage.length}/500",
                    fontSize = 10.sp,
                    color = Ink.copy(alpha = 0.5f)
                )
            }

            // Optional Email Field
            OutlinedTextField(
                value = senderEmail,
                onValueChange = { senderEmail = it },
                label = { Text("Your Email (Optional for reply)", fontFamily = QuicksandFontFamily, fontSize = 13.sp) },
                placeholder = { Text("name@example.com", fontSize = 12.sp, color = Ink.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().testTag("feedback_email_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepBurgundy,
                    unfocusedBorderColor = Mauve.copy(alpha = 0.6f),
                    focusedContainerColor = Cream.copy(alpha = 0.4f),
                    unfocusedContainerColor = Cream.copy(alpha = 0.3f),
                    focusedLabelColor = DeepBurgundy,
                    unfocusedLabelColor = Ink.copy(alpha = 0.7f),
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                )
            )

            // Submit Button
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    val trimmed = feedbackMessage.trim()
                    if (trimmed.length < 5) {
                        submissionStatus = "Please enter at least 5 characters."
                        isSuccess = false
                        return@rememberHapticOnClick
                    }

                    isSubmitting = true
                    submissionStatus = null

                    val feedbackData = hashMapOf(
                        "category" to selectedCategory,
                        "message" to trimmed,
                        "senderEmail" to senderEmail.trim(),
                        "userId" to (authUser?.uid ?: "anonymous"),
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "platform" to "Android",
                        "appVersion" to BuildConfig.VERSION_NAME
                    )

                    // Safe client-side Firestore submission + asynchronous server-side mail queue
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("feedback")
                        .add(feedbackData)
                        .addOnSuccessListener {
                            // Queue notification for support team (server-side delivery)
                            val mailDoc = hashMapOf(
                                "to" to listOf("support@tinypaws.org"),
                                "template" to "feedback",
                                "message" to hashMapOf(
                                    "subject" to "[TinyPaws $selectedCategory] New Feedback Received",
                                    "text" to "Category: $selectedCategory\nFrom: ${senderEmail.ifBlank { "Anonymous" }}\n\n$trimmed",
                                    "html" to "<p><b>Category:</b> $selectedCategory</p><p><b>From:</b> ${senderEmail.ifBlank { "Anonymous" }}</p><hr><p>${trimmed.replace("\n", "<br>")}</p>"
                                ),
                                "userId" to (authUser?.uid ?: "anonymous"), "createdAt" to com.google.firebase.Timestamp.now(),
                                "status" to "pending"
                            )
                            db.collection("mail").add(mailDoc)

                            isSubmitting = false
                            isSuccess = true
                            submissionStatus = "Thank you! Your feedback has been sent to our team. 🐾"
                            feedbackMessage = ""
                        }
                        .addOnFailureListener { e ->
                            isSubmitting = false
                            isSuccess = false
                            android.util.Log.e("FeedbackSupport", "Failed to submit feedback", e)
                            submissionStatus = "Couldn't send right now. Please try again in a moment. 🐾"
                        }
                },
                enabled = !isSubmitting && feedbackMessage.trim().length >= 5,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_feedback_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBurgundy,
                    contentColor = Cream,
                    disabledContainerColor = Mauve.copy(alpha = 0.4f),
                    disabledContentColor = Cream.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Cream, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Feedback 🐾", fontWeight = FontWeight.Bold, fontFamily = QuicksandFontFamily)
                }
            }

            submissionStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isSuccess) Color(0xFF2E7D32) else DeepBurgundy,
                        fontWeight = FontWeight.Medium,
                        fontFamily = QuicksandFontFamily
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


