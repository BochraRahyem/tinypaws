package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreFeedingStationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun getFeedingStations(): Flow<List<FeedingStation>> = callbackFlow {
        val subscription = firestore.collection("feedingStations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(FeedingStation::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createFeedingStation(station: FeedingStation) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Not signed in")
        val newStation = station.copy(
            createdBy = uid,
            status = station.status.ifBlank { "active" }
        )
        firestore.collection("feedingStations").add(newStation).await()
        // Free-tier replacement for onStationCreated stats.
        try {
            firestore.collection("statistics").document("global").set(
                mapOf("totalFeedingStations" to FieldValue.increment(1)),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            android.util.Log.w("FirestoreFeedingStationRepo", "Stats update skipped: ${e.message}")
        }
    }

    suspend fun reachFeedingStation(stationId: String, foodAvailable: Boolean) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Not signed in")

        // P0-8: Check if this user already reached this station to prevent stats double-counting.
        val existingReach = firestore.collection("feedingStations").document(stationId)
            .collection("reaches").document(uid).get().await()
        val isDuplicate = existingReach.exists()

        // Atomic: the reach record AND the public counters move together.
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
        if (!isDuplicate) {
            batch.set(
                firestore.collection("statistics").document("global"),
                mapOf("totalFeedingStationsReached" to FieldValue.increment(1)),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
        batch.commit().await()
    }
}
