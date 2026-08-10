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

            // Section 4: Developer & Diagnostics
            DeveloperDiagnosticsCard()

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

@Composable
fun DeveloperDiagnosticsCard() {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    var emailStatusMessage by remember { mutableStateOf<String?>(null) }
    var mailLogs by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoadingMailLogs by remember { mutableStateOf(false) }

    // Fetch FCM logs
    val fcmLogs = remember {
        val prefs = context.getSharedPreferences("tinypaws_fcm_logs", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("fcm_logs_json", "[]") ?: "[]"
        val list = mutableListOf<Map<String, String>>()
        try {
            val array = org.json.JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    mapOf(
                        "title" to obj.optString("title", "No Title"),
                        "body" to obj.optString("body", "No Body"),
                        "data" to obj.optString("data", ""),
                        "timestamp" to obj.optString("timestamp", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    val fetchMailLogs = {
        isLoadingMailLogs = true
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("mail")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { query ->
                val logs = query.documents.map { doc ->
                    val status = doc.getString("status") ?: "pending"
                    val sentAt = doc.get("sentAt")?.toString() ?: doc.get("createdAt")?.toString() ?: "N/A"
                    val error = doc.getString("error") ?: "None"
                    val toList = doc.get("to")?.toString() ?: "Unknown"
                    mapOf(
                        "id" to doc.id,
                        "status" to status,
                        "sentAt" to sentAt,
                        "error" to error,
                        "to" to toList
                    )
                }
                mailLogs = logs
                isLoadingMailLogs = false
            }
            .addOnFailureListener {
                isLoadingMailLogs = false
            }
    }

    LaunchedEffect(Unit) {
        fetchMailLogs()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Mauve.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().testTag("developer_diagnostics_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = DeepBurgundy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🛠️ Developer & Diagnostics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy,
                            fontFamily = FrauncesFontFamily
                        )
                    )
                }
            }

            Text(
                text = "Developer testing controls for Resend email queue & FCM push notifications.",
                style = MaterialTheme.typography.bodySmall.copy(color = Ink.copy(alpha = 0.8f))
            )

            HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

            // 1. Trigger Welcome Email Button
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val email = authUser?.email ?: "user@tinypaws.org"
                    val mailDoc = mapOf(
                        "to" to listOf(email),
                        "template" to "welcome",
                        "message" to mapOf(
                            "subject" to "Welcome to TinyPaws 🐾",
                            "text" to "Welcome to TinyPaws! Thank you for supporting community cats.",
                            "html" to "<p>Welcome to <b>TinyPaws</b>! Thank you for supporting community cats.</p>"
                        ),
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "status" to "pending"
                    )
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("mail")
                        .add(mailDoc)
                        .addOnSuccessListener {
                            emailStatusMessage = "Welcome email document created! (ID: ${it.id})"
                            fetchMailLogs()
                        }
                        .addOnFailureListener { err ->
                            emailStatusMessage = "Failed: ${err.message}"
                        }
                },
                modifier = Modifier.fillMaxWidth().testTag("trigger_welcome_email_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy, contentColor = Cream),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trigger Test Welcome Email 📩", fontWeight = FontWeight.Bold)
            }

            if (emailStatusMessage != null) {
                Text(
                    text = emailStatusMessage!!,
                    style = MaterialTheme.typography.bodySmall.copy(color = DeepBurgundy, fontWeight = FontWeight.Medium)
                )
            }

            HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

            // 2. Admin Mail Status Diagnostic View
            Text(
                text = "📬 Admin Mail Queue (Last 10 Firestore Docs)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DeepBurgundy,
                fontFamily = FrauncesFontFamily
            )

            if (isLoadingMailLogs) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally), color = DeepBurgundy)
            } else if (mailLogs.isEmpty()) {
                Text("No mail documents found in Firestore queue.", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    mailLogs.forEach { log ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Cream.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "To: ${log["to"]}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = DeepBurgundy
                                    )
                                    val statusColor = when (log["status"]) {
                                        "sent", "success" -> Color(0xFF2E7D32)
                                        "failed" -> Color(0xFFC62828)
                                        else -> Color(0xFFE65100)
                                    }
                                    Text(
                                        text = "Status: ${log["status"]}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = statusColor
                                    )
                                }
                                Text("Time: ${log["sentAt"]}", fontSize = 10.sp, color = Ink.copy(alpha = 0.7f))
                                if (log["error"] != "None" && log["error"]!!.isNotBlank()) {
                                    Text("Error: ${log["error"]}", fontSize = 10.sp, color = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

            // 3. FCM Notification Payload Diagnostic View
            Text(
                text = "🔔 FCM Notification Arrival Diagnostics (Last 5)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DeepBurgundy,
                fontFamily = FrauncesFontFamily
            )

            if (fcmLogs.isEmpty()) {
                Text("No FCM payloads recorded yet. (Trigger a rescue report to test).", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    fcmLogs.forEach { log ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BlushPink.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = log["title"] ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = DeepBurgundy
                                )
                                Text(
                                    text = log["body"] ?: "",
                                    fontSize = 11.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "Received: ${log["timestamp"]} | Data: ${log["data"]}",
                                    fontSize = 9.sp,
                                    color = Ink.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


