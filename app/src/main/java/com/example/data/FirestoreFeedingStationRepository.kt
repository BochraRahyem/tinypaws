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
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(FeedingStation::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createFeedingStation(station: FeedingStation) {
        val uid = auth.currentUser?.uid ?: return
        val newStation = station.copy(createdBy = uid)
        firestore.collection("feedingStations").add(newStation).await()
    }

    suspend fun reachFeedingStation(stationId: String, foodAvailable: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val reachDoc = firestore.collection("feedingStations").document(stationId)
            .collection("reaches").document(uid)
        
        reachDoc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
        
        firestore.collection("feedingStations").document(stationId).update(
            mapOf(
                "foodAvailable" to foodAvailable,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
