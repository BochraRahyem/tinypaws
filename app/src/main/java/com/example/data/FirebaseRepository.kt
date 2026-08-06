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

    fun getUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(UserProfile::class.java))
            }
        awaitClose { subscription.remove() }
    }

    // Reports
    suspend fun createReport(report: CatReport) {
        val uid = auth.currentUser?.uid ?: return
        val newReport = report.copy(reportedBy = uid)
        firestore.collection("reports").add(newReport).await()
    }

    fun getActiveReports(): Flow<List<CatReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(CatReport::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    fun getRescueStories(): Flow<List<CatReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .whereEqualTo("rescued", true)
            .orderBy("rescuedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(CatReport::class.java) ?: emptyList())
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
    }

    // Feeding Stations
    suspend fun createFeedingStation(station: FeedingStation) {
        val uid = auth.currentUser?.uid ?: return
        val newStation = station.copy(createdBy = uid)
        firestore.collection("feedingStations").add(newStation).await()
    }

    fun getFeedingStations(): Flow<List<FeedingStation>> = callbackFlow {
        val subscription = firestore.collection("feedingStations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
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
            .addSnapshotListener { snapshot, _ ->
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
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            return@callbackFlow
        }
        val subscription = firestore.collection("users").document(uid).collection("diary")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(DiaryEntry::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteDiaryEntry(entryId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).collection("diary").document(entryId).delete().await()
    }
}
