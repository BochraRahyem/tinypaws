package com.example

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.core.view.WindowCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import com.example.ui.stringResource
import com.example.ui.LocalLanguage
import com.example.ui.FeedingStationsScreen
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.LogEntry
import com.example.data.LogRepository
import com.example.data.StrayReportRepository
import com.example.data.CatRepository
import com.example.ui.Badge
import com.example.ui.TinyPawsViewModel
import com.example.ui.TinyPawsViewModelFactory
import com.example.ui.TrackerUiState
import com.example.ui.GuideModuleScreen
import com.example.ui.TinyPawsIntroSequence
import com.example.ui.CatsNearMeScreen
import com.example.ui.GameModuleScreen
import com.example.ui.LocationModuleScreen
import com.example.ui.CreationModuleScreen
import com.example.ui.CookModuleScreen
import com.example.ui.MyCatScreen
import com.example.ui.MyCatHubScreen
import com.example.ui.ReminderScreen
import com.example.ui.CertificateScreen
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.SunsetSoundscapePlayer
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        SunsetSoundscapePlayer.start(this)
    }

    override fun onStop() {
        super.onStop()
        SunsetSoundscapePlayer.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SharedPreferences name persistence
        val sharedPrefs = getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LogRepository(database.logDao(), database.favoriteDao())
        val strayRepository = StrayReportRepository(database.strayReportDao())
        val catRepository = CatRepository(database.catProfileDao(), database.catWeightLogDao(), database.catCheckInLogDao(), database.reminderDao())
        
        // Instantiate ViewModel
        val viewModel: TinyPawsViewModel by viewModels {
            TinyPawsViewModelFactory(application, repository, strayRepository, catRepository)
        }

        // Schedule periodic background weather alerts using WorkManager
        com.example.worker.WeatherAlertWorker.schedulePeriodicWeatherCheck(applicationContext)

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Request location permissions on startup, just like notification permission
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 102)
        }

        // Load existing onboarded name and language
        val savedName = sharedPrefs.getString("user_name", "") ?: ""
        viewModel.updateOnboardedName(savedName)

        val savedLang = sharedPrefs.getString("user_lang", "en") ?: "en"
        viewModel.setLanguage(savedLang)

        val today = System.currentTimeMillis() / 86400000L
        val lastOpened = sharedPrefs.getLong("last_opened_date", 0L)
        var streak = sharedPrefs.getInt("streak_count", 0)

        if (lastOpened != today) {
            if (lastOpened == today - 1L) {
                streak += 1
            } else if (lastOpened < today - 1L) {
                streak = 1
            }
            sharedPrefs.edit()
                .putLong("last_opened_date", today)
                .putInt("streak_count", streak)
                .apply()
        } else if (streak == 0) {
            streak = 1
            sharedPrefs.edit()
                .putLong("last_opened_date", today)
                .putInt("streak_count", streak)
                .apply()
        }
        viewModel.setStreak(streak)

        val savedDarkMode = sharedPrefs.getBoolean("user_dark_mode", false)
        viewModel.setDarkMode(savedDarkMode)
        
        val savedExtremeWeather = sharedPrefs.getBoolean("extreme_weather_notifications", false)
        viewModel.setExtremeWeatherNotify(savedExtremeWeather)
        
        // Ensure AppCompatDelegate has the right locale on startup
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
                ThemeProvider(currentLanguage = currentLang) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = PastelBlueBg
                    ) { innerPadding ->
                        TinyPawsMainContainer(
                            viewModel = viewModel,
                            onSaveName = { name ->
                                sharedPrefs.edit().putString("user_name", name).apply()
                                viewModel.updateOnboardedName(name)
                            },
                            onLanguageChange = { code ->
                                sharedPrefs.edit().putString("user_lang", code).apply()
                                viewModel.setLanguage(code)
                                val appLocale = LocaleListCompat.forLanguageTags(code)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            },
                            onLogout = {
                                sharedPrefs.edit().putString("user_name", "").apply()
                                viewModel.updateOnboardedName("")
                                viewModel.resetTriage()
                                viewModel.resetGameProgress()

                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TinyPawsMainContainer(
    viewModel: TinyPawsViewModel,
    onSaveName: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onboardedName by viewModel.onboardedName.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isExtremeWeatherNotify by viewModel.isExtremeWeatherNotify.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf("dashboard") } // "dashboard", "guide", "play", "find", "cook", "chat"
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // We need sharedPrefs to save dark mode
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    var hasSeenIntro by remember { mutableStateOf(sharedPrefs.getBoolean("has_seen_intro", false)) }

    if (!hasSeenIntro) {
        TinyPawsIntroSequence(
            onComplete = {
                sharedPrefs.edit().putBoolean("has_seen_intro", true).apply()
                hasSeenIntro = true
            }
        )
    } else if (onboardedName.isEmpty()) {
        OnboardingStartupScreen(onStepIn = onSaveName)
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.7f)
                ) {
                    ModalDrawerSheet(
                        modifier = Modifier.fillMaxSize(),
                        drawerContainerColor = if (isDarkMode) com.example.ui.theme.DarkBgStart else Color(0xFFFFF0F2),
                        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
                    ) {
                        Spacer(Modifier.height(48.dp))
                        Text(
                            stringResource(R.string.menu_language),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepBurgundy
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            thickness = 1.dp,
                            color = Mauve.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val languages = listOf(
                            "en" to ("🇺🇸  " + stringResource(R.string.lang_en)),
                            "ar" to ("🇸🇦  " + stringResource(R.string.lang_ar)),
                            "fr" to ("🇫🇷  " + stringResource(R.string.lang_fr)),
                            "es" to ("🇪🇸  " + stringResource(R.string.lang_es))
                        )
                        
                        languages.forEach { (code, label) ->
                            val isSelected = currentLang == code
                            NavigationDrawerItem(
                                label = { 
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    ) 
                                },
                                selected = isSelected,
                                onClick = com.example.ui.theme.rememberHapticOnClick { 
                                    onLanguageChange(code)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .height(56.dp),
                                shape = CircleShape,
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = DeepBurgundy,
                                    unselectedContainerColor = BlushPink.copy(alpha = 0.25f),
                                    selectedTextColor = Cream,
                                    unselectedTextColor = Ink
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            thickness = 1.dp,
                            color = Mauve.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationDrawerItem(
                            label = { 
                                Text(
                                    text = stringResource(R.string.main_settings),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                ) 
                            },
                            selected = currentScreen == "settings",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                currentScreen = "settings"
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .height(56.dp)
                                .testTag("drawer_settings_item"),
                            shape = CircleShape,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DeepBurgundy,
                                unselectedContainerColor = BlushPink.copy(alpha = 0.25f),
                                selectedTextColor = Cream,
                                unselectedTextColor = Ink
                            )
                        )
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(if (com.example.ui.theme.LocalIsDarkMode.current) com.example.ui.theme.OmbreGradientBrushDark else com.example.ui.theme.OmbreGradientBrushLight)) {
                when (currentScreen) {
                    "dashboard" -> TinyPawsDashboard(
                        viewModel = viewModel,
                        userName = onboardedName,
                        onLogout = onLogout,
                        onNavigate = { currentScreen = it },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        modifier = modifier
                    )
                "my_cat_hub" -> MyCatHubScreen(
                    onNavigate = { currentScreen = it },
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "my_cat_profile" -> MyCatScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "guide" -> GuideModuleScreen(
                    viewModel = viewModel,
                    userName = onboardedName,
                    onBack = { currentScreen = "dashboard" },
                    onNavigateToWeather = { currentScreen = "weather" },
                    modifier = modifier
                )
                "weather" -> com.example.ui.WeatherAppScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "play" -> GameModuleScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    onClaimCertificate = { currentScreen = "certificate" },
                    modifier = modifier
                )
                "cats_near_me" -> CatsNearMeScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "feeding_stations" -> FeedingStationsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "find" -> LocationModuleScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "cook" -> CookModuleScreen(
                    viewModel = viewModel,
                    userName = onboardedName,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "chat" -> TinyPawsHelperScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "reminders" -> ReminderScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "certificate" -> CertificateScreen(
                    userName = onboardedName,
                    onClose = { currentScreen = "play" },
                    modifier = modifier
                )
                "settings" -> com.example.ui.SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
            }
        }
    }
}
}

@Composable
fun OnboardingStartupScreen(
    onStepIn: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (com.example.ui.theme.LocalIsDarkMode.current) com.example.ui.theme.OmbreGradientBrushDark else com.example.ui.theme.OmbreGradientBrushLight)
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
                    text = "👧🐈",
                    fontSize = 72.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.onboarding_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.onboarding_question),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = { Text(stringResource(R.string.onboarding_placeholder), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input"),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBurgundy,
                        unfocusedBorderColor = Mauve,
                        focusedContainerColor = Cream.copy(alpha = 0.6f),
                        unfocusedContainerColor = Cream.copy(alpha = 0.4f),
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        if (nameInput.trim().isNotEmpty()) {
                            onStepIn(nameInput.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("step_in_softly_btn"),
                    enabled = nameInput.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepBurgundy,
                        disabledContainerColor = Mauve.copy(alpha = 0.5f)
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_btn),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (nameInput.trim().isNotEmpty()) Cream else Ink.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TinyPawsDashboard(
    viewModel: TinyPawsViewModel,
    userName: String,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val triageStep by viewModel.triageStep.collectAsStateWithLifecycle()
    val hasInjuries by viewModel.hasInjuries.collectAsStateWithLifecycle()
    val catAgeGroup by viewModel.catAgeGroup.collectAsStateWithLifecycle()
    val trackerState by viewModel.trackerUiState.collectAsStateWithLifecycle()
    val streakCount by viewModel.streak.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("chat") },
                containerColor = DeepBurgundy,
                contentColor = Cream,
                shape = CircleShape
            ) {
                Text("🐾", fontSize = 24.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Hero Header Banner
            HeroHeader(streakCount = streakCount, userName = userName, onLogout = onLogout, onMenuClick = onMenuClick)

            // Tip of the Day (top)
            TipOfTheDayCard()

            // Section 1: Choose your diary room
            Text(
                text = stringResource(R.string.diary_room_title),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            NavigationGridSection(onNavigate = onNavigate)

            // Section 2: Help a stray cat
            Text(
                text = stringResource(R.string.help_stray_title),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Cats Near Me
            NavigationCard(
                title = stringResource(R.string.nav_cats_near_me_title),
                subtitle = stringResource(R.string.cats_near_me_subtitle),
                backgroundColor = Color(0xFFE8F5E9), // Soft green
                accentColor = Color(0xFF2E7D32),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    viewModel.updateCatsNearMeTab("browse")
                    onNavigate("cats_near_me")
                }
            )

            // Heatwave & Extreme Weather Alert Tracker
            NavigationCard(
                title = "🔥 Heatwave & Weather Tracker",
                subtitle = "7-day temperature warnings (>35°C red, <15°C blue) for cat safety.",
                backgroundColor = Color(0xFFFFF3E0), // Soft warm peach
                accentColor = Color(0xFFD32F2F), // Red
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    onNavigate("weather")
                }
            )

            // Feeding Stations
            NavigationCard(
                title = stringResource(R.string.nav_feeding_stations_title),
                subtitle = stringResource(R.string.feeding_stations_subtitle),
                backgroundColor = Color(0xFFFFF3E0), // Soft warm peach
                accentColor = Color(0xFFE65100), // Deep orange
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    onNavigate("feeding_stations")
                }
            )
            
            // Did You Find a Cat? (Interactive Triage Guide)
            TriageGuideCard(
                step = triageStep,
                hasInjuries = hasInjuries,
                catAgeGroup = catAgeGroup,
                onStart = { viewModel.startTriage() },
                onSelectInjuries = { viewModel.selectInjuries(it) },
                onSelectAge = { viewModel.selectAgeGroup(it) },
                onReset = { viewModel.resetTriage() }
            )
            
            // Community Care Tracker
            CommunityImpactTrackerCard(
                state = trackerState,
                onLogActivity = { type, notes -> viewModel.logActivity(type, notes) },
                onDeleteLog = { id -> viewModel.deleteLog(id) },
                onClearLogs = { viewModel.clearAllLogs() }
            )

            // Section 3: My Kitty's Corner
            Text(
                text = stringResource(R.string.main_my_kitty_corner),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )
            
            SurpriseMeSection(
                viewModel = viewModel,
                onNavigate = onNavigate
            )
            
            // About Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .glassyCard(shape = RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.main_about_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.main_about_desc),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = QuicksandFontFamily,
                            color = Ink.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            // 5. Friendly Footer
            FooterSection()
        }
    }
}
@Composable
fun TipOfTheDayCard() {

    val facts = androidx.compose.ui.res.stringArrayResource(id = R.array.cat_facts)
    val fact = remember { facts.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = BlushPink.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.tip_of_day_title), fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy)
            Spacer(modifier = Modifier.height(4.dp))
            Text(fact, fontFamily = QuicksandFontFamily, color = Ink)
        }
    }
}
@Composable
fun SurpriseMeSection(
    viewModel: TinyPawsViewModel,
    onNavigate: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassyCard(shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.main_discover_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.main_discover_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = QuicksandFontFamily,
                        color = Ink.copy(alpha = 0.8f)
                    )
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    val result = viewModel.selectRandomSurprise(sharedPrefs)
                    val tab = result.first
                    val projectId = result.second
                    viewModel.updateActiveGuideTab(tab)
                    viewModel.updateActiveDiyProject(projectId)
                    onNavigate("guide")
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("surprise_me_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBurgundy,
                    contentColor = Cream
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = "Surprise Me")
            }
        }
    }
}

@Composable
fun CatsNearMeDashboardSection(
    viewModel: TinyPawsViewModel,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassyCard(shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.main_cats_near_me_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepBurgundy
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.main_cats_near_me_desc),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = QuicksandFontFamily,
                    color = Ink.copy(alpha = 0.8f)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Report Sighting (Oval)
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        viewModel.updateCatsNearMeTab("report")
                        onNavigate("cats_near_me")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_report_stray_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepBurgundy,
                        contentColor = Cream
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.main_report_stray),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Button 2: Browse Nearby (Oval, Outlined/Blush Theme)
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        viewModel.updateCatsNearMeTab("browse")
                        onNavigate("cats_near_me")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.5.dp, DeepBurgundy, CircleShape)
                        .testTag("home_browse_strays_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cream.copy(alpha = 0.6f),
                        contentColor = DeepBurgundy
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.main_browse_nearby),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun HeroHeader(
    streakCount: Int,
    userName: String,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        // Story-driven generated illustration
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner_1783532549587),
            contentDescription = "Beautiful cozy illustration of a sleeping cat next to a diary",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Soft gradient overlay to blend into the cream top of ombre background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Cream.copy(alpha = 0.4f),
                            Cream
                        ),
                        startY = 150f
                    )
                )
        )

        // Top Header greeting "Hi, [Name]!" with log-out trigger and hamburger menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .glassyCard(shape = CircleShape, elevation = 4.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onMenuClick() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open navigation menu",
                    tint = DeepBurgundy
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Streak badge
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(Gold.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streakCount day streak",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                )
            }

            Text(
                text = stringResource(R.string.hi_user, userName),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onLogout() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = stringResource(R.string.logout_content_description),
                    tint = Wine,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .glassyCard(shape = RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.dashboard_tagline),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationGridSection(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavigationCard(
                title = stringResource(R.string.nav_guide_title),
                subtitle = stringResource(R.string.nav_guide_subtitle),
                backgroundColor = LightPurpleBg,
                accentColor = PastelPurpleDark,
                modifier = Modifier.weight(1f),
                onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("guide") }
            )

            NavigationCard(
                title = stringResource(R.string.nav_play_title),
                subtitle = stringResource(R.string.nav_play_subtitle),
                backgroundColor = Color(0xFFE0F2F1), // Soft mint green
                accentColor = Color(0xFF00796B),
                modifier = Modifier.weight(1f),
                onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("play") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavigationCard(
                title = stringResource(R.string.nav_find_title),
                subtitle = stringResource(R.string.nav_find_subtitle),
                backgroundColor = Color(0xFFFFF3E0), // Soft orange/peach
                accentColor = Color(0xFFE65100),
                modifier = Modifier.weight(1f),
                onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("find") }
            )

            NavigationCard(
                title = stringResource(R.string.nav_cook_title),
                subtitle = stringResource(R.string.nav_cook_subtitle),
                backgroundColor = Color(0xFFFCE4EC), // Soft pink
                accentColor = Color(0xFFC2185B),
                modifier = Modifier.weight(1f),
                onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("cook") }
            )
        }
    }
}
@Composable
fun NavigationCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .glassyCard(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { onClick() }),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MyCatAndRemindersSection(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // My Cat Card (Small)
        NavigationCard(
            title = "My Cat 🐱",
            subtitle = "Profile, Weight, Mood",
            backgroundColor = BlushPink.copy(alpha = 0.2f),
            accentColor = DeepBurgundy,
            modifier = Modifier.weight(1f),
            onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("my_cat") }
        )
        // Reminders Card (Small)
        NavigationCard(
            title = "Reminders ⏰",
            subtitle = "Feeding, Vet",
            backgroundColor = Color(0xFFE1F5FE),
            accentColor = Color(0xFF0288D1),
            modifier = Modifier.weight(1f),
            onClick = com.example.ui.theme.rememberHapticOnClick {  onNavigate("reminders") }
        )
    }
}
@Composable
fun TriageGuideCard(
    step: Int,
    hasInjuries: Boolean?,
    catAgeGroup: String?,
    onStart: () -> Unit,
    onSelectInjuries: (Boolean) -> Unit,
    onSelectAge: (String) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("triage_guide_card")
            .glassyCard(shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .animateContentSize()
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Cream, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Medical",
                        tint = DeepBurgundy,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.triage_header),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            when (step) {
                0 -> {
                    // Start screen
                    Text(
                        text = stringResource(R.string.triage_start_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onStart() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_triage_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        shape = CircleShape
                    ) {
                        Text(
                            text = stringResource(R.string.triage_start_btn),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Cream
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Offline / No Mobile Data Quick Guidance
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, DeepBurgundy.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "No Mobile Data",
                                    tint = DeepBurgundy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.offline_tips_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DeepBurgundy
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.offline_tip_1) + "\n" +
                                       stringResource(R.string.offline_tip_2) + "\n" +
                                       stringResource(R.string.offline_tip_3) + "\n" +
                                       stringResource(R.string.offline_tip_4),
                                fontSize = 11.sp,
                                color = Ink.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                1 -> {
                    // Step 1: Check injuries
                    Text(
                        text = "STEP 1 OF 2",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.triage_step_injury_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.triage_step_injury_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectInjuries(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("injury_yes_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = RedError),
                            shape = CircleShape
                        ) {
                            Text(
                                text = stringResource(R.string.triage_injury_yes),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Cream
                                )
                            )
                        }
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectInjuries(false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("injury_no_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                            shape = CircleShape
                        ) {
                            Text(
                                text = stringResource(R.string.triage_injury_no),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Cream
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onReset() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.triage_cancel),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                2 -> {
                    // Step 2: Check Age
                    Text(
                        text = stringResource(R.string.triage_step_counter, 2),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.triage_step_age_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.triage_step_age_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AgeOptionCard(
                            title = stringResource(R.string.triage_age_baby),
                            description = stringResource(R.string.triage_age_baby_desc),
                            onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectAge("baby") }
                        )
                        AgeOptionCard(
                            title = stringResource(R.string.triage_age_young),
                            description = stringResource(R.string.triage_age_young_desc),
                            onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectAge("young") }
                        )
                        AgeOptionCard(
                            title = stringResource(R.string.triage_age_adult),
                            description = stringResource(R.string.triage_age_adult_desc),
                            onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectAge("adult") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = com.example.ui.theme.rememberHapticOnClick {  onSelectInjuries(false) }) {
                            Text(text = stringResource(R.string.back_btn), color = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(onClick = com.example.ui.theme.rememberHapticOnClick { onReset() }) {
                            Text(text = stringResource(R.string.reset_btn), color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                3 -> {
                    // Step 3: Result/Triage Plan
                    val isInjured = hasInjuries == true
                    
                    Text(
                        text = stringResource(R.string.triage_plan_header),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isInjured) stringResource(R.string.plan_emergency_title) else stringResource(R.string.plan_care_guide_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isInjured) RedError else DeepBurgundy
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isInjured) {
                        EmergencyPlanView()
                    } else {
                        when (catAgeGroup) {
                            "baby" -> TinyKittenPlanView()
                            "young" -> YoungKittenPlanView()
                            "adult" -> AdultCatPlanView()
                            else -> DefaultPlanView()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onReset() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_triage_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        shape = CircleShape
                    ) {
                        Text(
                            text = stringResource(R.string.triage_completed_btn),
                            color = Cream,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgeOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { onClick() }),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurpleBg),
        border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelPurpleDark
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextDark,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

@Composable
fun EmergencyPlanView() {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.plan_emergency_intro),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        BulletPoint(stringResource(R.string.plan_emergency_step1_title), stringResource(R.string.plan_emergency_step1_desc))
        BulletPoint(stringResource(R.string.plan_emergency_step2_title), stringResource(R.string.plan_emergency_step2_desc))
        BulletPoint(stringResource(R.string.plan_emergency_step3_title), stringResource(R.string.plan_emergency_step3_desc))
    }
}

@Composable
fun TinyKittenPlanView() {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.plan_baby_intro),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        BulletPoint(stringResource(R.string.plan_baby_step1_title), stringResource(R.string.plan_baby_step1_desc))
        BulletPoint(stringResource(R.string.plan_baby_step2_title), stringResource(R.string.plan_baby_step2_desc))
        BulletPoint(stringResource(R.string.plan_baby_step3_title), stringResource(R.string.plan_baby_step3_desc))
        BulletPoint(stringResource(R.string.plan_baby_step4_title), stringResource(R.string.plan_baby_step4_desc))
    }
}

@Composable
fun YoungKittenPlanView() {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.plan_young_intro),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        BulletPoint(stringResource(R.string.plan_young_step1_title), stringResource(R.string.plan_young_step1_desc))
        BulletPoint(stringResource(R.string.plan_young_step2_title), stringResource(R.string.plan_young_step2_desc))
        BulletPoint(stringResource(R.string.plan_young_step3_title), stringResource(R.string.plan_young_step3_desc))
        BulletPoint(stringResource(R.string.plan_young_step4_title), stringResource(R.string.plan_young_step4_desc))
    }
}

@Composable
fun AdultCatPlanView() {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.plan_adult_intro),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
        BulletPoint(stringResource(R.string.plan_adult_step1_title), stringResource(R.string.plan_adult_step1_desc))
        BulletPoint(stringResource(R.string.plan_adult_step2_title), stringResource(R.string.plan_adult_step2_desc))
        BulletPoint(stringResource(R.string.plan_adult_step3_title), stringResource(R.string.plan_adult_step3_desc))
        BulletPoint(stringResource(R.string.plan_adult_step4_title), stringResource(R.string.plan_adult_step4_desc))
    }
}

@Composable
fun DefaultPlanView() {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BulletPoint(stringResource(R.string.plan_default_step1_title), stringResource(R.string.plan_default_step1_desc))
        BulletPoint(stringResource(R.string.plan_default_step2_title), stringResource(R.string.plan_default_step2_desc))
        BulletPoint(stringResource(R.string.plan_default_step3_title), stringResource(R.string.plan_default_step3_desc))
    }
}

@Composable
fun BulletPoint(title: String, description: String) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PastelPurpleDark
            )
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            )
        }
    }
}

@Composable
fun CommunityImpactTrackerCard(
    state: TrackerUiState,
    onLogActivity: (String, String) -> Unit,
    onDeleteLog: (Int) -> Unit,
    onClearLogs: () -> Unit
) {
    var selectedActivityType by remember { mutableStateOf("feed_cat") }
    var notesText by remember { mutableStateOf("") }
    var showAllHistory by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("tracker_card")
            .glassyCard(shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BlushPink.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Impact",
                        tint = Wine,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.tracker_header),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Score and Progress Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.tracker_rank_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(state.rankResId),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${state.totalPoints} pts",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Beautiful Custom Progress Bar (Blush track, Burgundy progress fill)
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { state.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = BlushPink.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (state.pointsToNextRank > 0) {
                    Text(
                        text = stringResource(R.string.tracker_points_needed, state.pointsToNextRank, stringResource(state.nextRankResId)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                } else {
                    Text(
                        text = stringResource(R.string.tracker_hero_tier),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Badges Section
            Text(
                text = stringResource(R.string.tracker_badges_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.badges.forEach { badge ->
                    BadgeView(badge = badge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BlushPink.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Log activity Form
            Text(
                text = stringResource(R.string.tracker_log_form_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Activity Chips Grid / Row
            val activities = listOf(
                "feed_cat" to stringResource(R.string.tracker_activity_feed),
                "build_shelter" to stringResource(R.string.tracker_activity_shelter),
                "rescue_cat" to stringResource(R.string.tracker_activity_medical),
                "vet_visit" to stringResource(R.string.tracker_activity_medical),
                "cuddle_socialize" to stringResource(R.string.tracker_activity_socialize),
                "donate_supplies" to stringResource(R.string.tracker_activity_donate)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { (type, label) ->
                    val isSelected = selectedActivityType == type
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) DeepBurgundy else BlushPink.copy(alpha = 0.4f))
                            .clickable { selectedActivityType = type }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Cream else DeepBurgundy
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes input
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.tracker_notes_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input_field"),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepBurgundy,
                    unfocusedBorderColor = Mauve,
                    focusedContainerColor = Cream.copy(alpha = 0.6f),
                    unfocusedContainerColor = Cream.copy(alpha = 0.4f),
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    onLogActivity(selectedActivityType, notesText)
                    notesText = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_log_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                shape = CircleShape
            ) {
                Text(
                    text = stringResource(R.string.tracker_add_btn, 
                        when (selectedActivityType) {
                            "feed_cat" -> 15
                            "build_shelter" -> 60
                            "rescue_cat" -> 100
                            "vet_visit" -> 50
                            "cuddle_socialize" -> 10
                            "donate_supplies" -> 30
                            else -> 10
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Cream
                    )
                )
            }

            // Recent Logs Section
            if (state.logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.tracker_recent_entries),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (state.logs.size > 3) {
                        TextButton(onClick = com.example.ui.theme.rememberHapticOnClick {  showAllHistory = !showAllHistory }) {
                            Text(
                                text = if (showAllHistory) stringResource(R.string.tracker_show_less) else stringResource(R.string.tracker_view_all, state.logs.size),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val logsToDisplay = if (showAllHistory) state.logs else state.logs.take(3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    logsToDisplay.forEach { log ->
                        HistoryLogItem(log = log, onDelete = { onDeleteLog(log.id) })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick { onClearLogs() },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Wine),
                    border = BorderStroke(1.dp, Mauve),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.tracker_reset_data),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeView(badge: Badge) {

    // Resolve translated name and description from keys
    val badgeName = when (badge.name) {
        "badge_first_steps_name" -> stringResource(R.string.badge_first_steps_name)
        "badge_kind_feeder_name" -> stringResource(R.string.badge_kind_feeder_name)
        "badge_master_builder_name" -> stringResource(R.string.badge_master_builder_name)
        "badge_life_saver_name" -> stringResource(R.string.badge_life_saver_name)
        "badge_health_guardian_name" -> stringResource(R.string.badge_health_guardian_name)
        "badge_hero_name" -> stringResource(R.string.badge_hero_name)
        else -> badge.name
    }
    
    val badgeDesc = when (badge.description) {
        "badge_first_steps_desc" -> stringResource(R.string.badge_first_steps_desc)
        "badge_kind_feeder_desc" -> stringResource(R.string.badge_kind_feeder_desc)
        "badge_master_builder_desc" -> stringResource(R.string.badge_master_builder_desc)
        "badge_life_saver_desc" -> stringResource(R.string.badge_life_saver_desc)
        "badge_health_guardian_desc" -> stringResource(R.string.badge_health_guardian_desc)
        "badge_hero_desc" -> stringResource(R.string.badge_hero_desc)
        else -> badge.description
    }

    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) LightPurpleBg else SoftGray.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (badge.isUnlocked) PastelPinkAccent else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (badge.isUnlocked) PastelPinkAccent.copy(alpha = 0.6f) else SoftGray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = badgeName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) TextDark else TextMuted
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = badgeDesc,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

@Composable
fun HistoryLogItem(
    log: LogEntry,
    onDelete: () -> Unit
) {
    // Resolve translated name if it's a key
    val activityName = when (log.activityName) {
        "activity_feed" -> stringResource(R.string.activity_feed)
        "activity_shelter" -> stringResource(R.string.activity_shelter)
        "activity_rescue" -> stringResource(R.string.activity_rescue)
        "activity_vet" -> stringResource(R.string.activity_vet)
        "activity_socialize" -> stringResource(R.string.activity_socialize)
        "activity_donate" -> stringResource(R.string.activity_donate)
        "activity_quiz" -> stringResource(R.string.activity_quiz)
        "activity_art" -> stringResource(R.string.activity_art)
        "activity_music" -> stringResource(R.string.activity_music)
        "activity_default" -> stringResource(R.string.activity_default)
        else -> log.activityName
    }

    val dateString = remember(log.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurpleBg.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, SoftGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = activityName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                    Box(
                        modifier = Modifier
                            .background(GreenSuccess, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${log.points}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                    }
                }
                if (log.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${log.notes}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextDark,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onDelete() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = RedError.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TinyPawsHelperScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.OmbreGradientBrushLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Semi-transparent Top Bar
            Surface(
                color = White.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = com.example.ui.theme.rememberHapticOnClick { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBurgundy)
                    }
                    Text(
                        text = stringResource(R.string.chat_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FrauncesFontFamily,
                            color = DeepBurgundy
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.chat_delete), tint = DeepBurgundy.copy(alpha = 0.7f))
                    }
                }
            }

            // Response Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.chat_placeholder),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Ink.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium,
                                fontFamily = QuicksandFontFamily
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                messages.forEach { message ->
                    val isUser = message.role == "user"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (isUser) DeepBurgundy else White.copy(alpha = 0.95f),
                            shape = if (isUser) RoundedCornerShape(24.dp, 24.dp, 4.dp, 24.dp) else RoundedCornerShape(24.dp, 24.dp, 24.dp, 4.dp),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (isUser) Cream else Ink,
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                        }
                    }
                }

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = BlushPink.copy(alpha = 0.9f),
                            shape = CircleShape,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🐾", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Thinking & licking paws...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DeepBurgundy,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = QuicksandFontFamily
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Glassy Input Field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White.copy(alpha = 0.85f),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { 
                            Text(
                                stringResource(R.string.chat_input_placeholder), 
                                style = MaterialTheme.typography.bodyLarge.copy(color = Ink.copy(alpha = 0.5f), fontFamily = QuicksandFontFamily)
                            ) 
                        },
                        modifier = Modifier.weight(1f)
                            .background(Color.Transparent),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Cream,
                            unfocusedContainerColor = Cream,
                            disabledContainerColor = Cream,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { 
                            if (inputText.isNotBlank() && !isLoading) {
                                viewModel.sendChatMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier.background(if (inputText.isNotBlank()) DeepBurgundy else Mauve, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, 
                            contentDescription = stringResource(R.string.chat_send),
                            tint = Cream
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FooterSection() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Heartwarming "Our Story" visual card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightPurpleBg),
            border = BorderStroke(1.5.dp, PastelPurplePrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.footer_label),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PastelPinkDark,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.footer_mission_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PastelPurpleDark
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.footer_mission_text),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.footer_app_name),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PastelPurpleDark
            )
        )
        Text(
            text = stringResource(R.string.footer_thank_you),
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        )
    }

}

