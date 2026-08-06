package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppDatabase
import com.example.data.GeminiClient
import com.example.data.LogEntry
import com.example.data.LogRepository
import com.example.data.NearbyPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import java.io.File
import com.example.data.FavoriteDiy
import com.example.data.StrayReport
import com.example.data.StrayReportRepository
import com.example.data.CatProfile
import com.example.data.CatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.data.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

// Chat Message Model
data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val isPending: Boolean = false
)

// Badge Model
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean
)

// UI State representing the Game/Tracker state
data class TrackerUiState(
    val logs: List<LogEntry> = emptyList(),
    val totalPoints: Int = 0,
    val rankResId: Int = R.string.rank_kitten,
    val pointsToNextRank: Int = 100,
    val nextRankResId: Int = R.string.rank_feline,
    val progressPercentage: Float = 0.0f,
    val badges: List<Badge> = emptyList()
)

class TinyPawsViewModel(
    application: Application,
    private val repository: LogRepository,
    private val catRepository: CatRepository,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository(),
    private val reportRepository: FirestoreReportRepository = FirestoreReportRepository(),
    private val stationRepository: FirestoreFeedingStationRepository = FirestoreFeedingStationRepository()
) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // Firestore States with Loading
    private val _isReportsLoading = MutableStateFlow(true)
    val isReportsLoading = _isReportsLoading.asStateFlow()

    val activeReports: StateFlow<List<CatReport>> = reportRepository.getActiveReports()
        .map { 
            _isReportsLoading.value = false
            it 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isFeedingStationsLoading = MutableStateFlow(true)
    val isFeedingStationsLoading = _isFeedingStationsLoading.asStateFlow()

    val feedingStations: StateFlow<List<FeedingStation>> = stationRepository.getFeedingStations()
        .map {
            _isFeedingStationsLoading.value = false
            it
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRescueStoriesLoading = MutableStateFlow(true)
    val isRescueStoriesLoading = _isRescueStoriesLoading.asStateFlow()

    val rescueStories: StateFlow<List<CatReport>> = reportRepository.getRescueStories()
        .map {
            _isRescueStoriesLoading.value = false
            it
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalStats: StateFlow<GlobalStatistics?> = firebaseRepository.getGlobalStatistics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val firebaseUserProfile: StateFlow<UserProfile?> = user.flatMapLatest { 
        if (it != null) firebaseRepository.getUserProfile(it.uid) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Firestore Actions
    fun createReport(description: String, latitude: Double, longitude: Double, photoUrl: String, needs: String = "") {
        viewModelScope.launch {
            reportRepository.createReport(
                CatReport(
                    latitude = latitude,
                    longitude = longitude,
                    description = description,
                    photoUrl = photoUrl,
                    needs = needs
                )
            )
        }
    }

    fun createFeedingStation(description: String, latitude: Double, longitude: Double, photoUrl: String) {
        viewModelScope.launch {
            stationRepository.createFeedingStation(
                FeedingStation(
                    latitude = latitude,
                    longitude = longitude,
                    description = description,
                    photoUrl = photoUrl
                )
            )
        }
    }

    fun reachCat(reportId: String) {
        viewModelScope.launch { reportRepository.reachCat(reportId) }
    }

    fun reachFeedingStation(stationId: String, foodAvailable: Boolean) {
        viewModelScope.launch { stationRepository.reachFeedingStation(stationId, foodAvailable) }
    }

    fun markAsRescued(reportId: String, photoUrl: String, desc: String) {
        viewModelScope.launch { reportRepository.markAsRescued(reportId, photoUrl, desc) }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message ?: "Authentication failed")
                }
            }
    }

    fun signUp(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    private val sharedPrefs = application.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)

    // Cat Profile State
    val catProfile: StateFlow<CatProfile?> = catRepository.catProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun saveCatProfile(name: String, ageYears: Int, ageMonths: Int, coatColor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.saveProfile(CatProfile(name = name, ageYears = ageYears, ageMonths = ageMonths, coatColor = coatColor))
        }
    }

    // Cat Weight Logs State
    val allWeightLogs: StateFlow<List<com.example.data.CatWeightLog>> = catRepository.allWeightLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveWeightLog(date: Long, weight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.saveWeightLog(date, weight)
        }
    }

    fun deleteWeightLog(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.deleteWeightLog(id)
        }
    }

    // Cat Check-In Logs State (Firestore)
    private val _isDiaryLoading = MutableStateFlow(true)
    val isDiaryLoading = _isDiaryLoading.asStateFlow()

    val allCheckInLogs: StateFlow<List<DiaryEntry>> = firebaseRepository.getDiaryEntries()
        .map {
            _isDiaryLoading.value = false
            it
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCareLogs: StateFlow<List<com.example.data.DailyCareLog>> = catRepository.allCareLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _cloudBackupState = MutableStateFlow<String?>(null)
    val cloudBackupState = _cloudBackupState.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing = _isCloudSyncing.asStateFlow()

    fun backupDataToCloud(onResult: (String) -> Unit) {
        if (_isCloudSyncing.value) return
        _isCloudSyncing.value = true
        viewModelScope.launch {
            val res = com.example.data.FirestoreBackupHelper.backupDataToCloud(
                profile = catProfile.value,
                careLogs = allCareLogs.value,
                weightLogs = allWeightLogs.value,
                diaryLogs = allCheckInLogs.value,
                reminders = allReminders.value
            )
            _isCloudSyncing.value = false
            val message = res.getOrElse { "Cloud backup failed: ${it.localizedMessage}" }
            _cloudBackupState.value = message
            onResult(message)
        }
    }

    fun restoreDataFromCloud(onResult: (String) -> Unit) {
        if (_isCloudSyncing.value) return
        _isCloudSyncing.value = true
        viewModelScope.launch {
            val res = com.example.data.FirestoreBackupHelper.restoreDataFromCloud(catRepository)
            _isCloudSyncing.value = false
            val message = res.getOrElse { "Cloud restore failed: ${it.localizedMessage}" }
            _cloudBackupState.value = message
            onResult(message)
        }
    }

    fun getCareLogForDate(dateStr: String): StateFlow<com.example.data.DailyCareLog?> {
        return catRepository.getCareLogForDate(dateStr)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun saveCareLog(careLog: com.example.data.DailyCareLog) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.saveCareLog(careLog)
        }
    }

    fun saveCheckInLog(
        date: Long,
        mood: String,
        notes: String,
        weight: Float? = null,
        photos: String = "",
        diaryEntryType: String = "general",
        reminderTimeMillis: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.saveDiaryEntry(
                DiaryEntry(
                    date = date,
                    mood = mood,
                    notes = notes,
                    weight = weight,
                    photos = photos,
                    diaryEntryType = diaryEntryType
                )
            )
        }
    }

    fun deleteCheckInLog(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.deleteDiaryEntry(entryId)
        }
    }

    // Reminders State
    val allReminders: StateFlow<List<com.example.data.Reminder>> = catRepository.allReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveReminder(title: String, timeMillis: Long, type: String = "general") {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.saveReminder(title, timeMillis, type)
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.deleteReminder(id)
        }
    }

    // Tracks recently viewed guide sections and DIY projects to avoid repeating them in Surprise Me

    // Favorites State
    val favoriteDiyIds: StateFlow<Set<String>> = repository.allFavorites
        .map { list -> list.map { it.projectId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun toggleFavoriteDiy(projectId: String) {
        val isFav = favoriteDiyIds.value.contains(projectId)
        viewModelScope.launch {
            repository.toggleFavorite(projectId, !isFav)
        }
    }

    // Connectivity state flow
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun startNetworkMonitoring(context: Context) {
        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager = cm
        if (cm == null) {
            _isOnline.value = true
            return
        }

        // Initial check
        _isOnline.value = isCurrentNetworkActive(cm)

        // Dynamic network callback registration
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = isCurrentNetworkActive(cm)
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isOnline.value = hasInternet
                }
            }
            networkCallback = callback
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.e("TinyPawsViewModel", "Failed to register network callback", e)
            _isOnline.value = isCurrentNetworkActive(cm)
        }
    }

    fun stopNetworkMonitoring() {
        try {
            networkCallback?.let {
                connectivityManager?.unregisterNetworkCallback(it)
            }
        } catch (e: Exception) {
            Log.e("TinyPawsViewModel", "Failed to unregister network callback", e)
        } finally {
            networkCallback = null
            connectivityManager = null
        }
    }


    private fun isCurrentNetworkActive(cm: ConnectivityManager): Boolean {
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Onboarded Name State
    private val _onboardedName = MutableStateFlow("")
    val onboardedName = _onboardedName.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _streak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val streak = _streak.asStateFlow()

    fun setStreak(s: Int) {
        _streak.value = s
    }

    private val _isDarkMode = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
    }

    private val _isExtremeWeatherNotify = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isExtremeWeatherNotify = _isExtremeWeatherNotify.asStateFlow()

    fun setExtremeWeatherNotify(notify: Boolean) {
        _isExtremeWeatherNotify.value = notify
    }

    fun updateOnboardedName(name: String) {
        _onboardedName.value = name
    }

    fun setLanguage(langCode: String) {
        _currentLanguage.value = langCode
    }

    // 1. Triage Wizard States
    private val _triageStep = MutableStateFlow(0) // 0: Start, 1: Injury Check, 2: Age Check, 3: Next Steps
    val triageStep = _triageStep.asStateFlow()

    private val _hasInjuries = MutableStateFlow<Boolean?>(null)
    val hasInjuries = _hasInjuries.asStateFlow()

    private val _catAgeGroup = MutableStateFlow<String?>(null) // "baby", "young", "adult"
    val catAgeGroup = _catAgeGroup.asStateFlow()

    // 2. Growth Journey Game States (Persisted in SharedPreferences)
    private val _currentIsland = MutableStateFlow(sharedPrefs.getInt("quiz_current_island", 0)) // 0 to 4 (Islands 1 to 5)
    val currentIsland = _currentIsland.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0) // 0 to 19
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _quizCompletedOnCurrentIsland = MutableStateFlow(sharedPrefs.getBoolean("quiz_completed_on_current", false))
    val quizCompletedOnCurrentIsland = _quizCompletedOnCurrentIsland.asStateFlow()

    private val _completedIslands = MutableStateFlow<Set<Int>>(
        sharedPrefs.getString("quiz_completed_islands", "")?.takeIf { it.isNotEmpty() }
            ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    )
    val completedIslands = _completedIslands.asStateFlow()

    val allQuizzesCompleted = _completedIslands.map { it.size == 5 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _scoreOnCurrentIsland = MutableStateFlow(sharedPrefs.getInt("quiz_score_on_current", 0))
    val scoreOnCurrentIsland = _scoreOnCurrentIsland.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // index -> selectedOptionIndex
    val selectedAnswers = _selectedAnswers.asStateFlow()

    fun selectAnswer(questionIdx: Int, optionIdx: Int) {
        val updated = _selectedAnswers.value.toMutableMap()
        updated[questionIdx] = optionIdx
        _selectedAnswers.value = updated
    }

    fun submitQuizForCurrentIsland(correctCount: Int) {
        _scoreOnCurrentIsland.value = correctCount
        _quizCompletedOnCurrentIsland.value = true
        val updated = _completedIslands.value + _currentIsland.value
        _completedIslands.value = updated
        sharedPrefs.edit()
            .putInt("quiz_score_on_current", correctCount)
            .putBoolean("quiz_completed_on_current", true)
            .putString("quiz_completed_islands", updated.joinToString(","))
            .apply()
        logActivity("complete_quiz", "Completed Level ${_currentIsland.value + 1} Quiz (Score: $correctCount/10)")
    }

    fun nextIslandWithAnimation(onTriggerAnimation: () -> Unit) {
        if (_currentIsland.value < 4) {
            onTriggerAnimation()
            val next = _currentIsland.value + 1
            _currentIsland.value = next
            _currentQuestionIndex.value = 0
            _quizCompletedOnCurrentIsland.value = false
            _scoreOnCurrentIsland.value = 0
            _selectedAnswers.value = emptyMap()
            sharedPrefs.edit()
                .putInt("quiz_current_island", next)
                .putBoolean("quiz_completed_on_current", false)
                .putInt("quiz_score_on_current", 0)
                .apply()
        }
    }

    fun jumpToIsland(islandIndex: Int) {
        if (islandIndex in 0..4) {
            _currentIsland.value = islandIndex
            _currentQuestionIndex.value = 0
            _quizCompletedOnCurrentIsland.value = false
            _scoreOnCurrentIsland.value = 0
            _selectedAnswers.value = emptyMap()
            sharedPrefs.edit()
                .putInt("quiz_current_island", islandIndex)
                .putBoolean("quiz_completed_on_current", false)
                .putInt("quiz_score_on_current", 0)
                .apply()
        }
    }

    fun resetGameProgress() {
        _currentIsland.value = 0
        _currentQuestionIndex.value = 0
        _quizCompletedOnCurrentIsland.value = false
        _scoreOnCurrentIsland.value = 0
        _selectedAnswers.value = emptyMap()
        _completedIslands.value = emptySet()
        sharedPrefs.edit()
            .putInt("quiz_current_island", 0)
            .putBoolean("quiz_completed_on_current", false)
            .putInt("quiz_score_on_current", 0)
            .putString("quiz_completed_islands", "")
            .apply()
    }

    // 3. Location Search States
    private val _locationQuery = MutableStateFlow("")
    val locationQuery = _locationQuery.asStateFlow()

    private val _searchCategory = MutableStateFlow("vets") // "vets", "shops", "shelters"
    val searchCategory = _searchCategory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<NearbyPlace>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>>(Pair(36.8065, 10.1815)) // Default Tunis, Tunisia
    val userLocation = _userLocation.asStateFlow()

    fun updateLocationQuery(query: String) {
        _locationQuery.value = query
    }

    fun updateSearchCategory(diaryEntryType: String) {
        _searchCategory.value = diaryEntryType
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    fun getDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return com.example.util.LocationUtils.calculateHaversineDistanceKm(lat1, lon1, lat2, lon2)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return com.example.util.LocationUtils.calculateHaversineDistanceKm(lat1, lon1, lat2, lon2)
    }

    fun searchPlaces(diaryEntryType: String, query: String) {
        _searchCategory.value = diaryEntryType
        _isSearching.value = true
        viewModelScope.launch {
            try {
                val results = GeminiClient.searchNearbyPlaces(diaryEntryType, query)
                val userLoc = _userLocation.value
                val mappedResults = results.map { place ->
                    val dist = calculateDistance(userLoc.first, userLoc.second, place.latitude, place.longitude)
                    place.copy(distance = dist)
                }
                // Sort by distance (Nearest to Farthest)
                _searchResults.value = mappedResults.sortedBy { it.distance }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    // 4. AI Studio Generation States
    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap = _generatedBitmap.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    // Runtime Step Image Cache
    private val _stepImageCache = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val stepImageCache = _stepImageCache.asStateFlow()

    fun generateStepImage(stepId: String, prompt: String) {
        if (_stepImageCache.value.containsKey(stepId)) return

        viewModelScope.launch {
            try {
                val bitmap = GeminiClient.generateImage(prompt, isProModel = false, imageSize = "1K")
                if (bitmap != null) {
                    val updatedCache = _stepImageCache.value.toMutableMap()
                    updatedCache[stepId] = bitmap
                    _stepImageCache.value = updatedCache
                }
            } catch (e: Exception) {
                Log.e("TinyPawsViewModel", "Error generating step image", e)
            }
        }
    }

    private val _generatedMusicFile = MutableStateFlow<File?>(null)
    val generatedMusicFile = _generatedMusicFile.asStateFlow()

    private val _isGeneratingMusic = MutableStateFlow(false)
    val isGeneratingMusic = _isGeneratingMusic.asStateFlow()

    fun generateCatArt(prompt: String, isPro: Boolean, size: String) {
        _isGeneratingImage.value = true
        _generatedBitmap.value = null
        viewModelScope.launch {
            try {
                val bitmap = GeminiClient.generateImage(prompt, isPro, size)
                _generatedBitmap.value = bitmap
                if (bitmap != null) {
                    logActivity("generate_art", "Generated custom cat art using AI Studio")
                }
            } catch (e: Exception) {
                _generatedBitmap.value = null
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    fun generateKittenMusic(context: Application, prompt: String, isFullTrack: Boolean) {
        _isGeneratingMusic.value = true
        _generatedMusicFile.value = null
        viewModelScope.launch {
            try {
                val file = GeminiClient.generateMusic(context, prompt, isFullTrack)
                _generatedMusicFile.value = file
                if (file != null) {
                    logActivity("generate_music", "Generated custom ambient kitten track using Lyria")
                }
            } catch (e: Exception) {
                _generatedMusicFile.value = null
            } finally {
                _isGeneratingMusic.value = false
            }
        }
    }

    // 5. Cat Researcher Chat States
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isChatLoading.value) return
        
        val userMessage = ChatMessage("user", text)
        _chatMessages.value = _chatMessages.value + userMessage
        
        val modelMessage = ChatMessage("model", "", isPending = true)
        _chatMessages.value = _chatMessages.value + modelMessage
        
        val systemInstruction = """
            You are the TinyPaws Helper — a chill, funny Gen Z friend who knows their stuff about animal rescue. 
            Talk casual, warm, and playful. Use natural slang sparingly. 
            No corporate speak. No vet textbook vibes. 
            
            Language: Respond in the following language: ${_currentLanguage.value}.
            
            Scope: Answer questions about stray animal care (feeding, shelter, first aid, TNR, rescue etiquette). 
            Give solid practical advice. Mention a source briefly if relevant (e.g., "ASPCA says...").
            
            PERSONALITY:
            - If the user tells you their name, remember it and use it naturally (e.g., "Good question, [name]!").
            - Use emojis to add warmth, but sparingly (max 1-2 per message).
            
            BOUNDARIES: 
            - Keep answers short (a few sentences max). 
            - If it's a real emergency (badly hurt or aggressive animal), tell them to use the app's Emergency Report feature or contact a local vet. 
            - If asked about illegal topics, adult content, self-harm, or violence, respond warmly and redirect: "That's not really my thing — I'm just here to help with cats and stray animal care! Got a question about that? 🐾"
            - Stay on topic: stray animal care and TinyPaws.
        """.trimIndent()
        
        val history = _chatMessages.value.filter { !it.isPending }.map { it.role to it.text }

        _isChatLoading.value = true
        viewModelScope.launch {
            var fullResponse = ""
            com.example.data.GroqClient.chatStream(systemInstruction, history) { chunk ->
                fullResponse += chunk
                val currentMessages = _chatMessages.value.toMutableList()
                if (currentMessages.isNotEmpty()) {
                    val lastIdx = currentMessages.lastIndex
                    currentMessages[lastIdx] = ChatMessage("model", fullResponse, isPending = false)
                    _chatMessages.value = currentMessages
                }
            }
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    // 6. Tracker / Game States (Combining logs with computed metrics)
    val trackerUiState: StateFlow<TrackerUiState> = repository.allLogs
        .combine(MutableStateFlow(Unit)) { logs, _ ->
            val totalPoints = logs.sumOf { it.points }
            
            // Calculate Rank
            val rankResId: Int
            val nextRankResId: Int
            val pointsToNextRank: Int
            val progressPercentage: Float
            
            when {
                totalPoints < 100 -> {
                    rankResId = R.string.rank_kitten
                    nextRankResId = R.string.rank_feline
                    pointsToNextRank = 100 - totalPoints
                    progressPercentage = totalPoints.toFloat() / 100f
                }
                totalPoints < 300 -> {
                    rankResId = R.string.rank_feline
                    nextRankResId = R.string.rank_champion
                    pointsToNextRank = 300 - totalPoints
                    val currentProgress = totalPoints - 100
                    progressPercentage = currentProgress.toFloat() / 200f
                }
                totalPoints < 600 -> {
                    rankResId = R.string.rank_champion
                    nextRankResId = R.string.rank_hero
                    pointsToNextRank = 600 - totalPoints
                    val currentProgress = totalPoints - 300
                    progressPercentage = currentProgress.toFloat() / 300f
                }
                else -> {
                    rankResId = R.string.rank_hero
                    nextRankResId = R.string.rank_max
                    pointsToNextRank = 0
                    progressPercentage = 1.0f
                }
            }

            // Calculate Badge Unlocks
            val firstLog = logs.isNotEmpty()
            val feedsCount = logs.count { it.activityType == "feed_cat" }
            val sheltersCount = logs.count { it.activityType == "build_shelter" }
            val rescuesCount = logs.count { it.activityType == "rescue_cat" }
            val vetVisitsCount = logs.count { it.activityType == "vet_visit" }
            
            val badges = listOf(
                Badge(
                    id = "first_steps",
                    name = "badge_first_steps_name",
                    description = "badge_first_steps_desc",
                    iconEmoji = "🐾",
                    isUnlocked = firstLog
                ),
                Badge(
                    id = "kind_feeder",
                    name = "badge_kind_feeder_name",
                    description = "badge_kind_feeder_desc",
                    iconEmoji = "🐟",
                    isUnlocked = feedsCount >= 3
                ),
                Badge(
                    id = "master_builder",
                    name = "badge_master_builder_name",
                    description = "badge_master_builder_desc",
                    iconEmoji = "🏡",
                    isUnlocked = sheltersCount >= 1
                ),
                Badge(
                    id = "life_saver",
                    name = "badge_life_saver_name",
                    description = "badge_life_saver_desc",
                    iconEmoji = "❤️",
                    isUnlocked = rescuesCount >= 1
                ),
                Badge(
                    id = "health_guardian",
                    name = "badge_health_guardian_name",
                    description = "badge_health_guardian_desc",
                    iconEmoji = "🩺",
                    isUnlocked = vetVisitsCount >= 1
                ),
                Badge(
                    id = "tiny_paws_hero",
                    name = "badge_hero_name",
                    description = "badge_hero_desc",
                    iconEmoji = "🏆",
                    isUnlocked = totalPoints >= 500
                )
            )

            TrackerUiState(
                logs = logs,
                totalPoints = totalPoints,
                rankResId = rankResId,
                pointsToNextRank = pointsToNextRank,
                nextRankResId = nextRankResId,
                progressPercentage = progressPercentage,
                badges = badges
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrackerUiState()
        )

    // Triage Navigation Methods
    fun startTriage() {
        _triageStep.value = 1
        _hasInjuries.value = null
        _catAgeGroup.value = null
    }

    fun selectInjuries(injured: Boolean) {
        _hasInjuries.value = injured
        if (injured) {
            // Straight to immediate action result (Step 3)
            _triageStep.value = 3
        } else {
            // No injuries, proceed to age check (Step 2)
            _triageStep.value = 2
        }
    }

    fun selectAgeGroup(ageGroup: String) {
        _catAgeGroup.value = ageGroup
        _triageStep.value = 3 // Go to Next Steps/Result
    }

    fun resetTriage() {
        _triageStep.value = 0
        _hasInjuries.value = null
        _catAgeGroup.value = null
    }

    // Activity Logging Methods
    fun logActivity(type: String, notes: String = "") {
        val (nameKey, points) = when (type) {
            "feed_cat" -> "activity_feed" to 15
            "build_shelter" -> "activity_shelter" to 60
            "rescue_cat" -> "activity_rescue" to 100
            "vet_visit" -> "activity_vet" to 50
            "cuddle_socialize" -> "activity_socialize" to 10
            "donate_supplies" -> "activity_donate" to 30
            "complete_quiz" -> "activity_quiz" to 20
            "generate_art" -> "activity_art" to 10
            "generate_music" -> "activity_music" to 15
            else -> "activity_default" to 10
        }

        viewModelScope.launch {
            repository.insert(
                LogEntry(
                    activityType = type,
                    activityName = nameKey, // Using the key for translation in UI
                    points = points,
                    notes = notes
                )
            )
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Active Guide and DIY state for navigation & deep linking (like Surprise Me)
    val activeGuideTab = MutableStateFlow("menu")
    val activeDiyProjectId = MutableStateFlow<String?>(null)
    val activeCatsNearMeTab = MutableStateFlow("browse") // "browse" or "report"

    fun updateActiveGuideTab(tab: String) {
        activeGuideTab.value = tab
    }

    fun updateActiveDiyProject(projectId: String?) {
        activeDiyProjectId.value = projectId
    }

    fun updateCatsNearMeTab(tab: String) {
        activeCatsNearMeTab.value = tab
    }

    // Tracks recently viewed guide sections and DIY projects to avoid repeating them in Surprise Me
    fun trackItemViewed(itemId: String, sharedPrefs: android.content.SharedPreferences) {
        val recentlyViewedStr = sharedPrefs.getString("recently_viewed_surprise", "") ?: ""
        val recentlyViewed = recentlyViewedStr.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (!recentlyViewed.contains(itemId)) {
            recentlyViewed.add(itemId)
            if (recentlyViewed.size > 15) {
                recentlyViewed.removeAt(0)
            }
            sharedPrefs.edit().putString("recently_viewed_surprise", recentlyViewed.joinToString(",")).apply()
        }
    }

    // Selects a random guide section or DIY project that has not been viewed recently
    fun selectRandomSurprise(sharedPrefs: android.content.SharedPreferences): Pair<String, String?> {
        val guideTabs = listOf("diseases", "adoption", "stray", "cook")
        val diyProjects = DiyProjectsData.projects.map { it.id }
        val allOptions = guideTabs.map { "guide_$it" } + diyProjects.map { "diy_$it" }

        val recentlyViewedStr = sharedPrefs.getString("recently_viewed_surprise", "") ?: ""
        val recentlyViewed = recentlyViewedStr.split(",").filter { it.isNotEmpty() }.toMutableList()

        var availableOptions = allOptions.filter { !recentlyViewed.contains(it) }

        if (availableOptions.isEmpty()) {
            recentlyViewed.clear()
            availableOptions = allOptions
        }

        val selected = availableOptions.randomOrNull() ?: "guide_diseases"

        recentlyViewed.add(selected)
        if (recentlyViewed.size > 15) {
            recentlyViewed.removeAt(0)
        }
        sharedPrefs.edit().putString("recently_viewed_surprise", recentlyViewed.joinToString(",")).apply()

        return if (selected.startsWith("guide_")) {
            Pair(selected.substringAfter("guide_"), null)
        } else {
            Pair("diy", selected.substringAfter("diy_"))
        }
    }
}

// ViewModel Factory
class TinyPawsViewModelFactory(
    private val application: Application,
    private val repository: LogRepository,
    private val catRepository: CatRepository,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TinyPawsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TinyPawsViewModel(application, repository, catRepository, firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
