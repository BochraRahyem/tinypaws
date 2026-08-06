package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
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

    suspend fun createReport(report: CatReport) {
        val uid = auth.currentUser?.uid ?: return
        val newReport = report.copy(reportedBy = uid)
        firestore.collection("reports").add(newReport).await()
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

    suspend fun reachCat(reportId: String) {
        val uid = auth.currentUser?.uid ?: return
        val reachDoc = firestore.collection("reports").document(reportId)
            .collection("reaches").document(uid)
        reachDoc.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
    }
}
