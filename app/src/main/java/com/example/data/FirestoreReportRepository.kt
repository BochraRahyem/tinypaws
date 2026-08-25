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

    suspend fun createReport(report: CatReport, reportId: String = "") {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: ""
        val rawName = report.reporterName.trim()
        val reporterName = when {
            rawName.isNotEmpty() -> rawName
            currentUser != null -> currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Community Member"
            else -> "Guest Reporter"
        }
        val imgUrl = report.catImageUrl.ifBlank { report.photoUrl }
        val newReport = report.copy(
            reportedBy = uid,
            reporterName = reporterName,
            catImageUrl = imgUrl,
            photoUrl = imgUrl,
            status = report.status.ifBlank { "active" }
        )
        android.util.Log.d("FirestoreReportRepo", "Writing report to collection 'reports' with ID: $reportId for reporter: $reporterName (UID: $uid)")
        if (reportId.isNotBlank()) {
            firestore.collection("reports").document(reportId).set(newReport).await()
            android.util.Log.d("FirestoreReportRepo", "Report successfully written to Firestore with ID: $reportId")
        } else {
            val docRef = firestore.collection("reports").add(newReport).await()
            android.util.Log.d("FirestoreReportRepo", "Report successfully written to Firestore with generated ID: ${docRef.id}")
        }
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
