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
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ui.AuthScreen
import com.example.ui.Badge
import com.example.ui.FeedingMoodDialog
import com.example.data.DiaryEntry
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
import com.example.ui.PixelCard
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
        val catRepository = CatRepository(database.catProfileDao(), database.catWeightLogDao(), database.catCheckInLogDao(), database.reminderDao(), database.dailyCareLogDao(), database.catHistoryEntryDao())
        
        // Instantiate ViewModel
        val viewModel: TinyPawsViewModel by viewModels {
            TinyPawsViewModelFactory(application, repository, catRepository)
        }

        // Schedule periodic background weather alerts using WorkManager
        com.example.worker.WeatherAlertWorker.schedulePeriodicWeatherCheck(applicationContext)

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
            val userProfile by viewModel.firebaseUserProfile.collectAsStateWithLifecycle()
            val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()

            androidx.compose.runtime.LaunchedEffect(userProfile) {
                userProfile?.preferredLanguage?.let { preferred ->
                    if (preferred.isNotEmpty() && preferred != currentLang) {
                        sharedPrefs.edit().putString("user_lang", preferred).apply()
                        viewModel.setLanguage(preferred)
                        val appLocale = LocaleListCompat.forLanguageTags(preferred)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                ThemeProvider(currentLanguage = currentLang) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = PastelBlueBg,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                                viewModel.signOut()
                                viewModel.resetTriage()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
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
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currentLang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isExtremeWeatherNotify by viewModel.isExtremeWeatherNotify.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf("dashboard") } // "dashboard", "guide", "play", "find", "cook", "chat"
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // We need sharedPrefs to save dark mode
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    var hasSelectedLang by remember { mutableStateOf(sharedPrefs.getBoolean("has_selected_lang", false)) }
    var hasSeenIntro by remember { mutableStateOf(sharedPrefs.getBoolean("has_seen_intro", false)) }

    LaunchedEffect(Unit) {
        viewModel.reloadUser()
    }

    if (!hasSelectedLang) {
        com.example.ui.FirstLaunchLanguageScreen(
            currentLangCode = currentLang,
            onPreviewLanguage = { code ->
                onLanguageChange(code)
            },
            onLanguageSelected = { code ->
                onLanguageChange(code)
                sharedPrefs.edit().putBoolean("has_selected_lang", true).apply()
                hasSelectedLang = true
            }
        )
    } else if (user == null && onboardedName.isEmpty()) {
        AuthScreen(
            viewModel = viewModel, 
            onAuthSuccess = { /* user flow continues */ },
            onGuestEntry = { name -> onSaveName(name) }
        )
    } else if (!hasSeenIntro) {
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
                                    text = stringResource(R.string.drawer_ai_chatbot),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                ) 
                            },
                            selected = currentScreen == "chat",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                currentScreen = "chat"
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .height(56.dp)
                                .testTag("drawer_chat_item"),
                            shape = CircleShape,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DeepBurgundy,
                                unselectedContainerColor = BlushPink.copy(alpha = 0.25f),
                                selectedTextColor = Cream,
                                unselectedTextColor = Ink
                            )
                        )
                        NavigationDrawerItem(
                            label = { 
                                Text(
                                    text = stringResource(R.string.drawer_rescue_profile),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                ) 
                            },
                            selected = currentScreen == "profile",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                currentScreen = "profile"
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .height(56.dp)
                                .testTag("drawer_profile_item"),
                            shape = CircleShape,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DeepBurgundy,
                                unselectedContainerColor = BlushPink.copy(alpha = 0.25f),
                                selectedTextColor = Cream,
                                unselectedTextColor = Ink
                            )
                        )
                        NavigationDrawerItem(
                            label = { 
                                Text(
                                    text = stringResource(R.string.drawer_discover_tinypaws),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                ) 
                            },
                            selected = false,
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                val result = viewModel.selectRandomSurprise(sharedPrefs)
                                val tab = result.first
                                val projectId = result.second
                                viewModel.updateActiveGuideTab(tab)
                                viewModel.updateActiveDiyProject(projectId)
                                currentScreen = "guide"
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .height(56.dp)
                                .testTag("drawer_discover_item"),
                            shape = CircleShape,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = DeepBurgundy,
                                unselectedContainerColor = BlushPink.copy(alpha = 0.25f),
                                selectedTextColor = Cream,
                                unselectedTextColor = Ink
                            )
                        )
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
                    viewModel = viewModel,
                    onNavigate = { currentScreen = it },
                    onBack = { currentScreen = "dashboard" },
                    modifier = modifier
                )
                "my_cat_profile" -> MyCatScreen(
                    viewModel = viewModel,
                    mode = "profile",
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "weight_tracker" -> MyCatScreen(
                    viewModel = viewModel,
                    mode = "weight",
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "data_sync" -> com.example.ui.DataSyncScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "daily_checklist" -> com.example.ui.DailyChecklistScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "daily_checkin" -> com.example.ui.DiaryFeedScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "diary_feed" -> com.example.ui.DiaryFeedScreen(
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
                "rescue_stories" -> com.example.ui.RescueStoriesScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" }
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
                    initialCategory = "all",
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "vet_reminders" -> ReminderScreen(
                    viewModel = viewModel,
                    initialCategory = "vet_visit",
                    onBack = { currentScreen = "my_cat_hub" },
                    modifier = modifier
                )
                "care_reminders" -> ReminderScreen(
                    viewModel = viewModel,
                    initialCategory = "general",
                    onBack = { currentScreen = "my_cat_hub" },
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
                "profile", "user_profile" -> com.example.ui.UserProfileScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "dashboard" }
                )
                "community_tracker" -> {
                    val trackerState by viewModel.trackerUiState.collectAsStateWithLifecycle()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (com.example.ui.theme.LocalIsDarkMode.current) com.example.ui.theme.OmbreGradientBrushDark else com.example.ui.theme.OmbreGradientBrushLight)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick { currentScreen = "dashboard" },
                                modifier = Modifier.testTag("tracker_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back_btn),
                                    tint = DeepBurgundy
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.tracker_header),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBurgundy
                                )
                            )
                        }
                        
                        CommunityImpactTrackerCard(
                            state = trackerState,
                            onLogActivity = { type, notes -> viewModel.logActivity(type, notes) },
                            onDeleteLog = { id -> viewModel.deleteLog(id) },
                            onClearLogs = { viewModel.clearAllLogs() }
                        )
                    }
                }
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
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PixelCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            emblemType = "paw"
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
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
    val diaryLogs by viewModel.allCheckInLogs.collectAsStateWithLifecycle()
    val globalStats by viewModel.globalStats.collectAsStateWithLifecycle()
    val userProfile by viewModel.firebaseUserProfile.collectAsStateWithLifecycle()

    var showQuickLogDialog by remember { mutableStateOf(false) }
    var isGuestBannerDismissed by remember { mutableStateOf(false) }
    val firebaseAuthUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGuest = firebaseAuthUser?.isAnonymous == true

    val isEmailVerified = firebaseAuthUser?.isEmailVerified == true
    var isVerificationBannerDismissed by remember { mutableStateOf(false) }
    var resendVerificationMessage by remember { mutableStateOf<String?>(null) }
    val resendCooldownSeconds by viewModel.resendCooldownSeconds.collectAsStateWithLifecycle()
    val currentActivityContext = androidx.compose.ui.platform.LocalContext.current

    if (showQuickLogDialog) {
        FeedingMoodDialog(
            onDismiss = { showQuickLogDialog = false },
            onSave = { type, notes ->
                viewModel.saveCheckInLog(
                    date = System.currentTimeMillis(),
                    mood = if (type == "Mood") notes else "happy",
                    notes = if (type == "Feeding") "Feeding: $notes" else notes,
                    diaryEntryType = type.lowercase()
                )
                showQuickLogDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExtendedFloatingActionButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("chat") },
                    containerColor = DeepBurgundy,
                    contentColor = Cream,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Text("🤖", fontSize = 18.sp) },
                    text = { Text(stringResource(R.string.ai_helper_fab_text), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("ai_helper_fab")
                )
                FloatingActionButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick { showQuickLogDialog = true },
                    containerColor = BlushPink,
                    contentColor = DeepBurgundy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("quick_log_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Quick Log")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroHeader(
                    streakCount = streakCount,
                    userName = userName,
                    onLogout = onLogout,
                    onMenuClick = onMenuClick,
                    globalStats = globalStats
                )
            }

            if (isGuest && !isGuestBannerDismissed) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFADCE0)),
                        border = BorderStroke(1.dp, Color(0xFFE57373))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Guest Warning",
                                        tint = Color(0xFFC62828)
                                    )
                                    Text(
                                        text = "Guest Mode Warning ⚠️",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF601827),
                                            fontFamily = QuicksandFontFamily
                                        )
                                    )
                                }
                                IconButton(onClick = { isGuestBannerDismissed = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF601827)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.guest_warning_banner),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF2D1B1E),
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onLogout() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF601827)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = stringResource(R.string.guest_upgrade_btn),
                                    color = Color(0xFFFFF0F2),
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!isGuest && firebaseAuthUser != null && !isEmailVerified && !isVerificationBannerDismissed) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Unverified Email Alert",
                                        tint = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = stringResource(R.string.auth_email_unverified_status),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            fontFamily = QuicksandFontFamily
                                        )
                                    )
                                }
                                IconButton(onClick = { isVerificationBannerDismissed = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFFE65100)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.auth_verification_sent_msg),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF5D4037),
                                    fontFamily = QuicksandFontFamily
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            resendVerificationMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF2E7D32),
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.checkEmailVerificationStatus(currentActivityContext) { success, msg ->
                                            resendVerificationMessage = msg
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, DeepBurgundy)
                                ) {
                                    Text(
                                        text = stringResource(R.string.auth_check_verification),
                                        color = DeepBurgundy,
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.resendVerificationEmail(currentActivityContext) { success, msg ->
                                            resendVerificationMessage = msg
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = resendCooldownSeconds == 0
                                ) {
                                    Text(
                                        text = if (resendCooldownSeconds > 0)
                                            stringResource(R.string.auth_resend_cooldown, resendCooldownSeconds)
                                        else
                                            stringResource(R.string.auth_resend_verification),
                                        color = Cream,
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                TipOfTheDayCard()
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                SurpriseMeSection(viewModel = viewModel, onNavigate = onNavigate)
            }

            item {
                Text(
                    text = stringResource(R.string.diary_room_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DeepBurgundy,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FrauncesFontFamily
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                NavigationGridSection(onNavigate = onNavigate)
            }

            item {
                Text(
                    text = stringResource(R.string.help_stray_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DeepBurgundy,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FrauncesFontFamily
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 1. Cats Near Me
            item {
                NavigationCard(
                    title = stringResource(R.string.nav_cats_near_me_title),
                    subtitle = stringResource(R.string.cats_near_me_subtitle),
                    backgroundColor = Color(0xFFE8F5E9), // Soft green
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("nav_cats_near_me_card"),
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        viewModel.updateCatsNearMeTab("browse")
                        onNavigate("cats_near_me")
                    }
                )
            }

            // 2. Heatwave & Weather Tracker
            item {
                NavigationCard(
                    title = stringResource(R.string.nav_weather_title),
                    subtitle = stringResource(R.string.nav_weather_subtitle),
                    backgroundColor = Color(0xFFFFF3E0), // Soft warm peach
                    accentColor = Color(0xFFD32F2F), // Red
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("nav_weather_card"),
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        onNavigate("weather")
                    }
                )
            }

            // 3. Feeding Stations Near Me
            item {
                NavigationCard(
                    title = stringResource(R.string.nav_feeding_stations_title),
                    subtitle = stringResource(R.string.feeding_stations_subtitle),
                    backgroundColor = Color(0xFFE3F2FD), // Soft blue
                    accentColor = Color(0xFF1565C0), // Deep blue
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("nav_feeding_stations_card"),
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        onNavigate("feeding_stations")
                    }
                )
            }

            // 4. Did You Find a Cat? (Triage Guide Card)
            item {
                TriageGuideCard(
                    step = triageStep,
                    hasInjuries = hasInjuries,
                    catAgeGroup = catAgeGroup,
                    onStart = { viewModel.startTriage() },
                    onSelectInjuries = { injured -> viewModel.selectInjuries(injured) },
                    onSelectAge = { age -> viewModel.selectAgeGroup(age) },
                    onReset = { viewModel.resetTriage() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 5. Community Care Tracker
            item {
                NavigationCard(
                    title = stringResource(R.string.community_care_tracker_title),
                    subtitle = stringResource(R.string.community_care_tracker_subtitle),
                    backgroundColor = Color(0xFFF3E5F5), // Soft lavender/pink
                    accentColor = Color(0xFF7B1FA2), // Deep purple / burgundy
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("nav_community_care_tracker_card"),
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        onNavigate("community_tracker")
                    }
                )
            }
            
            item {
                Text(
                    text = "🎀 " + stringResource(R.string.main_my_kitty_corner) + " 🐾",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DeepBurgundy,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FrauncesFontFamily
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            item {
                MyKittysCornerGrid(onNavigate = onNavigate)
            }

            item {
                val isDark = LocalIsDarkMode.current
                val missionGradient = Brush.linearGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0xFF3D151D).copy(alpha = 0.75f),
                            Color(0xFF23112E).copy(alpha = 0.75f)
                        )
                    } else {
                        listOf(
                            Color(0xFFFFECEF).copy(alpha = 0.85f),
                            Color(0xFFF3EAF6).copy(alpha = 0.85f)
                        )
                    }
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .glassyCard(shape = RoundedCornerShape(26.dp))
                        .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { }),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(missionGradient)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.about_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepBurgundy,
                                fontSize = 20.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.about_subtitle),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = Wine,
                                letterSpacing = 1.2.sp,
                                fontSize = 12.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🐾🤍",
                            fontSize = 24.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.about_mission_header),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepBurgundy,
                                fontSize = 16.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.about_mission_desc),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = QuicksandFontFamily,
                                color = Ink.copy(alpha = 0.85f),
                                lineHeight = 20.sp,
                                fontSize = 14.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.about_creator_note),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepBurgundy,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.about_final_note),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = QuicksandFontFamily,
                                color = Ink.copy(alpha = 0.85f),
                                fontSize = 13.5.sp,
                                lineHeight = 18.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TipOfTheDayCard() {

    val facts = androidx.compose.ui.res.stringArrayResource(id = R.array.cat_facts)
    val fact = remember { facts.random() }

    com.example.ui.PixelCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        backgroundColor = BlushPink.copy(alpha = 0.25f),
        emblemType = "paw"
    ) {
        Text(stringResource(R.string.tip_of_day_title), fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy)
        Spacer(modifier = Modifier.height(6.dp))
        Text(fact, fontFamily = QuicksandFontFamily, color = Ink, fontSize = 14.sp, lineHeight = 20.sp)
    }
}
@Composable
fun SurpriseMeSection(
    viewModel: TinyPawsViewModel,
    onNavigate: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    com.example.ui.PixelCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        emblemType = "star"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            com.example.ui.TactileButton(
                onClick = { 
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
                containerColor = DeepBurgundy,
                contentColor = Cream
            ) {
                Icon(Icons.Default.Star, contentDescription = "Surprise Me", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CatsNearMeDashboardSection(
    viewModel: TinyPawsViewModel,
    onNavigate: (String) -> Unit
) {
    com.example.ui.PixelCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        emblemType = "paw"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
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
                // Button 1: Report Sighting
                com.example.ui.TactileButton(
                    onClick = { 
                        viewModel.updateCatsNearMeTab("report")
                        onNavigate("cats_near_me")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_report_stray_btn"),
                    containerColor = DeepBurgundy,
                    contentColor = Cream
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

                // Button 2: Browse Nearby
                com.example.ui.TactileButton(
                    onClick = { 
                        viewModel.updateCatsNearMeTab("browse")
                        onNavigate("cats_near_me")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("home_browse_strays_btn"),
                    containerColor = PastelLightPink,
                    contentColor = DeepBurgundy
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
    onMenuClick: () -> Unit,
    globalStats: com.example.data.GlobalStatistics? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        // Cozy personal story illustration featuring the user with their beloved calico torbie cat on lap
        Image(
            painter = painterResource(id = R.drawable.img_hero_person_with_lap_cat_1786905991525),
            contentDescription = "Warm cozy personal illustration of sitting comfortably with cat on lap",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Soft gradient overlay to blend beautifully into the background and ensure high contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.45f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Top Control Bar: Hamburger Menu (Left) and Logout Button (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onMenuClick() },
                modifier = Modifier
                    .testTag("home_hamburger_btn")
                    .size(46.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = DeepBurgundy,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onLogout() },
                modifier = Modifier
                    .testTag("home_logout_btn")
                    .size(46.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                Text(
                    text = "🚪",
                    fontSize = 22.sp
                )
            }
        }

        // Bottom Content: Greeting & Streak (overlaid directly on the image with perfect contrast)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_greeting, userName),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            blurRadius = 8f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.PixelCanvas(
                        pixelGrid = com.example.ui.PixelCatModels.Star,
                        colorMap = com.example.ui.PixelColorMap,
                        pixelSize = 1.2.dp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = stringResource(R.string.home_streak, streakCount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.6f),
                                blurRadius = 8f
                            )
                        )
                    )
                }
            }

            // Super cute animated orange cat sitting and blinking next to the greeting card!
            Box(
                modifier = Modifier
                    .padding(end = 4.dp, bottom = 4.dp)
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.PixelCatAnimated(pixelSize = 3.2.dp)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: Int, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 18.sp)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall.copy(
                color = DeepBurgundy,
                fontWeight = FontWeight.Bold,
                fontFamily = FrauncesFontFamily
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = DeepBurgundy.copy(alpha = 0.6f),
                fontFamily = QuicksandFontFamily
            )
        )
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
fun MyKittysCornerGrid(
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onNavigate("my_cat_hub") }
            .testTag("nav_my_cat_hub_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BlushPink.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(DeepBurgundy.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎀", fontSize = 28.sp)
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.step_into_cat_universe),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DeepBurgundy,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FrauncesFontFamily
                    )
                )
                Text(
                    text = stringResource(R.string.step_into_cat_universe_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Ink.copy(alpha = 0.75f),
                        fontFamily = QuicksandFontFamily
                    )
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go",
                tint = DeepBurgundy,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun KittyActionChip(
    title: String,
    subtitle: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { onClick() }),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = QuicksandFontFamily
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.8f),
                    fontFamily = QuicksandFontFamily
                )
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
    val pixelModelGrid = when {
        title.contains("Guide", ignoreCase = true) || title.contains("Care", ignoreCase = true) -> com.example.ui.PixelCatModels.Book
        title.contains("Play", ignoreCase = true) || title.contains("Quiz", ignoreCase = true) || title.contains("Game", ignoreCase = true) -> com.example.ui.PixelCatModels.Trophy
        title.contains("Find", ignoreCase = true) || title.contains("Vet", ignoreCase = true) || title.contains("Map", ignoreCase = true) -> com.example.ui.PixelCatModels.MapMarker
        title.contains("Cook", ignoreCase = true) || title.contains("Recipe", ignoreCase = true) -> com.example.ui.PixelCatModels.FoodBowl
        else -> null
    }

    Card(
        modifier = modifier
            .height(130.dp)
            .glassyCard(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { onClick() }),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background pixel decoration
            if (pixelModelGrid != null) {
                com.example.ui.PixelCanvas(
                    pixelGrid = pixelModelGrid,
                    colorMap = com.example.ui.PixelColorMap,
                    pixelSize = 1.8.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .scale(0.85f)
                )
            }

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
                    ),
                    modifier = Modifier.padding(end = 24.dp) // Leave room for the emblem
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

    LaunchedEffect(messages.size, isLoading) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val topColor = Cream
    val mid1Color = BlushPink.copy(alpha = 0.35f)
    val mid2Color = PastelLavender.copy(alpha = 0.45f)
    val cozyBackgroundBrush = remember(topColor, mid1Color, mid2Color) {
        Brush.verticalGradient(
            colors = listOf(
                topColor,
                mid1Color,
                mid2Color,
                topColor
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Atmospheric Cozy Room Background Wallpaper
        Image(
            painter = painterResource(id = R.drawable.img_chat_cozy_room_bg_1786906005537),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.28f
        )

        // Soft pastel gradient overlay ensuring 100% text readability & warmth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Cream.copy(alpha = 0.85f),
                            PastelLightPink.copy(alpha = 0.70f),
                            PastelLavender.copy(alpha = 0.75f),
                            Cream.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Semi-transparent Cozy Top Bar
            Surface(
                color = White.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = com.example.ui.theme.rememberHapticOnClick { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.chat_back), tint = DeepBurgundy)
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
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            com.example.ui.PixelCanvas(
                                pixelGrid = com.example.ui.PixelCatModels.AIHead,
                                colorMap = com.example.ui.PixelColorMap,
                                pixelSize = 3.8.dp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                            Text(
                                text = stringResource(R.string.chat_placeholder),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Ink.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = QuicksandFontFamily
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
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
                    val infiniteTransition = rememberInfiniteTransition(label = "thinking_anim")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.55f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "thinking_alpha"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = BlushPink.copy(alpha = pulseAlpha),
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
                                    text = stringResource(R.string.chat_thinking),
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

            // Glassy Input Field container with smooth IME handling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = White.copy(alpha = 0.9f),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        modifier = Modifier
                            .weight(1f)
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
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    com.example.ui.TactileButton(
                        onClick = { 
                            if (inputText.isNotBlank()) {
                                val textToSend = inputText
                                inputText = ""
                                viewModel.sendChatMessage(textToSend)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button"),
                        containerColor = DeepBurgundy,
                        contentColor = Cream,
                        enabled = inputText.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.chat_send),
                            tint = if (inputText.isNotBlank() && !isLoading) Cream else Cream.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
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

