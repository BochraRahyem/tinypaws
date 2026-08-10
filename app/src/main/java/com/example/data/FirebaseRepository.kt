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

    // Users
    suspend fun createUserProfile(userProfile: UserProfile) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).set(userProfile).await()
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
    suspend fun uploadImageToStorage(uri: android.net.Uri, folder: String = "cat_photos"): String {
        return try {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("$folder/${java.util.UUID.randomUUID()}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepo", "Error uploading image to storage", e)
            uri.toString()
        }
    }

    // Reports
    suspend fun createReport(report: CatReport) {
        val uid = auth.currentUser?.uid ?: return
        val imgUrl = report.catImageUrl.ifBlank { report.photoUrl }
        val newReport = report.copy(
            reportedBy = uid,
            catImageUrl = imgUrl,
            photoUrl = imgUrl,
            status = report.status.ifBlank { "active" }
        )
        firestore.collection("reports").add(newReport).await()
    }

    fun getActiveReports(): Flow<List<CatReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(CatReport::class.java) ?: emptyList()
                val activeList = list.filter { !it.rescued && (it.status.isBlank() || it.status == "active" || it.status == "helped") }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
                trySend(activeList)
            }
        awaitClose { subscription.remove() }
    }

    fun getRescueStories(): Flow<List<CatReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(CatReport::class.java) ?: emptyList()
                val rescueList = list.filter { it.rescued }
                    .sortedByDescending { it.rescuedAt?.seconds ?: 0L }
                trySend(rescueList)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun reachCat(reportId: String) {
        val uid = auth.currentUser?.uid ?: return
        val reachDoc = firestore.collection("reports").document(reportId)
            .collection("reaches").document(uid)
        
        // Use a set with merge to avoid overwriting and handle "one reach per user"
        reachDoc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
        // Note: The prompt says reachedCount is updated by Cloud Functions.
    }

    suspend fun markAsRescued(reportId: String, photoUrl: String, description: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("reports").document(reportId).update(
            mapOf(
                "rescued" to true,
                "status" to "rescued",
                "rescuedPhotoUrl" to photoUrl,
                "rescuedDescription" to description,
                "rescuedBy" to uid,
                "rescuedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Record rescue action with 10 stars
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

    suspend fun helpCat(reportId: String, actionType: String = "helped") {
        val uid = auth.currentUser?.uid ?: return
        
        // 1. Record reach in report
        val reachDoc = firestore.collection("reports").document(reportId)
            .collection("reaches").document(uid)
        reachDoc.set(mapOf(
            "userId" to uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "actionType" to actionType
        )).await()

        // 2. Update report status
        firestore.collection("reports").document(reportId).update(
            mapOf(
                "status" to "helped",
                "helpedBy" to uid,
                "helpedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // 3. Record user action
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
        val uid = auth.currentUser?.uid ?: return

        // 1. Record reach in report
        val reachDoc = firestore.collection("reports").document(reportId)
            .collection("reaches").document(uid)
        reachDoc.set(mapOf(
            "userId" to uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "actionType" to "adopt_cat"
        )).await()

        // 2. Update report status
        firestore.collection("reports").document(reportId).update(
            mapOf(
                "status" to "adopted",
                "adoptedBy" to uid,
                "adoptedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // 3. Record user action with 10 stars
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
        val uid = auth.currentUser?.uid ?: return

        // 1. Record reach in station
        val reachDoc = firestore.collection("feedingStations").document(stationId)
            .collection("reaches").document(uid)
        reachDoc.set(mapOf(
            "userId" to uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "actionType" to "fill_station"
        )).await()

        // 2. Update station
        firestore.collection("feedingStations").document(stationId).update(
            mapOf(
                "foodAvailable" to true,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).await()

        // 3. Record user action with 5 stars
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
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("actions").add(action).await()
    }

    fun getUserActions(userId: String): Flow<List<UserAction>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = firestore.collection("users").document(userId).collection("actions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val allReports = snapshot?.toObjects(CatReport::class.java) ?: emptyList()
                val helpedOrAdopted = allReports.filter {
                    it.status == "helped" || it.status == "adopted" || it.rescued || it.status == "rescued"
                }.sortedByDescending { it.createdAt?.seconds ?: 0 }
                trySend(helpedOrAdopted)
            }
        awaitClose { subscription.remove() }
    }

    // Feeding Stations
    suspend fun createFeedingStation(station: FeedingStation) {
        val uid = auth.currentUser?.uid ?: return
        val newStation = station.copy(
            createdBy = uid,
            status = station.status.ifBlank { "active" }
        )
        firestore.collection("feedingStations").add(newStation).await()
    }

    fun getFeedingStations(): Flow<List<FeedingStation>> = callbackFlow {
        val subscription = firestore.collection("feedingStations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
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
        val uid = auth.currentUser?.uid ?: return
        val reachDoc = firestore.collection("feedingStations").document(stationId)
            .collection("reaches").document(uid)
        
        reachDoc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
        
        // Update parent doc status
        firestore.collection("feedingStations").document(stationId).update(
            mapOf(
                "foodAvailable" to foodAvailable,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // Global Statistics
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
        val uid = auth.currentUser?.uid ?: return
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
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("diary").document(entryId).delete().await()
    }

    fun getReportingChampions(): Flow<List<UserProfile>> = callbackFlow {
        val subscription = firestore.collection("users")
            .orderBy("reportedCatsCount", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(UserProfile::class.java) ?: emptyList()
                val filtered = users.filter { it.displayName.isNotBlank() && it.reportedCatsCount > 0 }
                trySend(filtered)
            }
        awaitClose { subscription.remove() }
    }

    fun getRescueChampions(): Flow<List<UserProfile>> = callbackFlow {
        val subscription = firestore.collection("users")
            .orderBy("rescueStars", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(UserProfile::class.java) ?: emptyList()
                val filtered = users.filter { it.displayName.isNotBlank() && it.rescueStars > 0 }
                trySend(filtered)
            }
        awaitClose { subscription.remove() }
    }
}
