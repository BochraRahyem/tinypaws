package com.example.ui

import kotlinx.coroutines.flow.first
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
import kotlinx.coroutines.withContext
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
import kotlinx.coroutines.tasks.await

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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TinyPawsViewModel(
    application: Application,
    private val repository: LogRepository,
    private val catRepository: CatRepository,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository(),
    private val reportRepository: FirestoreReportRepository = FirestoreReportRepository(),
    private val stationRepository: FirestoreFeedingStationRepository = FirestoreFeedingStationRepository()
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)

    private val _onboardedName = MutableStateFlow("")
    val onboardedName = _onboardedName.asStateFlow()

    private val _selectedCatId = MutableStateFlow(sharedPrefs.getInt("active_cat_id", 1))
    val selectedCatId: StateFlow<Int> = _selectedCatId.asStateFlow()

    private val _currentIsland = MutableStateFlow(sharedPrefs.getInt("quiz_current_island", 0))
    val currentIsland = _currentIsland.asStateFlow()

    private val _quizCompletedOnCurrentIsland = MutableStateFlow(sharedPrefs.getBoolean("quiz_completed_on_current", false))
    val quizCompletedOnCurrentIsland = _quizCompletedOnCurrentIsland.asStateFlow()

    private val _completedIslands = MutableStateFlow<Set<Int>>(
        sharedPrefs.getString("quiz_completed_islands", "")?.takeIf { it.isNotEmpty() }
            ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    )
    val completedIslands = _completedIslands.asStateFlow()

    private val _scoreOnCurrentIsland = MutableStateFlow(sharedPrefs.getInt("quiz_score_on_current", 0))
    val scoreOnCurrentIsland = _scoreOnCurrentIsland.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _user.value = firebaseAuth.currentUser
        syncUserAccountState(firebaseAuth.currentUser?.uid)
    }

    private fun getAccountPrefix(): String {
        val uid = _user.value?.uid
        return if (uid.isNullOrEmpty()) "guest_" else "user_${uid}_"
    }

    private fun syncUserAccountState(userId: String?) {
        val prefix = if (userId.isNullOrEmpty()) "guest_" else "user_${userId}_"
        val savedName = sharedPrefs.getString("${prefix}onboarded_name", null)
            ?: if (userId.isNullOrEmpty()) {
                sharedPrefs.getString("user_name", "") ?: ""
            } else {
                auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.email?.substringBefore("@")
                    ?: ""
            }
        _onboardedName.value = savedName

        if (userId.isNullOrEmpty()) {
            _currentIsland.value = sharedPrefs.getInt("guest_quiz_current_island", sharedPrefs.getInt("quiz_current_island", 0))
            _quizCompletedOnCurrentIsland.value = sharedPrefs.getBoolean("guest_quiz_completed_on_current", sharedPrefs.getBoolean("quiz_completed_on_current", false))
            val islandsStr = sharedPrefs.getString("guest_quiz_completed_islands", null)
                ?: sharedPrefs.getString("quiz_completed_islands", "") ?: ""
            _completedIslands.value = islandsStr.takeIf { it.isNotEmpty() }
                ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            _scoreOnCurrentIsland.value = sharedPrefs.getInt("guest_quiz_score_on_current", sharedPrefs.getInt("quiz_score_on_current", 0))
            _selectedCatId.value = sharedPrefs.getInt("guest_active_cat_id", sharedPrefs.getInt("active_cat_id", 1))
        } else {
            _currentIsland.value = sharedPrefs.getInt("user_${userId}_quiz_current_island", 0)
            _quizCompletedOnCurrentIsland.value = sharedPrefs.getBoolean("user_${userId}_quiz_completed_on_current", false)
            val islandsStr = sharedPrefs.getString("user_${userId}_quiz_completed_islands", "") ?: ""
            _completedIslands.value = islandsStr.takeIf { it.isNotEmpty() }
                ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            _scoreOnCurrentIsland.value = sharedPrefs.getInt("user_${userId}_quiz_score_on_current", 0)
            _selectedCatId.value = sharedPrefs.getInt("user_${userId}_active_cat_id", sharedPrefs.getInt("active_cat_id", 1))
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        syncUserAccountState(auth.currentUser?.uid)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<com.google.firebase.auth.FirebaseUser?> = _user.asStateFlow()

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

    val reportingChampions: StateFlow<List<UserProfile>> = firebaseRepository.getReportingChampions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rescueChampions: StateFlow<List<UserProfile>> = firebaseRepository.getRescueChampions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val helpedAndAdoptedCats: StateFlow<List<CatReport>> = firebaseRepository.getHelpedAndAdoptedCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userActions: StateFlow<List<UserAction>> = user.flatMapLatest {
        if (it != null) firebaseRepository.getUserActions(it.uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Firestore Actions
    /** Last failed user action, surfaced to the UI as a truthful failure message. */
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()
    fun clearActionError() { _actionError.value = null }

    // P0-8: Track in-flight actions to prevent duplicate submissions from
    // double-taps, rapid retries, recomposition/re-render, or repeated screen entry.
    private val _pendingActions = MutableStateFlow<Set<String>>(emptySet())

    private suspend fun reportActionFailure(what: String, e: Exception) {
        android.util.Log.e("TinyPawsVM", "$what failed", e)
        _actionError.value = when {
            e is IllegalStateException && e.message == "Not signed in" ->
                "You need to be signed in to do that."
            e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                "Permission denied by server. Please try signing in again."
            else -> "$what failed. Please check your connection and try again."
        }
    }

    fun helpCat(reportId: String, actionType: String = "helped") {
        val actionKey = "help_${reportId}_$actionType"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            try {
                firebaseRepository.helpCat(reportId, actionType)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Marking cat as helped", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }

    fun adoptCat(reportId: String) {
        val actionKey = "adopt_$reportId"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            try {
                firebaseRepository.adoptCat(reportId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Adoption update", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }

    fun feedCat(reportId: String) {
        val actionKey = "feed_$reportId"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            try {
                firebaseRepository.feedCat(reportId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Feeding log", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }

    fun fillFeedingStation(stationId: String) {
        val actionKey = "fill_$stationId"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            try {
                firebaseRepository.fillFeedingStation(stationId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Feeding station update", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }
fun createReport(
        description: String,
        latitude: Double,
        longitude: Double,
        photoUrl: String,
        needs: String = "",
        reporterName: String = "",
        imageUri: android.net.Uri? = null,
        reportId: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isReportsLoading.value = true
            var newlyUploadedUrl: String? = null
            try {
                val trimmedName = reporterName.trim()
                val currentUser = auth.currentUser

                val finalReporterName = when {
                    trimmedName.isNotEmpty() -> trimmedName
                    currentUser != null -> currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Community Member"
                    else -> ""
                }

                if (finalReporterName.isBlank()) {
                    onComplete(false, "Please enter your name to report as a guest.")
                    return@launch
                }

                if (finalReporterName.length > 50) {
                    onComplete(false, "Reporter name is too long (maximum 50 characters).")
                    return@launch
                }

                if (description.trim().isBlank()) {
                    onComplete(false, "Please enter a description for the stray cat report.")
                    return@launch
                }

                val hasNetwork = isNetworkAvailable()

                if (imageUri != null && !hasNetwork) {
                    onComplete(false, "Image upload requires an active internet connection. Please connect to the internet to submit a photo report.")
                    return@launch
                }

                var finalPhotoUrl = photoUrl
                if (imageUri != null) {
                    try {
                        finalPhotoUrl = firebaseRepository.uploadImageToStorage(imageUri, "cat_photos", getApplication())
                        newlyUploadedUrl = finalPhotoUrl
                        android.util.Log.d("TinyPawsVM", "Image uploaded successfully: $finalPhotoUrl")
                    } catch (e: Exception) {
                        android.util.Log.e("TinyPawsVM", "Error uploading cat photo to Storage", e)
                        throw Exception("Failed to upload cat image: ${e.localizedMessage}")
                    }
                }

                val finalId = reportId.ifBlank { java.util.UUID.randomUUID().toString() }
                reportRepository.createReport(
                    com.example.data.CatReport(
                        id = finalId,
                        latitude = latitude,
                        longitude = longitude,
                        description = description.trim(),
                        catImageUrl = finalPhotoUrl,
                        photoUrl = finalPhotoUrl,
                        needs = needs,
                        reporterName = finalReporterName,
                        status = "active"
                    ),
                    finalId
                )

                logActivity("rescue_cat", "Reported a stray cat in need: $needs")
                android.util.Log.d("TinyPawsVM", "Report successfully created and logged to Firestore")
                
                if (hasNetwork) {
                    onComplete(true, "Successfully synced")
                } else {
                    onComplete(true, "Saved locally / Waiting for connection")
                }
            } catch (e: Exception) {
                android.util.Log.e("TinyPawsVM", "Failed to create report", e)
                if (newlyUploadedUrl != null) {
                    android.util.Log.d("TinyPawsVM", "Image uploaded to relay host but report write failed; image remains on ImgBB (host-managed retention)")
                }
                onComplete(false, e.localizedMessage ?: "Failed to submit report. Please check your connection and try again.")
            } finally {
                _isReportsLoading.value = false
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val context = getApplication<Application>()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        return false
    }

    fun createFeedingStation(
        description: String,
        latitude: Double,
        longitude: Double,
        photoUrl: String,
        imageUri: android.net.Uri? = null,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            var finalPhotoUrl = photoUrl
            if (imageUri != null) {
                try {
                    finalPhotoUrl = firebaseRepository.uploadImageToStorage(imageUri, "feeding_stations", getApplication())
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "Error uploading station photo", e)
                    reportActionFailure("Uploading station photo", e)
                }
            }
            try {
                stationRepository.createFeedingStation(
                    FeedingStation(
                        latitude = latitude,
                        longitude = longitude,
                        description = description,
                        photoUrl = finalPhotoUrl,
                        status = "active"
                    )
                )
                onComplete?.invoke()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Creating feeding station", e)
            }
        }
    }

    fun reachCat(reportId: String) {
        val actionKey = "reach_$reportId"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            try {
                reportRepository.reachCat(reportId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Recording your reach", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }

    fun reachFeedingStation(stationId: String, foodAvailable: Boolean) {
        val actionKey = "reach_station_$stationId"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            try {
                stationRepository.reachFeedingStation(stationId, foodAvailable)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Feeding station update", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }

    fun markAsRescued(
        reportId: String,
        photoUrl: String,
        desc: String,
        imageUri: android.net.Uri? = null
    ) {
        val actionKey = "rescue_$reportId"
        if (_pendingActions.value.contains(actionKey)) return
        _pendingActions.value = _pendingActions.value + actionKey
        viewModelScope.launch {
            var finalPhotoUrl = photoUrl
            if (imageUri != null) {
                try {
                    finalPhotoUrl = firebaseRepository.uploadImageToStorage(imageUri, "rescue_photos", getApplication())
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "Error uploading rescue photo", e)
                    reportActionFailure("Uploading rescue photo", e)
                    _pendingActions.value = _pendingActions.value - actionKey
                    return@launch
                }
            }
            try {
                // P0-8: markAsRescued returns false if already rescued — skip reward.
                val wasUpdated = reportRepository.markAsRescued(reportId, finalPhotoUrl, desc)
                if (wasUpdated) {
                    firebaseRepository.recordUserAction(
                        com.example.data.UserAction(
                            actionType = "adopt_cat",
                            starsEarned = 10,
                            rewardAmount = 10,
                            description = "Adopted/rescued cat permanently",
                            relatedCatId = reportId,
                            catId = reportId
                        )
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                reportActionFailure("Marking cat as rescued", e)
            } finally {
                _pendingActions.value = _pendingActions.value - actionKey
            }
        }
    }

    fun signInAnonymously(name: String, onResult: (Boolean, String?) -> Unit) {
        val displayName = if (name.isBlank()) "Rescue Friend" else name.trim()
        try {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        _user.value = firebaseUser
                        _onboardedName.value = displayName
                        if (firebaseUser != null) {
                            viewModelScope.launch {
                                val profile = UserProfile(
                                    userId = firebaseUser.uid,
                                    displayName = displayName,
                                    email = "guest_${firebaseUser.uid.take(6)}@tinypaws.app",
                                    preferredLanguage = _currentLanguage.value
                                )
                                try {
                                    firebaseRepository.createUserProfile(profile)
                                } catch (e: Exception) {
                                    android.util.Log.e("TinyPawsVM", "Error creating guest profile", e)
                                }
                                flushPendingFcmToken()
                            }
                        }
                        onResult(true, null)
                    } else {
                        // P0-2: Never swallow auth failures. Return the failure to the caller.
                        android.util.Log.w("TinyPawsVM", "Firebase anonymous sign in failed: ${task.exception?.message}")
                        onResult(false, "Sign-in failed. Please check your connection and try again.")
                    }
                }
        } catch (e: Exception) {
            // P0-2: Network or unexpected errors must not silently succeed.
            android.util.Log.e("TinyPawsVM", "Firebase auth exception during anonymous sign-in", e)
            onResult(false, "Sign-in failed. Please check your connection and try again.")
        }
    }

    /**
     * Pushes the device FCM token to the signed-in user's profile.
     * Uses any token staged by TinyPawsMessagingService while signed out,
     * otherwise asks FirebaseMessaging for the current token (best effort;
     * silently skipped on devices without Google Play services).
     */
    private fun flushPendingFcmToken() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
                val staged = prefs.getString("pending_fcm_token", null)
                if (!staged.isNullOrBlank()) {
                    firebaseRepository.updateUserMetadata(fcmToken = staged)
                    prefs.edit().remove("pending_fcm_token").apply()
                    return@launch
                }
                val task = com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                val token = task.await()
                if (!token.isNullOrBlank()) {
                    firebaseRepository.updateUserMetadata(fcmToken = token)
                }
            } catch (e: Exception) {
                android.util.Log.w("TinyPawsVM", "FCM token sync skipped: ${e.message}")
            }
        }
    }

    fun mapAuthError(e: Throwable?, context: Context): String {
        android.util.Log.e("TinyPawsVM", "Auth error: ", e)
        if (e == null) return context.getString(com.example.R.string.auth_err_default)
        val message = e.message ?: ""
        return when {
            e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ||
            e is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            message.contains("wrong password", ignoreCase = true) ||
            message.contains("user not found", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_invalid_credentials)
            }
            e is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
            message.contains("email already in use", ignoreCase = true) ||
            message.contains("EMAIL_EXISTS", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_email_already_in_use)
            }
            e is com.google.firebase.auth.FirebaseAuthWeakPasswordException ||
            message.contains("weak password", ignoreCase = true) ||
            message.contains("WEAK_PASSWORD", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_weak_password)
            }
            message.contains("invalid email", ignoreCase = true) ||
            message.contains("INVALID_EMAIL", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_invalid_email)
            }
            e is com.google.firebase.FirebaseTooManyRequestsException ||
            message.contains("too many requests", ignoreCase = true) ||
            message.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_too_many_requests)
            }
            e is com.google.firebase.FirebaseNetworkException ||
            message.contains("network", ignoreCase = true) ||
            message.contains("connection", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_network_error)
            }
            message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
            message.contains("admin", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_configuration)
            }
            message.contains("An internal error has occurred", ignoreCase = true) ||
            message.contains("internal error", ignoreCase = true) -> {
                context.getString(com.example.R.string.auth_err_default)
            }
            else -> context.getString(com.example.R.string.auth_err_default)
        }
    }

    fun signIn(email: String, pass: String, context: Context, onResult: (Boolean, String?) -> Unit) {
        // P1-2: Capture the previous user ID BEFORE signing in so we can detect account switching.
        val previousUserId = auth.currentUser?.uid
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    _user.value = firebaseUser
                    firebaseUser?.displayName?.let { name ->
                        if (name.isNotBlank()) {
                            updateOnboardedName(name)
                        }
                    }
                    // P1-2: Restore data from cloud on sign in. Only clear local data
                    // when switching accounts (different UID). Never clear before confirming
                    // the restore can proceed.
                    viewModelScope.launch(Dispatchers.IO) {
                        val newUserId = firebaseUser?.uid
                        val isAccountSwitch = previousUserId != null && newUserId != null && previousUserId != newUserId
                        if (isAccountSwitch) {
                            // Backup previous account's data before clearing to prevent data loss.
                            // If backup fails, abort the account switch to protect local data.
                            try {
                                val backupResult = com.example.data.FirestoreBackupHelper.backupDataToCloud(
                                    allProfiles = catRepository.allCatProfiles.first(),
                                    careLogs = catRepository.allCareLogs.first(),
                                    weightLogs = catRepository.allWeightLogs.first(),
                                    diaryLogs = catRepository.allCheckInLogs.first(),
                                    reminders = catRepository.allReminders.first(),
                                    historyEntries = catRepository.allHistoryEntries.first()
                                )
                                if (backupResult.isFailure) {
                                    android.util.Log.e("TinyPawsVM", "Pre-switch backup failed — aborting account switch", backupResult.exceptionOrNull())
                                    viewModelScope.launch(Dispatchers.Main) {
                                        onResult(false, "Cannot switch accounts: backup failed. Please check your connection and try again.")
                                    }
                                    return@launch
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("TinyPawsVM", "Pre-switch backup failed", e)
                                viewModelScope.launch(Dispatchers.Main) {
                                    onResult(false, "Cannot switch accounts: backup failed. Please check your connection and try again.")
                                }
                                return@launch
                            }
                            com.example.data.AppDatabase.getDatabase(getApplication()).clearAllTables()
                        }
                        restoreDataFromCloud(context) { resultMsg ->
                            android.util.Log.d("TinyPawsVM", "Auto-restore after sign-in: $resultMsg")
                            val restoreSucceeded = !resultMsg.contains("failed", ignoreCase = true)
                                    && !resultMsg.contains("error", ignoreCase = true)
                            viewModelScope.launch(Dispatchers.Main) {
                                onResult(restoreSucceeded, if (restoreSucceeded) null else resultMsg)
                            }
                        }
                        flushPendingFcmToken()
                    }
                } else {
                    val mappedError = mapAuthError(task.exception, context)
                    onResult(false, mappedError)
                }
            }
    }

    // NOTE: The welcome email is sent by exactly ONE authoritative path:
    // createUserProfile -> Firestore onUserCreated trigger -> mail collection
    // -> onMailCreated -> Resend. The app must NOT call the Cloudflare email
    // worker directly anymore (it caused duplicate welcome emails).

    private val _resendCooldownSeconds = MutableStateFlow(0)
    val resendCooldownSeconds: StateFlow<Int> = _resendCooldownSeconds.asStateFlow()

    private var cooldownJob: kotlinx.coroutines.Job? = null

    private fun startResendCooldown(seconds: Int = 60) {
        cooldownJob?.cancel()
        _resendCooldownSeconds.value = seconds
        cooldownJob = viewModelScope.launch {
            while (_resendCooldownSeconds.value > 0) {
                kotlinx.coroutines.delay(1000)
                _resendCooldownSeconds.value -= 1
            }
        }
    }

    fun resendVerificationEmail(context: Context, onResult: (Boolean, String) -> Unit) {
        if (_resendCooldownSeconds.value > 0) {
            onResult(false, context.getString(com.example.R.string.auth_resend_cooldown, _resendCooldownSeconds.value))
            return
        }

        val user = auth.currentUser
        if (user == null) {
            onResult(false, context.getString(com.example.R.string.auth_err_user_not_found))
            return
        }

        if (user.isEmailVerified) {
            onResult(true, context.getString(com.example.R.string.auth_already_verified))
            return
        }

        val actionSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://tinypaws-diary.web.app/verify-email")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.tinypaws.app", true, null)
            .build()
        user.sendEmailVerification(actionSettings).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startResendCooldown(60)
                onResult(true, context.getString(com.example.R.string.auth_verification_sent_msg))
            } else {
                val mappedError = mapAuthError(task.exception, context)
                onResult(false, mappedError)
            }
        }
    }

    fun checkEmailVerificationStatus(context: Context, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, context.getString(com.example.R.string.auth_err_user_not_found))
            return
        }

        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updatedUser = auth.currentUser ?: user
                _user.value = updatedUser
                if (updatedUser.isEmailVerified) {
                    onResult(true, context.getString(com.example.R.string.auth_verified_success))
                } else {
                    onResult(false, context.getString(com.example.R.string.auth_not_verified_yet))
                }
            } else {
                val mappedError = mapAuthError(task.exception, context)
                onResult(false, mappedError)
            }
        }
    }

    fun reloadUser() {
        auth.currentUser?.reload()?.addOnCompleteListener {
            _user.value = auth.currentUser
        }
    }

    fun linkGuestAccount(fullName: String, email: String, country: String, pass: String, context: Context, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        val trimmedEmail = email.trim()
        val nameToSave = fullName.trim()
        val countryToSave = country.trim()

        if (currentUser != null && currentUser.isAnonymous) {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(trimmedEmail, pass)
            currentUser.linkWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updatedUser = auth.currentUser ?: currentUser
                    _user.value = updatedUser
                    updateOnboardedName(nameToSave)

                    viewModelScope.launch {
                        val profile = UserProfile(
                            userId = updatedUser.uid,
                            displayName = nameToSave,
                            email = trimmedEmail,
                            country = countryToSave,
                            preferredLanguage = _currentLanguage.value
                        )
                        try {
                            firebaseRepository.createUserProfile(profile)
                        } catch (e: Exception) {
                            android.util.Log.e("TinyPawsVM", "Error creating profile on link", e)
                        }
                        sendWelcomeEmail(trimmedEmail)
                        flushPendingFcmToken()
                    }

                    val actionSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                        .setUrl("https://tinypaws-diary.web.app/verify-email")
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName("com.tinypaws.app", true, null)
                        .build()
                    updatedUser.sendEmailVerification(actionSettings).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            startResendCooldown(60)
                            onResult(true, context.getString(com.example.R.string.auth_verification_sent_msg))
                        } else {
                            val mappedError = mapAuthError(emailTask.exception, context)
                            onResult(true, context.getString(com.example.R.string.auth_account_created_email_failed, mappedError))
                        }
                    }
                } else {
                    val mappedError = mapAuthError(task.exception, context)
                    onResult(false, mappedError)
                }
            }
        } else {
            signUp(fullName, trimmedEmail, countryToSave, pass, context, onResult)
        }
    }

    fun signUp(fullName: String, email: String, country: String, pass: String, context: Context, onResult: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val countryToSave = country.trim()
        val nameToSave = fullName.trim()

        auth.createUserWithEmailAndPassword(trimmedEmail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    _user.value = firebaseUser
                    updateOnboardedName(nameToSave)

                    if (firebaseUser != null) {
                        viewModelScope.launch {
                            val profile = UserProfile(
                                userId = firebaseUser.uid,
                                displayName = nameToSave,
                                email = trimmedEmail,
                                country = countryToSave,
                                preferredLanguage = _currentLanguage.value
                            )
                            try {
                                firebaseRepository.createUserProfile(profile)
                            } catch (e: Exception) {
                                android.util.Log.e("TinyPawsVM", "Error creating user profile", e)
                            }
                            sendWelcomeEmail(trimmedEmail)
                            flushPendingFcmToken()
                        }

                        val actionSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                            .setUrl("https://tinypaws-diary.web.app/verify-email")
                            .setHandleCodeInApp(true)
                            .setAndroidPackageName("com.tinypaws.app", true, null)
                            .build()
                        firebaseUser.sendEmailVerification(actionSettings).addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                startResendCooldown(60)
                                onResult(true, context.getString(com.example.R.string.auth_verification_sent_msg))
                            } else {
                                val mappedError = mapAuthError(emailTask.exception, context)
                                onResult(true, context.getString(com.example.R.string.auth_account_created_email_failed, mappedError))
                            }
                        }
                    } else {
                        onResult(true, context.getString(com.example.R.string.auth_verification_sent_msg))
                    }
                } else {
                    val mappedError = mapAuthError(task.exception, context)
                    onResult(false, mappedError)
                }
            }
    }

    fun sendPasswordResetEmail(email: String, context: Context, onResult: (Boolean, String) -> Unit) {
        val actionSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://tinypaws-diary.web.app/reset-password")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.tinypaws.app", true, null)
            .build()
        auth.sendPasswordResetEmail(email.trim(), actionSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, context.getString(com.example.R.string.auth_reset_sent_msg))
                } else {
                    val ex = task.exception
                    val msg = if (ex is com.google.firebase.FirebaseNetworkException) {
                        context.getString(com.example.R.string.auth_err_network_error)
                    } else if (ex?.message?.contains("invalid email", ignoreCase = true) == true) {
                        context.getString(com.example.R.string.auth_err_invalid_email)
                    } else {
                        mapAuthError(ex, context)
                    }
                    onResult(false, msg)
                }
            }
    }

    fun verifyPasswordResetCode(oobCode: String, onResult: (String?, String?) -> Unit) {
        auth.verifyPasswordResetCode(oobCode)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(task.result, null)
                } else {
                    onResult(null, mapAuthError(task.exception, getApplication()))
                }
            }
    }

    fun confirmPasswordReset(oobCode: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        auth.confirmPasswordReset(oobCode, newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, getApplication<Application>().getString(com.example.R.string.auth_reset_success_msg))
                } else {
                    onResult(false, mapAuthError(task.exception, getApplication()))
                }
            }
    }

    fun signOut() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                // P1-1: Backup MUST succeed before wiping local data.
                val backupResult = try {
                    com.example.data.FirestoreBackupHelper.backupDataToCloud(
                        allProfiles = catRepository.allCatProfiles.first(),
                        careLogs = catRepository.allCareLogs.first(),
                        weightLogs = catRepository.allWeightLogs.first(),
                        diaryLogs = catRepository.allCheckInLogs.first(),
                        reminders = catRepository.allReminders.first(),
                        historyEntries = catRepository.allHistoryEntries.first()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "Automatic backup on logout failed", e)
                    Result.failure(e)
                }
                if (backupResult.isFailure) {
                    // P1-1: Do NOT wipe local data if backup fails.
                    withContext(Dispatchers.Main) {
                        _actionError.value = "Backup failed. Your local data is preserved. Please check your connection and try again."
                    }
                    return@launch
                }
                try {
                    com.example.data.AppDatabase.getDatabase(getApplication()).clearAllTables()
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "clearAllTables failed during signOut", e)
                }
                withContext(Dispatchers.Main) {
                    // P0-1: Clear auth-dependent navigation state BEFORE signing out.
                    _onboardedName.value = ""
                    sharedPrefs.edit()
                        .remove("${getAccountPrefix()}onboarded_name")
                        .remove("user_name")
                        .apply()
                    _user.value = null
                    auth.signOut()
                    syncUserAccountState(null)
                    _user.value = auth.currentUser
                }
            }
        } else {
            _user.value = null
            auth.signOut()
            _onboardedName.value = ""
            sharedPrefs.edit()
                .remove("${getAccountPrefix()}onboarded_name")
                .remove("user_name")
                .apply()
            viewModelScope.launch(Dispatchers.IO) {
                com.example.data.AppDatabase.getDatabase(getApplication()).clearAllTables()
            }
            syncUserAccountState(null)
        }
    }

    // Multi-Cat Profiles & Active Cat State
    val allCatProfiles: StateFlow<List<CatProfile>> = catRepository.allCatProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCat(catId: Int) {
        _selectedCatId.value = catId
        val prefix = getAccountPrefix()
        sharedPrefs.edit()
            .putInt("${prefix}active_cat_id", catId)
            .putInt("active_cat_id", catId)
            .apply()
    }

    // Dynamic Active Cat Profile
    val catProfile: StateFlow<CatProfile?> = combine(allCatProfiles, selectedCatId) { profiles, activeId ->
        if (profiles.isEmpty()) {
            null
        } else {
            profiles.find { it.id == activeId } ?: profiles.firstOrNull()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun saveCatProfile(
        name: String,
        ageYears: Int,
        ageMonths: Int,
        coatColor: String,
        photoUrl: String? = null,
        personality: String = "",
        adoptionPhotoUrl: String? = null,
        foodPreferences: String = "",
        chronicConditions: String? = null,
        id: Int = selectedCatId.value
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = CatProfile(
                id = id,
                name = name,
                ageYears = ageYears,
                ageMonths = ageMonths,
                coatColor = coatColor,
                photoUrl = photoUrl,
                personality = personality,
                adoptionPhotoUrl = adoptionPhotoUrl,
                foodPreferences = foodPreferences,
                chronicConditions = chronicConditions
            )
            val newId = catRepository.saveProfile(profile).toInt()
            if (id == 0 && newId > 0) {
                _selectedCatId.value = newId
                val prefix = getAccountPrefix()
                sharedPrefs.edit()
                    .putInt("${prefix}active_cat_id", newId)
                    .putInt("active_cat_id", newId)
                    .apply()
            }

            val uid = auth.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                try {
                    val petMap = mapOf(
                        "name" to name,
                        "ageYears" to ageYears,
                        "ageMonths" to ageMonths,
                        "coatColor" to coatColor,
                        "photoUrl" to (photoUrl ?: ""),
                        "personality" to personality,
                        "adoptionPhotoUrl" to (adoptionPhotoUrl ?: ""),
                        "foodPreferences" to foodPreferences,
                        "chronicConditions" to (chronicConditions ?: ""),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .collection("pets")
                        .document("pet_${id.takeIf { it > 0 } ?: newId}")
                        .set(petMap, com.google.firebase.firestore.SetOptions.merge())
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "Error syncing pet profile to cloud", e)
                }
            }
        }
    }

    fun addNewCatProfile(
        name: String,
        ageYears: Int = 1,
        ageMonths: Int = 0,
        coatColor: String = "Calico",
        photoUrl: String? = null,
        personality: String = "Playful",
        onCreated: (Int) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCount = allCatProfiles.value.size
            if (currentCount >= 7) {
                return@launch
            }
            
            val newProfile = CatProfile(
                id = 0,
                name = name,
                ageYears = ageYears,
                ageMonths = ageMonths,
                coatColor = coatColor,
                photoUrl = photoUrl,
                personality = personality
            )
            val generatedId = catRepository.saveProfile(newProfile).toInt()
            if (generatedId > 0) {
                _selectedCatId.value = generatedId
                val prefix = getAccountPrefix()
                sharedPrefs.edit()
                    .putInt("${prefix}active_cat_id", generatedId)
                    .putInt("active_cat_id", generatedId)
                    .apply()
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onCreated(generatedId)
                }
            }
        }
    }

    fun deleteCatProfile(catIdToDelete: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = com.example.data.AppDatabase.getDatabase(getApplication())
            val reminders = db.reminderDao().getAllRemindersSync()
            val notifHelper = com.example.util.NotificationHelper(getApplication())
            reminders.filter {
                it.catId == catIdToDelete ||
                    it.catIds.split(",").mapNotNull { t -> t.trim().toIntOrNull() }.contains(catIdToDelete)
            }.forEach {
                notifHelper.cancelNotification(it.id)
            }
            catRepository.deleteCatProfile(catIdToDelete)
            val remainingProfiles = catRepository.allCatProfiles.first()
            val nextCatId = remainingProfiles
                .filter { it.id != catIdToDelete }
                .minOfOrNull { it.id } ?: 1
            selectCat(nextCatId)
        }
    }

    // Filtered Weight Logs for active cat
    val allWeightLogs: StateFlow<List<com.example.data.CatWeightLog>> = combine(catRepository.allWeightLogs, selectedCatId) { logs, activeCatId ->
        logs.filter { it.catId == activeCatId || it.catId == 1 && activeCatId == 1 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveWeightLog(date: Long, weight: Float, id: Int = 0, catId: Int = selectedCatId.value) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.saveWeightLog(date, weight, id, catId)
        }
    }

    fun deleteWeightLog(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.deleteWeightLog(id)
        }
    }

    // Filtered History Entries for active cat
    val allHistoryEntries: StateFlow<List<com.example.data.CatHistoryEntry>> = combine(catRepository.allHistoryEntries, selectedCatId) { entries, activeCatId ->
        entries.filter { it.catId == activeCatId || it.catId == 1 && activeCatId == 1 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveHistoryEntry(entry: com.example.data.CatHistoryEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedEntry = if (entry.catId <= 0) entry.copy(catId = selectedCatId.value) else entry
            catRepository.saveHistoryEntry(updatedEntry)
        }
    }

    fun deleteHistoryEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.deleteHistoryEntry(id)
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

    val roomCheckInLogs: StateFlow<List<com.example.data.CatCheckInLog>> = catRepository.allCheckInLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCareLogs: StateFlow<List<com.example.data.DailyCareLog>> = combine(catRepository.allCareLogs, selectedCatId) { logs, activeCatId ->
        logs.filter { it.catId == activeCatId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _cloudBackupState = MutableStateFlow<String?>(null)
    val cloudBackupState = _cloudBackupState.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing = _isCloudSyncing.asStateFlow()

    fun backupDataToCloud(context: Context, onResult: (String) -> Unit) {
        if (_isCloudSyncing.value) return
        _isCloudSyncing.value = true
        viewModelScope.launch {
            val res = com.example.data.FirestoreBackupHelper.backupDataToCloud(
                allProfiles = catRepository.allCatProfiles.first(),
                careLogs = catRepository.allCareLogs.first(),
                weightLogs = catRepository.allWeightLogs.first(),
                diaryLogs = catRepository.allCheckInLogs.first(),
                reminders = catRepository.allReminders.first(),
                historyEntries = catRepository.allHistoryEntries.first()
            )
            _isCloudSyncing.value = false
            
            val message = res.fold(
                onSuccess = { msg ->
                    val backupDate = msg.substringAfterLast("(", "").substringBefore(")", "recent")
                    context.getString(com.example.R.string.cloud_backup_success, backupDate)
                },
                onFailure = { context.getString(com.example.R.string.cloud_backup_failed) }
            )
            
            _cloudBackupState.value = message
            onResult(message)
        }
    }

    fun restoreDataFromCloud(context: Context, onResult: (String) -> Unit) {
        if (_isCloudSyncing.value) {
            onResult("Sync already in progress. Please try again.")
            return
        }
        _isCloudSyncing.value = true
        viewModelScope.launch {
            val res = com.example.data.FirestoreBackupHelper.restoreDataFromCloud(catRepository)
            _isCloudSyncing.value = false
            
            val message = res.fold(
                onSuccess = { msg ->
                    // Extract the backup date if present in the success message
                    val backupDate = msg.substringAfterLast("(", "").substringBefore(")", "recent")
                    context.getString(com.example.R.string.cloud_restore_success, backupDate)
                },
                onFailure = { e ->
                    if (e.message?.contains("No cloud backup found", ignoreCase = true) == true) {
                        context.getString(com.example.R.string.cloud_restore_no_data)
                    } else {
                        context.getString(com.example.R.string.cloud_restore_failed)
                    }
                }
            )
            
            _cloudBackupState.value = message
            onResult(message)
        }
    }

    fun getCareLogForDate(dateStr: String): StateFlow<com.example.data.DailyCareLog?> {
        return catRepository.getCareLogForCatAndDate(selectedCatId.value, dateStr)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun saveCareLog(careLog: com.example.data.DailyCareLog) {
        viewModelScope.launch(Dispatchers.IO) {
            val logToSave = if (careLog.catId <= 0) careLog.copy(catId = selectedCatId.value) else careLog
            catRepository.saveCareLog(logToSave)
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
            try {
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
            } catch (e: Exception) {
                android.util.Log.e("TinyPawsVM", "Failed to save diary entry", e)
                withContext(Dispatchers.Main) {
                    _actionError.value = "Failed to save diary entry. Please check your connection."
                }
            }
        }
    }

    fun deleteCheckInLog(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebaseRepository.deleteDiaryEntry(entryId)
            } catch (e: Exception) {
                android.util.Log.e("TinyPawsVM", "Failed to delete diary entry", e)
                withContext(Dispatchers.Main) {
                    _actionError.value = "Failed to delete diary entry. Please check your connection."
                }
            }
        }
    }

    // Filtered Reminders State for active cat
    val allReminders: StateFlow<List<com.example.data.Reminder>> = combine(catRepository.allReminders, selectedCatId) { list, activeCatId ->
        list.filter { r ->
            r.catId == activeCatId ||
                r.catIds.split(",").map { it.trim() }.contains(activeCatId.toString()) ||
                r.catIds == "all"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveReminder(title: String, timeMillis: Long, type: String = "general", targetCatId: Int = selectedCatId.value, targetCatIds: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.saveReminder(title, timeMillis, type, targetCatId, targetCatIds)
        }
    }

    fun saveReminder(reminder: com.example.data.Reminder, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val rToSave = if (reminder.catId <= 0) reminder.copy(catId = selectedCatId.value) else reminder
            val id = catRepository.saveReminder(rToSave)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                onSaved(id)
            }
        }
    }

    fun updateReminder(reminder: com.example.data.Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            catRepository.updateReminder(reminder)
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            com.example.util.NotificationHelper(getApplication()).cancelNotification(id)
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

    private val _currentLanguage = MutableStateFlow(sharedPrefs.getString("user_lang", "en") ?: "en")
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _streak = kotlinx.coroutines.flow.MutableStateFlow(0)
    val streak = _streak.asStateFlow()

    fun setStreak(s: Int) {
        _streak.value = s
    }

    private val _isDarkMode = kotlinx.coroutines.flow.MutableStateFlow(sharedPrefs.getBoolean("dark_mode", sharedPrefs.getBoolean("user_dark_mode", false)))
    val isDarkMode = _isDarkMode.asStateFlow()

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        sharedPrefs.edit()
            .putBoolean("dark_mode", dark)
            .putBoolean("user_dark_mode", dark)
            .apply()
    }

    private val _isExtremeWeatherNotify = kotlinx.coroutines.flow.MutableStateFlow(sharedPrefs.getBoolean("extreme_weather_notifications", true))
    val isExtremeWeatherNotify = _isExtremeWeatherNotify.asStateFlow()

    fun setExtremeWeatherNotify(notify: Boolean) {
        _isExtremeWeatherNotify.value = notify
        sharedPrefs.edit()
            .putBoolean("extreme_weather_notifications", notify)
            .apply()
    }

    fun updateOnboardedName(name: String) {
        _onboardedName.value = name
        val prefix = getAccountPrefix()
        sharedPrefs.edit()
            .putString("${prefix}onboarded_name", name)
            .apply()
    }

    fun setLanguage(langCode: String) {
        _currentLanguage.value = langCode
        // Persist to SharedPreferences so language survives restart (even offline/guest).
        sharedPrefs.edit().putString("user_lang", langCode).apply()
        val currentUser = _user.value
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    firebaseRepository.updateUserMetadata(preferredLanguage = langCode)
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "Failed to update preferredLanguage in Firestore", e)
                }
            }
        }
    }

    // 1. Triage Wizard States
    private val _triageStep = MutableStateFlow(0) // 0: Start, 1: Injury Check, 2: Age Check, 3: Next Steps
    val triageStep = _triageStep.asStateFlow()

    private val _hasInjuries = MutableStateFlow<Boolean?>(null)
    val hasInjuries = _hasInjuries.asStateFlow()

    private val _catAgeGroup = MutableStateFlow<String?>(null) // "baby", "young", "adult"
    val catAgeGroup = _catAgeGroup.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0) // 0 to 19
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    val allQuizzesCompleted = _completedIslands.map { it.size == 5 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
        val prefix = getAccountPrefix()
        sharedPrefs.edit()
            .putInt("${prefix}quiz_score_on_current", correctCount)
            .putBoolean("${prefix}quiz_completed_on_current", true)
            .putString("${prefix}quiz_completed_islands", updated.joinToString(","))
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
            val prefix = getAccountPrefix()
            sharedPrefs.edit()
                .putInt("${prefix}quiz_current_island", next)
                .putBoolean("${prefix}quiz_completed_on_current", false)
                .putInt("${prefix}quiz_score_on_current", 0)
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
            val prefix = getAccountPrefix()
            sharedPrefs.edit()
                .putInt("${prefix}quiz_current_island", islandIndex)
                .putBoolean("${prefix}quiz_completed_on_current", false)
                .putInt("${prefix}quiz_score_on_current", 0)
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
        val prefix = getAccountPrefix()
        sharedPrefs.edit()
            .putInt("${prefix}quiz_current_island", 0)
            .putBoolean("${prefix}quiz_completed_on_current", false)
            .putInt("${prefix}quiz_score_on_current", 0)
            .putString("${prefix}quiz_completed_islands", "")
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

    suspend fun fetchDailyCatFact(languageCode: String? = null): String {
        val lang = languageCode ?: _currentLanguage.value
        // Cache one fact per language per day so re-opening the Hub (or switching
        // languages back and forth) does not fire a paid API call every time.
        val prefs = getApplication<Application>().getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
        val day = System.currentTimeMillis() / 86400000L
        val cacheKey = "daily_cat_fact_${lang}_$day"
        prefs.getString(cacheKey, null)?.let { return it }

        val fact = GeminiClient.fetchDailyCatFact(lang)
        if (fact.isNotBlank()) {
            prefs.edit().putString(cacheKey, fact).apply()
            // Prune yesterday's cached facts to keep the prefs file tiny.
            val editor = prefs.edit()
            for (k in prefs.all.keys) {
                if (k.startsWith("daily_cat_fact_") && k != cacheKey) editor.remove(k)
            }
            editor.apply()
        }
        return fact
    }

    // 4. AI Studio Generation States
    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap = _generatedBitmap.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    // Runtime Step Image Cache
    private val _stepImageCache = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val stepImageCache = _stepImageCache.asStateFlow()

    /** Step images whose generation recently failed - prevents retry storms on recomposition. */
    private val failedStepImageIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    fun generateStepImage(stepId: String, prompt: String) {
        if (_stepImageCache.value.containsKey(stepId)) return
        if (!failedStepImageIds.add(stepId)) return // already failed recently; no auto-retry

        viewModelScope.launch {
            try {
                val bitmap = GeminiClient.generateImage(prompt, isProModel = false, imageSize = "1K")
                if (bitmap != null) {
                    failedStepImageIds.remove(stepId)
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

    companion object {
        /** Max chat turns (user+model pairs count individually) re-sent to the model. */
        private const val MAX_CHAT_HISTORY_MESSAGES = 12
        private const val MAX_CHAT_INPUT_CHARS = 2000

        /**
         * Free-tier welcome-email path: our own Cloudflare Worker (free plan)
         * verifies the caller's Firebase ID token and sends via Resend's free
         * tier. This is the ONLY email path - Cloud Functions are not deployed
         * (they require the paid Blaze plan).
         */
        private const val WELCOME_EMAIL_WORKER_URL =
            "https://tinypaws-email.bochra0rhayem.workers.dev"
    }

    /**
     * Sends the welcome email exactly once per account via the Cloudflare worker.
     * Claims the flag BEFORE sending so retries never double-send; releases it if
     * the worker reports failure so a later attempt can retry.
     */
    private fun sendWelcomeEmail(email: String?) {
        val trimmedEmail = email?.trim() ?: return
        if (trimmedEmail.isBlank() || trimmedEmail.endsWith("@tinypaws.app")) return
        val user = auth.currentUser ?: return
        viewModelScope.launch(Dispatchers.IO) {
            var connection: java.net.HttpURLConnection? = null
            try {
                val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid)
                userDoc.set(
                    mapOf("welcomeEmailSent" to true),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()

                val token = user.getIdToken(true).await().token
                if (token == null) {
                    userDoc.set(mapOf("welcomeEmailSent" to false), com.google.firebase.firestore.SetOptions.merge()).await()
                    return@launch
                }

                connection = java.net.URL(WELCOME_EMAIL_WORKER_URL)
                    .openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.doOutput = true
                connection.outputStream.use { os ->
                    val body = org.json.JSONObject().apply {
                        put("to", trimmedEmail)
                        put("lang", _currentLanguage.value)
                        put("template", "welcome")
                    }.toString()
                    os.write(body.toByteArray(Charsets.UTF_8))
                }
                val code = connection.responseCode
                android.util.Log.d("TinyPawsVM", "Welcome email response: $code")
                if (code != 200) {
                    userDoc.set(mapOf("welcomeEmailSent" to false), com.google.firebase.firestore.SetOptions.merge()).await()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("TinyPawsVM", "Welcome email failed", e)
                runCatching {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(user.uid)
                        .set(mapOf("welcomeEmailSent" to false), com.google.firebase.firestore.SetOptions.merge())
                }
            } finally {
                connection?.disconnect()
            }
        }
    }


    fun sendChatMessage(text: String) {
        if (text.isBlank() || _isChatLoading.value) return

        if (text.length > MAX_CHAT_INPUT_CHARS) {
            _chatMessages.value = _chatMessages.value +
                ChatMessage("user", text.take(MAX_CHAT_INPUT_CHARS) + "…") +
                ChatMessage("model", "Sorry, that message is too long for me! Please split it into a shorter message 🐾")
            return
        }

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
        
        // Bound the conversation sent to the model: keep only the most recent
        // turns so cost stays linear-bounded and we never blow the context window.
        val history = _chatMessages.value
            .filter { !it.isPending && it.text.isNotBlank() }
            .takeLast(MAX_CHAT_HISTORY_MESSAGES)
            .map { it.role to it.text }

        _isChatLoading.value = true
        viewModelScope.launch {
            var fullResponse = ""
            try {
                com.example.data.GroqClient.chatStream(systemInstruction, history) { chunk ->
                    fullResponse += chunk
                    // chatStream callback runs on IO thread; dispatch UI update to Main.
                    viewModelScope.launch(Dispatchers.Main) {
                        val currentMessages = _chatMessages.value.toMutableList()
                        if (currentMessages.isNotEmpty()) {
                            val lastIdx = currentMessages.lastIndex
                            currentMessages[lastIdx] = ChatMessage("model", fullResponse, isPending = false)
                            _chatMessages.value = currentMessages
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TinyPawsVM", "Chat stream failed", e)
                val currentMessages = _chatMessages.value.toMutableList()
                if (currentMessages.isNotEmpty()) {
                    val lastIdx = currentMessages.lastIndex
                    val errorMsg = when {
                        e.message?.contains("timeout", true) == true -> "Sorry, the response timed out. Please try again 🐾"
                        e.message?.contains("network", true) == true -> "Network issue — please check your connection and try again 🐾"
                        else -> "Something went wrong. Please try again 🐾"
                    }
                    currentMessages[lastIdx] = ChatMessage("model", errorMsg, isPending = false)
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

        val isHelpingAction = type in listOf("feed_cat", "build_shelter", "rescue_cat", "vet_visit", "donate_supplies")
        viewModelScope.launch {
            if (isHelpingAction) {
                try {
                    firebaseRepository.recordUserAction(
                        com.example.data.UserAction(
                            actionType = type,
                            starsEarned = points,
                            rewardAmount = points,
                            description = notes.ifBlank { "Logged activity: $type" }
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("TinyPawsVM", "Error recording user action", e)
                }
            }
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
