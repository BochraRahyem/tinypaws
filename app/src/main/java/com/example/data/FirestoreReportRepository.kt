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
        // Newest 150 only: bounds billable reads and memory instead of streaming
        // the entire collection on every change.
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
        // Ordered by createdAt (rescuedAt is absent on legacy documents, and
        // Firestore excludes documents missing the sort field).
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
        android.util.Log.d("FirestoreReportRepo", "Writing report to collection 'reports' (reporter known: ${uid.isNotBlank()})")
        if (reportId.isNotBlank()) {
            firestore.collection("reports").document(reportId).set(newReport).await()
            android.util.Log.d("FirestoreReportRepo", "Report successfully written to Firestore")
        } else {
            firestore.collection("reports").add(newReport).await()
            android.util.Log.d("FirestoreReportRepo", "Report successfully written to Firestore with generated ID")
        }
    }

    /** Returns true if the report was actually updated (not already rescued). */
    suspend fun markAsRescued(reportId: String, photoUrl: String, description: String): Boolean {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Not signed in")

        // P0-8: Check if this report was already rescued to prevent stats double-counting.
        val reportDoc = firestore.collection("reports").document(reportId).get().await()
        val alreadyRescued = reportDoc.getBoolean("rescued") == true

        if (!alreadyRescued) {
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
            // Free-tier replacement for onReportUpdated rescue stats — only on first rescue.
            // Bounded by Firestore rules (max +5 per write). Client-side duplicate
            // check (alreadyRescued) prevents the same user from counting twice.
            try {
                firestore.collection("statistics").document("global").set(
                    mapOf(
                        "totalCatsHelped" to FieldValue.increment(1),
                        "totalCatsRescued" to FieldValue.increment(1)
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                android.util.Log.w("FirestoreReportRepo", "Stats update skipped: ${e.message}")
            }
            return true
        }
        return false
    }

    suspend fun reachCat(reportId: String) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Not signed in")

        // P0-8: Check if this user already reached this report to prevent stats double-counting.
        val existingReach = firestore.collection("reports").document(reportId)
            .collection("reaches").document(uid).get().await()
        val isDuplicate = existingReach.exists()

        // Atomic: the reach record AND the public reachedCount move together.
        val batch = firestore.batch()
        batch.set(
            firestore.collection("reports").document(reportId)
                .collection("reaches").document(uid),
            mapOf("createdAt" to FieldValue.serverTimestamp())
        )
        if (!isDuplicate) {
            batch.update(
                firestore.collection("reports").document(reportId),
                mapOf("reachedCount" to FieldValue.increment(1))
            )
        }
        batch.commit().await()
    }
}
