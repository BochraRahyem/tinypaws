package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /*
     * FREE-TIER SERVER REPLACEMENTS
     * -----------------------------
     * Cloud Functions require the paid Blaze plan, so the aggregate/reward
     * writes they used to perform are done here, guarded server-side by
     * Firestore rules:
     *   - /users + /publicProfiles reward counters: owner-only, monotonic
     *   - /statistics/global: authenticated, field-whitelisted increments only
     * Every helper below is best-effort: a stats failure never fails the
     * user's actual action.
     */

    /** Star rewards, identical to the former onUserActionCreated mapping. */
    private fun rewardFor(actionType: String): Int = when (actionType) {
        "feed_cat" -> 2
        "vet_visit", "vet" -> 3
        "fill_station" -> 5
        "adopt_cat", "rescue" -> 10
        else -> 0
    }

    private val helpingActionTypes = setOf(
        "feed_cat", "rescue_cat", "rescue", "adopt_cat", "adopt",
        "vet_visit", "vet", "fill_station", "fill_feeding_station",
        "donate_supplies", "build_shelter"
    )

    suspend fun bumpGlobalStats(fields: Map<String, Long>) {
        try {
            val updates = fields.mapValues { com.google.firebase.firestore.FieldValue.increment(it.value) }
            firestore.collection("statistics").document("global")
                .set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepo", "Stats update skipped: ${e.message}")
        }
    }

    private suspend fun bumpOwnRewards(reportedCats: Long = 0, catBadges: Long = 0, stars: Long = 0) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val inc: MutableMap<String, Any> = mutableMapOf()
            if (reportedCats > 0) inc["reportedCatsCount"] = com.google.firebase.firestore.FieldValue.increment(reportedCats)
            if (catBadges > 0) inc["catBadges"] = com.google.firebase.firestore.FieldValue.increment(catBadges)
            if (stars > 0) {
                inc["totalStars"] = com.google.firebase.firestore.FieldValue.increment(stars)
                inc["rescueStars"] = com.google.firebase.firestore.FieldValue.increment(stars)
            }
            if (inc.isEmpty()) return
            firestore.collection("users").document(uid)
                .set(inc, com.google.firebase.firestore.SetOptions.merge()).await()
            firestore.collection("publicProfiles").document(uid)
                .set(inc, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepo", "Reward update skipped: ${e.message}")
        }
    }

    // Users
    suspend fun createUserProfile(userProfile: UserProfile) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).set(userProfile).await()
        // Maintain the public, non-identifying projection used by leaderboards
        // (/users documents are owner-private under the security rules).
        val publicProfile = mapOf(
            "displayName" to userProfile.displayName,
            "reportedCatsCount" to userProfile.reportedCatsCount,
            "rescueStars" to userProfile.rescueStars,
            "catBadges" to userProfile.catBadges,
            "totalStars" to userProfile.totalStars,
            "preferredLanguage" to userProfile.preferredLanguage
        )
        try {
            firestore.collection("publicProfiles").document(uid).set(publicProfile).await()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Failed to write publicProfile (leaderboard may not include this user)", e)
        }
        // Free-tier replacement for onUserCreated stat bumps.
        bumpGlobalStats(mapOf("totalUsers" to 1L, "totalRegisteredUsers" to 1L))
    }

    suspend fun updateUserMetadata(
        latitude: Double? = null,
        longitude: Double? = null,
        geohash: String? = null,
        fcmToken: String? = null,
        preferredLanguage: String? = null
    ) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>()
        if (latitude != null) updates["latitude"] = latitude
        if (longitude != null) updates["longitude"] = longitude
        if (geohash != null) updates["geohash"] = geohash
        if (fcmToken != null) updates["fcmToken"] = fcmToken
        if (preferredLanguage != null) updates["preferredLanguage"] = preferredLanguage
        if (updates.isNotEmpty()) {
            firestore.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
        }
    }

    fun getUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { subscription.remove() }
    }

    // Storage
    private val imageRelayUrl = "https://tinypaws-email.bochra0rhayem.workers.dev/upload"

    /**
     * Uploads an image through our Cloudflare Worker relay (free-tier replacement
     * for Firebase Storage, which requires a paid plan). The worker authenticates
     * the caller's Firebase ID token and forwards the image to the image host.
     * `folder` is forwarded as metadata for future host-side segmentation.
     */
    suspend fun uploadImageToStorage(
        uri: android.net.Uri,
        folder: String,
        context: android.content.Context
    ): String {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val token = user.getIdToken(true).await().token
            ?: throw IllegalStateException("Could not refresh sign-in credentials")
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read the selected image")
        if (bytes.size > 10 * 1024 * 1024) throw IllegalStateException("Image too large (max 10 MB)")

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val conn = java.net.URL("$imageRelayUrl?folder=${java.net.URLEncoder.encode(folder, "UTF-8")}")
                .openConnection() as java.net.HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = 20000
                conn.readTimeout = 90000
                conn.setRequestProperty("Content-Type", "image/jpeg")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.doOutput = true
                conn.outputStream.use { it.write(bytes) }

                val code = conn.responseCode
                val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                    ?.bufferedReader()?.readText().orEmpty()
                if (code !in 200..299) {
                    throw IllegalStateException("Image upload failed (HTTP $code)")
                }
                val json = org.json.JSONObject(body)
                if (json.optBoolean("success") != true || json.optString("url").isBlank()) {
                    throw IllegalStateException("Image host did not return a URL")
                }
                json.getString("url")
            } finally {
                conn.disconnect()
            }
        }
    }

    suspend fun deleteImageFromStorage(imageUrl: String) {
        try {
            // Only Firebase Storage URLs can be deleted this way; relay-hosted
            // images (imgbb) are intentionally left in place - deletion URLs are
            // returned at upload time and managed by the host's retention policy.
            if (imageUrl.startsWith("http") && imageUrl.contains("firebasestorage.googleapis.com")) {
                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageRef.delete().await()
                android.util.Log.d("FirebaseRepo", "Successfully deleted uploaded image from Storage")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Error deleting image from Storage", e)
        }
    }

    // Reports
    suspend fun createReport(report: CatReport, reportId: String = "") {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: ""
        val imgUrl = report.catImageUrl.ifBlank { report.photoUrl }
        // Privacy: the reporter's display name is intentionally NOT persisted.
        // Only the opaque UID (needed for "my reports" and notifications) is stored.
        val newReport = report.copy(
            reportedBy = uid,
            reporterName = "",
            catImageUrl = imgUrl,
            photoUrl = imgUrl,
            status = report.status.ifBlank { "active" }
        )
        android.util.Log.d("FirebaseRepo", "Writing report to collection 'reports' (reporter known: ${uid.isNotBlank()})")
        if (reportId.isNotBlank()) {
            firestore.collection("reports").document(reportId).set(newReport).await()
            android.util.Log.d("FirebaseRepo", "Report successfully written to Firestore")
        } else {
            firestore.collection("reports").add(newReport).await()
            android.util.Log.d("FirebaseRepo", "Report successfully written to Firestore with generated ID")
        }
        // Free-tier replacements for onReportCreated: reporter badges + global stat.
        if (uid.isNotBlank()) {
            bumpOwnRewards(reportedCats = 1, catBadges = 1)
        }
        bumpGlobalStats(mapOf("totalCatsReported" to 1L))
    }

    fun getActiveReports(): Flow<List<CatReport>> = callbackFlow {
        // Newest 150 only: bounds billable reads and memory.
        val subscription = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(150)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(CatReport::class.java) ?: emptyList()
                val activeList = list.filter { !it.rescued && (it.status.isBlank() || it.status == "active" || it.status == "helped") }
                trySend(activeList)
            }
        awaitClose { subscription.remove() }
    }

    fun getRescueStories(): Flow<List<CatReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(CatReport::class.java) ?: emptyList()
                val rescueList = list.filter { it.rescued || it.status == "rescued" }
                trySend(rescueList)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun reachCat(reportId: String) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Not signed in")

        // Atomic: the reach record AND the public reachedCount move together.
        val batch = firestore.batch()
        batch.set(
            firestore.collection("reports").document(reportId)
                .collection("reaches").document(uid),
            mapOf("createdAt" to FieldValue.serverTimestamp())
        )
        batch.update(
            firestore.collection("reports").document(reportId),
            mapOf("reachedCount" to FieldValue.increment(1))
        )
        batch.commit().await()
    }

    // NOTE: The former duplicate markAsRescued() here was removed - the single
    // implementation lives in FirestoreReportRepository (used by the ViewModel),
    // with reward recording handled explicitly at the call site.

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")

    suspend fun helpCat(reportId: String, actionType: String = "helped") {
        val uid = requireUid()

        // Atomic: recording the reach and flipping the report status succeed or fail together.
        val batch = firestore.batch()
        batch.set(
            firestore.collection("reports").document(reportId)
                .collection("reaches").document(uid),
            mapOf(
                "userId" to uid,
                "timestamp" to FieldValue.serverTimestamp(),
                "actionType" to actionType
            )
        )
        batch.update(
            firestore.collection("reports").document(reportId),
            mapOf(
                "status" to "helped",
                "helpedBy" to uid,
                "helpedAt" to FieldValue.serverTimestamp()
            )
        )
        // Free-tier replacement for onReportUpdated stats.
        batch.set(
            firestore.collection("statistics").document("global"),
            mapOf(
                "totalCatsHelped" to FieldValue.increment(1),
                "totalCatsRescued" to FieldValue.increment(1)
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.commit().await()

        // Record user action (triggers server-side stats/global update)
        val stars = if (actionType == "vet_visit") 3 else 2
        val desc = if (actionType == "vet_visit") "Took cat to veterinarian" else "Helped stray cat"
        recordUserAction(
            UserAction(
                actionType = actionType,
                starsEarned = stars,
                rewardAmount = stars,
                description = desc,
                relatedCatId = reportId,
                catId = reportId
            )
        )
    }

    suspend fun adoptCat(reportId: String) {
        val uid = requireUid()

        val batch = firestore.batch()
        batch.set(
            firestore.collection("reports").document(reportId)
                .collection("reaches").document(uid),
            mapOf(
                "userId" to uid,
                "timestamp" to FieldValue.serverTimestamp(),
                "actionType" to "adopt_cat"
            )
        )
        batch.update(
            firestore.collection("reports").document(reportId),
            mapOf(
                "status" to "adopted",
                "adoptedBy" to uid,
                "adoptedAt" to FieldValue.serverTimestamp()
            )
        )
        // Free-tier replacement for onReportUpdated stats.
        batch.set(
            firestore.collection("statistics").document("global"),
            mapOf(
                "totalAdoptedCats" to FieldValue.increment(1),
                "totalCatsHelped" to FieldValue.increment(1),
                "totalCatsRescued" to FieldValue.increment(1)
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.commit().await()

        // Record user action with 10 stars (triggers server-side stats/global update)
        recordUserAction(
            UserAction(
                actionType = "adopt_cat",
                starsEarned = 10,
                rewardAmount = 10,
                description = "Adopted/rescued cat permanently",
                relatedCatId = reportId,
                catId = reportId
            )
        )
    }

    suspend fun feedCat(reportId: String) {
        recordUserAction(
            UserAction(
                actionType = "feed_cat",
                starsEarned = 2,
                rewardAmount = 2,
                description = "Fed individual stray cat",
                relatedCatId = reportId,
                catId = reportId
            )
        )
    }

    suspend fun fillFeedingStation(stationId: String) {
        val uid = requireUid()

        // Atomic: reach + station status update in one batch.
        val batch = firestore.batch()
        batch.set(
            firestore.collection("feedingStations").document(stationId)
                .collection("reaches").document(uid),
            mapOf(
                "userId" to uid,
                "timestamp" to FieldValue.serverTimestamp(),
                "actionType" to "fill_station"
            )
        )
        batch.update(
            firestore.collection("feedingStations").document(stationId),
            mapOf(
                "foodAvailable" to true,
                "reachedCount" to FieldValue.increment(1),
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            firestore.collection("statistics").document("global"),
            mapOf("totalFeedingStationsReached" to FieldValue.increment(1)),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.commit().await()

        // Record user action with 5 stars (triggers server-side stats/global update)
        recordUserAction(
            UserAction(
                actionType = "fill_station",
                starsEarned = 5,
                rewardAmount = 5,
                description = "Filled feeding station",
                relatedStationId = stationId,
                stationId = stationId
            )
        )
    }

    suspend fun recordUserAction(action: UserAction) {
        val uid = requireUid()
        val reward = if (action.rewardAmount > 0) action.rewardAmount else rewardFor(action.actionType)
        firestore.collection("users").document(uid).collection("actions")
            .add(action.copy(rewardAmount = reward)).await()
        // Free-tier replacement for onUserActionCreated (monotonic, rule-guarded).
        // Global statistics are bumped by the calling functions (helpCat, adoptCat,
        // fillFeedingStation) in their own atomic batches to avoid double-counting.
        bumpOwnRewards(stars = reward.toLong())
    }

    fun getUserActions(userId: String): Flow<List<UserAction>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = firestore.collection("users").document(userId).collection("actions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(UserAction::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    fun getHelpedAndAdoptedCats(): Flow<List<CatReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val allReports = snapshot?.toObjects(CatReport::class.java) ?: emptyList()
                val helpedOrAdopted = allReports.filter {
                    it.status == "helped" || it.status == "adopted" || it.rescued || it.status == "rescued"
                }
                trySend(helpedOrAdopted)
            }
        awaitClose { subscription.remove() }
    }

    // Feeding Stations
    suspend fun createFeedingStation(station: FeedingStation) {
        val uid = requireUid()
        val newStation = station.copy(
            createdBy = uid,
            status = station.status.ifBlank { "active" }
        )
        firestore.collection("feedingStations").add(newStation).await()
        // Free-tier replacement for onStationCreated stats.
        bumpGlobalStats(mapOf("totalFeedingStations" to 1L))
    }

    fun getFeedingStations(): Flow<List<FeedingStation>> = callbackFlow {
        val subscription = firestore.collection("feedingStations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(FeedingStation::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun reachFeedingStation(stationId: String, foodAvailable: Boolean) {
        val uid = requireUid()

        // Atomic: reach + station counters in one batch.
        val batch = firestore.batch()
        batch.set(
            firestore.collection("feedingStations").document(stationId)
                .collection("reaches").document(uid),
            mapOf("createdAt" to FieldValue.serverTimestamp())
        )
        batch.update(
            firestore.collection("feedingStations").document(stationId),
            mapOf(
                "foodAvailable" to foodAvailable,
                "reachedCount" to FieldValue.increment(1),
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        )
        batch.set(
            firestore.collection("statistics").document("global"),
            mapOf("totalFeedingStationsReached" to FieldValue.increment(1)),
            com.google.firebase.firestore.SetOptions.merge()
        )
        batch.commit().await()
    }

    suspend fun incrementDownloads(count: Long = 1L) {
        try {
            val statsRef = firestore.collection("stats").document("global")
            statsRef.set(
                mapOf(
                    "downloads" to FieldValue.increment(count),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            android.util.Log.d("FirebaseRepo", "Incremented stats/global downloads by $count")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Error incrementing stats/global downloads", e)
        }
    }

    suspend fun initializeGlobalStatsIfMissing() {
        try {
            val statsRef = firestore.collection("stats").document("global")
            val snapshot = statsRef.get().await()
            if (!snapshot.exists()) {
                statsRef.set(
                    mapOf(
                        "downloads" to 0L,
                        "catsHelped" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                android.util.Log.d("FirebaseRepo", "Initialized stats/global with 0 downloads and 0 catsHelped")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Error initializing stats/global", e)
        }
    }

    fun getGlobalStats(): Flow<GlobalStats?> = callbackFlow {
        val subscription = firestore.collection("stats").document("global")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(GlobalStats::class.java))
            }
        awaitClose { subscription.remove() }
    }

    fun getGlobalStatistics(): Flow<GlobalStatistics?> = callbackFlow {
        val subscription = firestore.collection("statistics").document("global")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(GlobalStatistics::class.java))
            }
        awaitClose { subscription.remove() }
    }

    // Diary
    suspend fun saveDiaryEntry(entry: DiaryEntry) {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("diary").add(entry.copy(userId = uid)).await()
    }

    fun getDiaryEntries(): Flow<List<DiaryEntry>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = firestore.collection("users").document(uid).collection("diary")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(DiaryEntry::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteDiaryEntry(entryId: String) {
        val uid = requireUid()
        firestore.collection("users").document(uid).collection("diary").document(entryId).delete().await()
    }

    // Leaderboards read from the public, non-identifying projection so the
    // private /users documents (email, coordinates, tokens) are never exposed.
    fun getReportingChampions(): Flow<List<UserProfile>> = callbackFlow {
        val subscription = firestore.collection("publicProfiles")
            .orderBy("reportedCatsCount", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val profiles = snapshot?.documents?.mapNotNull { doc ->
                    UserProfile(
                        userId = doc.id,
                        displayName = doc.getString("displayName") ?: "",
                        reportedCatsCount = (doc.getLong("reportedCatsCount") ?: 0L).toInt(),
                        rescueStars = (doc.getLong("rescueStars") ?: 0L).toInt(),
                        catBadges = (doc.getLong("catBadges") ?: 0L).toInt(),
                        totalStars = (doc.getLong("totalStars") ?: 0L).toInt()
                    )
                } ?: emptyList()
                val filtered = profiles.filter { it.displayName.isNotBlank() && it.reportedCatsCount > 0 }
                trySend(filtered)
            }
        awaitClose { subscription.remove() }
    }

    fun getRescueChampions(): Flow<List<UserProfile>> = callbackFlow {
        val subscription = firestore.collection("publicProfiles")
            .orderBy("rescueStars", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val profiles = snapshot?.documents?.mapNotNull { doc ->
                    UserProfile(
                        userId = doc.id,
                        displayName = doc.getString("displayName") ?: "",
                        reportedCatsCount = (doc.getLong("reportedCatsCount") ?: 0L).toInt(),
                        rescueStars = (doc.getLong("rescueStars") ?: 0L).toInt(),
                        catBadges = (doc.getLong("catBadges") ?: 0L).toInt(),
                        totalStars = (doc.getLong("totalStars") ?: 0L).toInt()
                    )
                } ?: emptyList()
                val filtered = profiles.filter { it.displayName.isNotBlank() && it.rescueStars > 0 }
                trySend(filtered)
            }
        awaitClose { subscription.remove() }
    }
}
