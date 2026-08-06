package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

object FirestoreBackupHelper {

    private const val TAG = "FirestoreBackupHelper"

    suspend fun backupDataToCloud(
        profile: CatProfile?,
        careLogs: List<DailyCareLog>,
        weightLogs: List<CatWeightLog>,
        diaryLogs: List<DiaryEntry>,
        reminders: List<Reminder>
    ): Result<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                val db = FirebaseFirestore.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val nowStr = dateFormat.format(Date())

                val profileMap = profile?.let {
                    mapOf(
                        "name" to it.name,
                        "ageYears" to it.ageYears,
                        "ageMonths" to it.ageMonths,
                        "coatColor" to it.coatColor
                    )
                } ?: emptyMap<String, Any>()

                val careLogsList = careLogs.map {
                    mapOf(
                        "dateString" to it.dateString,
                        "fed" to it.fed,
                        "watered" to it.watered,
                        "played" to it.played,
                        "litterCleaned" to it.litterCleaned,
                        "groomed" to it.groomed,
                        "medicationGiven" to it.medicationGiven
                    )
                }

                val weightLogsList = weightLogs.map {
                    mapOf(
                        "id" to it.id,
                        "date" to it.date,
                        "weight" to it.weight
                    )
                }

                val diaryLogsList = diaryLogs.map {
                    mapOf(
                        "id" to it.id,
                        "date" to it.date,
                        "mood" to it.mood,
                        "notes" to it.notes,
                        "weight" to (it.weight ?: 0f),
                        "photos" to it.photos,
                        "diaryEntryType" to it.diaryEntryType
                    )
                }

                val remindersList = reminders.map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "timeMillis" to it.timeMillis,
                        "type" to it.type
                    )
                }

                val backupPayload = hashMapOf(
                    "profile" to profileMap,
                    "dailyCareLogs" to careLogsList,
                    "weightLogs" to weightLogsList,
                    "diaryLogs" to diaryLogsList,
                    "reminders" to remindersList,
                    "lastBackupDate" to nowStr,
                    "timestamp" to System.currentTimeMillis()
                )

                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { authTask ->
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "device_user"
                    db.collection("users")
                        .document(uid)
                        .collection("backups")
                        .document("latest")
                        .set(backupPayload, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Firestore backup successful!")
                            continuation.resume(Result.success("Backup uploaded to cloud ($nowStr)"))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Firestore backup error", e)
                            continuation.resume(Result.failure(e))
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during cloud backup", e)
                continuation.resume(Result.failure(e))
            }
        }
    }

    suspend fun restoreDataFromCloud(
        catRepository: CatRepository
    ): Result<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                val db = FirebaseFirestore.getInstance()
                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "device_user"
                    db.collection("users")
                        .document(uid)
                        .collection("backups")
                        .document("latest")
                        .get()
                        .addOnSuccessListener { doc ->
                            if (doc == null || !doc.exists()) {
                                continuation.resume(Result.failure(Exception("No cloud backup found for this account.")))
                                return@addOnSuccessListener
                            }

                            try {
                                val data = doc.data ?: emptyMap<String, Any>()

                                // 1. Restore Profile
                                val profileMap = data["profile"] as? Map<*, *>
                                if (profileMap != null && profileMap.isNotEmpty()) {
                                    val name = profileMap["name"] as? String ?: "Whiskers"
                                    val ageYears = (profileMap["ageYears"] as? Number)?.toInt() ?: 2
                                    val ageMonths = (profileMap["ageMonths"] as? Number)?.toInt() ?: 0
                                    val coatColor = profileMap["coatColor"] as? String ?: "Orange Tabby"

                                    GlobalScope.launch(Dispatchers.IO) {
                                        catRepository.saveProfile(
                                            CatProfile(
                                                name = name,
                                                ageYears = ageYears,
                                                ageMonths = ageMonths,
                                                coatColor = coatColor
                                            )
                                        )
                                    }
                                }

                                // 2. Restore Care Logs
                                val careLogsList = data["dailyCareLogs"] as? List<Map<*, *>> ?: emptyList()
                                careLogsList.forEach { item ->
                                    val dateStr = item["dateString"] as? String ?: return@forEach
                                    val fed = item["fed"] as? Boolean ?: false
                                    val watered = item["watered"] as? Boolean ?: false
                                    val played = item["played"] as? Boolean ?: false
                                    val litterCleaned = item["litterCleaned"] as? Boolean ?: false
                                    val groomed = item["groomed"] as? Boolean ?: false
                                    val medicationGiven = item["medicationGiven"] as? Boolean ?: false

                                    GlobalScope.launch(Dispatchers.IO) {
                                        catRepository.saveCareLog(
                                            DailyCareLog(
                                                dateString = dateStr,
                                                fed = fed,
                                                watered = watered,
                                                played = played,
                                                litterCleaned = litterCleaned,
                                                groomed = groomed,
                                                medicationGiven = medicationGiven
                                            )
                                        )
                                    }
                                }

                                // 3. Restore Weight Logs
                                val weightLogsList = data["weightLogs"] as? List<Map<*, *>> ?: emptyList()
                                weightLogsList.forEach { item ->
                                    val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                                    val weight = (item["weight"] as? Number)?.toFloat() ?: 4.0f
                                    GlobalScope.launch(Dispatchers.IO) {
                                        catRepository.saveWeightLog(date, weight)
                                    }
                                }

                                // 4. Restore Diary / Check-in Logs
                                val diaryLogsList = data["diaryLogs"] as? List<Map<*, *>> ?: emptyList()
                                diaryLogsList.forEach { item ->
                                    val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                                    val mood = item["mood"] as? String ?: "happy"
                                    val healthStatus = item["healthStatus"] as? String ?: ""
                                    val weight = (item["weight"] as? Number)?.toFloat()
                                    val photos = item["photos"] as? String ?: ""
                                    val diaryEntryType = item["diaryEntryType"] as? String ?: "general"
                                    val remTime = (item["reminderTimeMillis"] as? Number)?.toLong()?.takeIf { it > 0 }

                                    GlobalScope.launch(Dispatchers.IO) {
                                        catRepository.saveCheckInLog(
                                            date = date,
                                            mood = mood,
                                            notes = healthStatus,
                                            weight = weight,
                                            photos = photos,
                                            diaryEntryType = diaryEntryType,
                                            reminderTimeMillis = remTime
                                        )
                                    }
                                }

                                // 5. Restore Reminders
                                val remindersList = data["reminders"] as? List<Map<*, *>> ?: emptyList()
                                remindersList.forEach { item ->
                                    val title = item["title"] as? String ?: return@forEach
                                    val timeMillis = (item["timeMillis"] as? Number)?.toLong() ?: return@forEach
                                    val type = item["type"] as? String ?: "general"

                                    GlobalScope.launch(Dispatchers.IO) {
                                        catRepository.saveReminder(title, timeMillis, type)
                                    }
                                }

                                val lastBackup = data["lastBackupDate"] as? String ?: "recent"
                                continuation.resume(Result.success("Successfully restored data from cloud ($lastBackup)!"))
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing cloud backup data", e)
                                continuation.resume(Result.failure(e))
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching cloud backup", e)
                            continuation.resume(Result.failure(e))
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during cloud restore", e)
                continuation.resume(Result.failure(e))
            }
        }
    }
}
