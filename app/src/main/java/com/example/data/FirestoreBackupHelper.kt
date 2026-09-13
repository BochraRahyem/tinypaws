package com.example.data

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cloud backup/restore for local cat data.
 *
 * Storage layout (users/{uid}/backups):
 *  - "latest"              -> metadata doc: lastBackupDate + per-collection counts
 *  - "latest_profiles"     -> { items: [...] }
 *  - "latest_careLogs"     -> { items: [...] }
 *  - "latest_weightLogs"   -> { items: [...] }
 *  - "latest_diaryLogs"    -> { items: [...] }
 *  - "latest_reminders"    -> { items: [...] }
 *  - "latest_historyEntries" -> { items: [...] }
 *
 * Splitting into one document per collection keeps each write well under the
 * Firestore 1 MiB document limit as history grows.
 *
 * Restore is IDEMPOTENT: every row is written with its original primary key via
 * REPLACE upserts, so running a restore twice can never duplicate rows.
 */
object FirestoreBackupHelper {

    private const val TAG = "FirestoreBackupHelper"

    suspend fun backupDataToCloud(
        allProfiles: List<CatProfile>,
        careLogs: List<DailyCareLog>,
        weightLogs: List<CatWeightLog>,
        diaryLogs: List<CatCheckInLog>,
        reminders: List<Reminder>,
        historyEntries: List<CatHistoryEntry>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val nowStr = dateFormat.format(Date())
            val backups = db.collection("users").document(user.uid).collection("backups")

            // Profiles keep their ids; child rows now export ids too so restores are idempotent.
            // Each write is awaited to guarantee ordering before the metadata document.
            Tasks.await(backups.document("latest_profiles").set(
                mapOf("items" to allProfiles.map { profile ->
                    mapOf(
                        "id" to profile.id,
                        "name" to profile.name,
                        "ageYears" to profile.ageYears,
                        "ageMonths" to profile.ageMonths,
                        "coatColor" to profile.coatColor,
                        "photoUrl" to profile.photoUrl,
                        "personality" to profile.personality,
                        "adoptionPhotoUrl" to profile.adoptionPhotoUrl,
                        "foodPreferences" to profile.foodPreferences,
                        "chronicConditions" to profile.chronicConditions
                    )
                }),
                SetOptions.merge()
            ), 10, java.util.concurrent.TimeUnit.SECONDS)

            Tasks.await(backups.document("latest_careLogs").set(
                mapOf("items" to careLogs.map { log ->
                    mapOf(
                        "catId" to log.catId,
                        "dateString" to log.dateString,
                        "fed" to log.fed,
                        "watered" to log.watered,
                        "played" to log.played,
                        "litterCleaned" to log.litterCleaned,
                        "groomed" to log.groomed,
                        "medicationGiven" to log.medicationGiven,
                        "lastUpdated" to log.lastUpdated
                    )
                }),
                SetOptions.merge()
            ), 10, java.util.concurrent.TimeUnit.SECONDS)

            Tasks.await(backups.document("latest_weightLogs").set(
                mapOf("items" to weightLogs.map { log ->
                    mapOf(
                        "id" to log.id,
                        "catId" to log.catId,
                        "date" to log.date,
                        "weight" to log.weight
                    )
                }),
                SetOptions.merge()
            ), 10, java.util.concurrent.TimeUnit.SECONDS)

            Tasks.await(backups.document("latest_diaryLogs").set(
                mapOf("items" to diaryLogs.map { log ->
                    mapOf(
                        "id" to log.id,
                        "catId" to log.catId,
                        "date" to log.date,
                        "mood" to log.mood,
                        "healthStatus" to log.healthStatus,
                        // Preserve null-ness instead of fabricating a 0.0 kg entry.
                        "weight" to log.weight,
                        "photos" to log.photos,
                        "category" to log.category,
                        "reminderTimeMillis" to (log.reminderTimeMillis ?: 0L)
                    )
                }),
                SetOptions.merge()
            ), 10, java.util.concurrent.TimeUnit.SECONDS)

            Tasks.await(backups.document("latest_reminders").set(
                mapOf("items" to reminders.map { reminder ->
                    mapOf(
                        "id" to reminder.id,
                        "catId" to reminder.catId,
                        "catIds" to reminder.catIds,
                        "title" to reminder.title,
                        "timeMillis" to reminder.timeMillis,
                        "type" to reminder.type,
                        "isRecurring" to reminder.isRecurring,
                        "isEnabled" to reminder.isEnabled
                    )
                }),
                SetOptions.merge()
            ), 10, java.util.concurrent.TimeUnit.SECONDS)

            Tasks.await(backups.document("latest_historyEntries").set(
                mapOf("items" to historyEntries.map { entry ->
                    mapOf(
                        "id" to entry.id,
                        "catId" to entry.catId,
                        "title" to entry.title,
                        "date" to entry.date,
                        "category" to entry.category,
                        "notes" to entry.notes
                    )
                }),
                SetOptions.merge()
            ), 10, java.util.concurrent.TimeUnit.SECONDS)

            // Metadata document written last marks the backup as complete.
            val metaTask = backups.document("latest").set(
                mapOf(
                    "lastBackupDate" to nowStr,
                    "layout" to "sharded_v1",
                    "counts" to mapOf(
                        "profiles" to allProfiles.size,
                        "careLogs" to careLogs.size,
                        "weightLogs" to weightLogs.size,
                        "diaryLogs" to diaryLogs.size,
                        "reminders" to reminders.size,
                        "historyEntries" to historyEntries.size
                    )
                ),
                SetOptions.merge()
            )

            try {
                Tasks.await(metaTask, 5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                Result.success("Backup uploaded to cloud ($nowStr)")
            } catch (e: java.util.concurrent.TimeoutException) {
                // Offline: the write stays queued locally. Track the outcome so we
                // never claim success if the queued write later fails permanently.
                metaTask.addOnFailureListener { failure ->
                    Log.e(TAG, "Queued cloud backup ultimately failed", failure)
                }
                Result.success("Backup queued offline ($nowStr)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud backup failed", e)
            Result.failure(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun restoreDataFromCloud(
        catRepository: CatRepository
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))

            val backups = db.collection("users").document(user.uid).collection("backups")
            val doc = Tasks.await(backups.document("latest").get())

            if (doc == null || !doc.exists()) {
                return@withContext Result.failure(Exception("No cloud backup found for this account."))
            }

            val data = doc.data ?: return@withContext Result.failure(Exception("Backup data is empty."))
            val sharded = (data["layout"] as? String) == "sharded_v1"

            fun readItems(name: String): List<Map<*, *>>? {
                return if (sharded) {
                    val shardDoc = Tasks.await(backups.document("latest_$name").get())
                    if (shardDoc == null || !shardDoc.exists()) null
                    else (shardDoc.data?.get("items") as? List<Map<*, *>>) ?: emptyList()
                } else {
                    // Legacy single-document layout from older app versions.
                    @Suppress("UNCHECKED_CAST")
                    data[name] as? List<Map<*, *>>
                }
            }

            var restoredRows = 0

            // 1. Restore Profiles (max 7)
            readItems("profiles")?.take(7)?.forEach { profileMap ->
                val cId = (profileMap["id"] as? Number)?.toInt() ?: 1
                catRepository.saveProfile(
                    CatProfile(
                        id = cId,
                        name = profileMap["name"] as? String ?: "Cat",
                        ageYears = (profileMap["ageYears"] as? Number)?.toInt() ?: 2,
                        ageMonths = (profileMap["ageMonths"] as? Number)?.toInt() ?: 0,
                        coatColor = profileMap["coatColor"] as? String ?: "Calico",
                        photoUrl = profileMap["photoUrl"] as? String,
                        personality = profileMap["personality"] as? String ?: "",
                        adoptionPhotoUrl = profileMap["adoptionPhotoUrl"] as? String,
                        foodPreferences = profileMap["foodPreferences"] as? String ?: "",
                        chronicConditions = profileMap["chronicConditions"] as? String
                    )
                )
                restoredRows++
            }
            if (!sharded) {
                // Legacy fallback: single "profile" map from very old versions.
                val profileMap = data["profile"] as? Map<*, *>
                if (profileMap != null && profileMap.isNotEmpty() && restoredRows == 0) {
                    catRepository.saveProfile(
                        CatProfile(
                            id = 1,
                            name = profileMap["name"] as? String ?: "Whiskers",
                            ageYears = (profileMap["ageYears"] as? Number)?.toInt() ?: 2,
                            ageMonths = (profileMap["ageMonths"] as? Number)?.toInt() ?: 0,
                            coatColor = profileMap["coatColor"] as? String ?: "Orange Tabby",
                            photoUrl = profileMap["photoUrl"] as? String,
                            personality = profileMap["personality"] as? String ?: "",
                            adoptionPhotoUrl = profileMap["adoptionPhotoUrl"] as? String,
                            foodPreferences = profileMap["foodPreferences"] as? String ?: "",
                            chronicConditions = profileMap["chronicConditions"] as? String
                        )
                    )
                    restoredRows++
                }
            }

            // 2. Restore Care Logs (composite key makes these naturally idempotent)
            readItems("careLogs")?.forEach { item ->
                val dateStr = item["dateString"] as? String ?: return@forEach
                catRepository.saveCareLog(
                    DailyCareLog(
                        catId = (item["catId"] as? Number)?.toInt() ?: 1,
                        dateString = dateStr,
                        fed = item["fed"] as? Boolean ?: false,
                        watered = item["watered"] as? Boolean ?: false,
                        played = item["played"] as? Boolean ?: false,
                        litterCleaned = item["litterCleaned"] as? Boolean ?: false,
                        groomed = item["groomed"] as? Boolean ?: false,
                        medicationGiven = item["medicationGiven"] as? Boolean ?: false,
                        lastUpdated = (item["lastUpdated"] as? Number)?.toLong() ?: 0L
                    )
                )
                restoredRows++
            }

            // 3. Restore Weight Logs (explicit id => repeat restores cannot duplicate)
            readItems("weightLogs")?.forEach { item ->
                val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                val hasId = item["id"] != null
                if (!hasId) {
                    // Legacy row without id: fall back to append semantics.
                    catRepository.saveWeightLog(
                        date,
                        (item["weight"] as? Number)?.toFloat() ?: 4.0f,
                        catId = (item["catId"] as? Number)?.toInt() ?: 1
                    )
                } else {
                    catRepository.restoreWeightLog(
                        CatWeightLog(
                            id = (item["id"] as? Number)?.toInt() ?: 0,
                            catId = (item["catId"] as? Number)?.toInt() ?: 1,
                            date = date,
                            weight = (item["weight"] as? Number)?.toFloat() ?: 0f
                        )
                    )
                }
                restoredRows++
            }

            // 4. Restore Diary / Check-in Logs
            readItems("diaryLogs")?.forEach { item ->
                val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                val mood = item["mood"] as? String ?: "happy"
                val notes = item["notes"] as? String ?: (item["healthStatus"] as? String ?: "")
                val weight = (item["weight"] as? Number)?.toFloat()
                val photos = item["photos"] as? String ?: ""
                val diaryEntryType = item["category"] as? String ?: (item["diaryEntryType"] as? String ?: "general")
                val remTime = (item["reminderTimeMillis"] as? Number)?.toLong()?.takeIf { it > 0 }

                val legacyId = (item["id"] as? Number)?.toInt() ?: 0
                if (legacyId > 0) {
                    catRepository.restoreCheckInLog(
                        CatCheckInLog(
                            id = legacyId,
                            catId = (item["catId"] as? Number)?.toInt() ?: 1,
                            date = date,
                            mood = mood,
                            healthStatus = notes,
                            weight = weight,
                            photos = photos,
                            category = diaryEntryType,
                            reminderTimeMillis = remTime
                        )
                    )
                } else {
                    catRepository.saveCheckInLog(
                        catId = (item["catId"] as? Number)?.toInt() ?: 1,
                        date = date,
                        mood = mood,
                        notes = notes,
                        weight = weight,
                        photos = photos,
                        diaryEntryType = diaryEntryType,
                        reminderTimeMillis = remTime
                    )
                }
                restoredRows++
            }

            // 5. Restore Reminders (defaults match Reminder entity, not old assumptions)
            readItems("reminders")?.forEach { item ->
                val title = item["title"] as? String ?: return@forEach
                val timeMillis = (item["timeMillis"] as? Number)?.toLong() ?: return@forEach
                val remId = (item["id"] as? Number)?.toInt() ?: 0
                catRepository.saveReminder(
                    Reminder(
                        id = if (remId > 0) remId else 0,
                        catId = (item["catId"] as? Number)?.toInt() ?: 1,
                        catIds = item["catIds"] as? String ?: "",
                        title = title,
                        timeMillis = timeMillis,
                        type = item["type"] as? String ?: "general",
                        isRecurring = item["isRecurring"] as? Boolean ?: false,
                        isEnabled = item["isEnabled"] as? Boolean ?: true
                    )
                )
                restoredRows++
            }

            // 6. Restore History Entries
            readItems("historyEntries")?.forEach { item ->
                val title = item["title"] as? String ?: return@forEach
                val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                val entryId = (item["id"] as? Number)?.toInt() ?: 0
                catRepository.restoreHistoryEntry(
                    CatHistoryEntry(
                        id = entryId,
                        catId = (item["catId"] as? Number)?.toInt() ?: 1,
                        title = title,
                        date = date,
                        category = item["category"] as? String ?: "Other",
                        notes = item["notes"] as? String ?: ""
                    )
                )
                restoredRows++
            }

            val lastBackup = data["lastBackupDate"] as? String ?: "recent"
            Result.success("Successfully restored $restoredRows rows from cloud ($lastBackup)!")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring cloud backup data", e)
            Result.failure(e)
        }
    }
}
