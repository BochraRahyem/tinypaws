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

            val data = hashMapOf(
                "lastBackupDate" to nowStr,
                "profiles" to allProfiles.map { profile ->
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
                },
                "dailyCareLogs" to careLogs.map { log ->
                    mapOf(
                        "catId" to log.catId,
                        "dateString" to log.dateString,
                        "fed" to log.fed,
                        "watered" to log.watered,
                        "played" to log.played,
                        "litterCleaned" to log.litterCleaned,
                        "groomed" to log.groomed,
                        "medicationGiven" to log.medicationGiven
                    )
                },
                "weightLogs" to weightLogs.map { log ->
                    mapOf(
                        "catId" to log.catId,
                        "date" to log.date,
                        "weight" to log.weight
                    )
                },
                "diaryLogs" to diaryLogs.map { log ->
                    mapOf(
                        "catId" to log.catId,
                        "date" to log.date,
                        "mood" to log.mood,
                        "healthStatus" to log.healthStatus,
                        "weight" to (log.weight ?: 0f),
                        "photos" to log.photos,
                        "category" to log.category,
                        "reminderTimeMillis" to (log.reminderTimeMillis ?: 0L)
                    )
                },
                "reminders" to reminders.map { reminder ->
                    mapOf(
                        "catId" to reminder.catId,
                        "catIds" to reminder.catIds,
                        "title" to reminder.title,
                        "timeMillis" to reminder.timeMillis,
                        "type" to reminder.type,
                        "isRecurring" to reminder.isRecurring,
                        "isEnabled" to reminder.isEnabled
                    )
                },
                "historyEntries" to historyEntries.map { entry ->
                    mapOf(
                        "catId" to entry.catId,
                        "title" to entry.title,
                        "date" to entry.date,
                        "category" to entry.category,
                        "notes" to entry.notes
                    )
                }
            )

            val task = db.collection("users")
                .document(user.uid)
                .collection("backups")
                .document("latest")
                .set(data, com.google.firebase.firestore.SetOptions.merge())
            
            try {
                com.google.android.gms.tasks.Tasks.await(task, 1500, java.util.concurrent.TimeUnit.MILLISECONDS)
                Result.success("Backup uploaded to cloud ($nowStr)")
            } catch (e: java.util.concurrent.TimeoutException) {
                // Timeout means we're offline and the write is queued locally.
                Result.success("Backup queued offline ($nowStr)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud backup failed", e)
            Result.failure(e)
        }
    }

    suspend fun restoreDataFromCloud(
        catRepository: CatRepository
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("User not logged in"))

            val doc = Tasks.await(
                db.collection("users")
                    .document(user.uid)
                    .collection("backups")
                    .document("latest")
                    .get()
            )

            if (doc == null || !doc.exists()) {
                return@withContext Result.failure(Exception("No cloud backup found for this account."))
            }

            val data = doc.data ?: return@withContext Result.failure(Exception("Backup data is empty."))

            // 1. Restore Profiles (max 7)
            val profilesList = data["profiles"] as? List<Map<*, *>>
            if (profilesList != null) {
                profilesList.take(7).forEach { profileMap ->
                    val cId = (profileMap["id"] as? Number)?.toInt() ?: 1
                    val name = profileMap["name"] as? String ?: "Cat"
                    val ageYears = (profileMap["ageYears"] as? Number)?.toInt() ?: 2
                    val ageMonths = (profileMap["ageMonths"] as? Number)?.toInt() ?: 0
                    val coatColor = profileMap["coatColor"] as? String ?: "Calico"
                    val photoUrl = profileMap["photoUrl"] as? String
                    val personality = profileMap["personality"] as? String ?: ""
                    val adoptionPhotoUrl = profileMap["adoptionPhotoUrl"] as? String
                    val foodPreferences = profileMap["foodPreferences"] as? String ?: ""
                    val chronicConditions = profileMap["chronicConditions"] as? String

                    catRepository.saveProfile(
                        CatProfile(
                            id = cId,
                            name = name,
                            ageYears = ageYears,
                            ageMonths = ageMonths,
                            coatColor = coatColor,
                            photoUrl = photoUrl,
                            personality = personality,
                            adoptionPhotoUrl = adoptionPhotoUrl,
                            foodPreferences = foodPreferences,
                            chronicConditions = chronicConditions
                        )
                    )
                }
            } else {
                val profileMap = data["profile"] as? Map<*, *>
                if (profileMap != null && profileMap.isNotEmpty()) {
                    val name = profileMap["name"] as? String ?: "Whiskers"
                    val ageYears = (profileMap["ageYears"] as? Number)?.toInt() ?: 2
                    val ageMonths = (profileMap["ageMonths"] as? Number)?.toInt() ?: 0
                    val coatColor = profileMap["coatColor"] as? String ?: "Orange Tabby"
                    val photoUrl = profileMap["photoUrl"] as? String
                    val personality = profileMap["personality"] as? String ?: ""
                    val adoptionPhotoUrl = profileMap["adoptionPhotoUrl"] as? String
                    val foodPreferences = profileMap["foodPreferences"] as? String ?: ""
                    val chronicConditions = profileMap["chronicConditions"] as? String

                    catRepository.saveProfile(
                        CatProfile(
                            id = 1,
                            name = name,
                            ageYears = ageYears,
                            ageMonths = ageMonths,
                            coatColor = coatColor,
                            photoUrl = photoUrl,
                            personality = personality,
                            adoptionPhotoUrl = adoptionPhotoUrl,
                            foodPreferences = foodPreferences,
                            chronicConditions = chronicConditions
                        )
                    )
                }
            }

            // 2. Restore Care Logs
            val careLogsList = data["dailyCareLogs"] as? List<Map<*, *>> ?: emptyList()
            careLogsList.forEach { item ->
                val catId = (item["catId"] as? Number)?.toInt() ?: 1
                val dateStr = item["dateString"] as? String ?: return@forEach
                val fed = item["fed"] as? Boolean ?: false
                val watered = item["watered"] as? Boolean ?: false
                val played = item["played"] as? Boolean ?: false
                val litterCleaned = item["litterCleaned"] as? Boolean ?: false
                val groomed = item["groomed"] as? Boolean ?: false
                val medicationGiven = item["medicationGiven"] as? Boolean ?: false

                catRepository.saveCareLog(
                    DailyCareLog(
                        catId = catId,
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

            // 3. Restore Weight Logs
            val weightLogsList = data["weightLogs"] as? List<Map<*, *>> ?: emptyList()
            weightLogsList.forEach { item ->
                val catId = (item["catId"] as? Number)?.toInt() ?: 1
                val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                val weight = (item["weight"] as? Number)?.toFloat() ?: 4.0f
                catRepository.saveWeightLog(date, weight, catId = catId)
            }

            // 4. Restore Diary / Check-in Logs
            val diaryLogsList = data["diaryLogs"] as? List<Map<*, *>> ?: emptyList()
            diaryLogsList.forEach { item ->
                val catId = (item["catId"] as? Number)?.toInt() ?: 1
                val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                val mood = item["mood"] as? String ?: "happy"
                val notes = item["notes"] as? String ?: (item["healthStatus"] as? String ?: "")
                val weight = (item["weight"] as? Number)?.toFloat()
                val photos = item["photos"] as? String ?: ""
                val diaryEntryType = item["category"] as? String ?: (item["diaryEntryType"] as? String ?: "general")
                val remTime = (item["reminderTimeMillis"] as? Number)?.toLong()?.takeIf { it > 0 }

                catRepository.saveCheckInLog(
                    catId = catId,
                    date = date,
                    mood = mood,
                    notes = notes,
                    weight = weight,
                    photos = photos,
                    diaryEntryType = diaryEntryType,
                    reminderTimeMillis = remTime
                )
            }

            // 5. Restore Reminders
            val remindersList = data["reminders"] as? List<Map<*, *>> ?: emptyList()
            remindersList.forEach { item ->
                val catId = (item["catId"] as? Number)?.toInt() ?: 1
                val catIds = item["catIds"] as? String ?: "all"
                val title = item["title"] as? String ?: return@forEach
                val timeMillis = (item["timeMillis"] as? Number)?.toLong() ?: return@forEach
                val type = item["type"] as? String ?: "general"
                val isRecurring = item["isRecurring"] as? Boolean ?: true
                val isEnabled = item["isEnabled"] as? Boolean ?: true

                catRepository.saveReminder(
                    Reminder(
                        catId = catId,
                        catIds = catIds,
                        title = title,
                        timeMillis = timeMillis,
                        type = type,
                        isRecurring = isRecurring,
                        isEnabled = isEnabled
                    )
                )
            }

            // 6. Restore History Logs
            val historyList = data["historyEntries"] as? List<Map<*, *>> ?: emptyList()
            historyList.forEach { item ->
                val catId = (item["catId"] as? Number)?.toInt() ?: 1
                val title = item["title"] as? String ?: return@forEach
                val date = (item["date"] as? Number)?.toLong() ?: return@forEach
                val category = item["category"] as? String ?: "Other"
                val notes = item["notes"] as? String ?: ""

                catRepository.saveHistoryEntry(
                    CatHistoryEntry(
                        catId = catId,
                        title = title,
                        date = date,
                        category = category,
                        notes = notes
                    )
                )
            }

            val lastBackup = data["lastBackupDate"] as? String ?: "recent"
            Result.success("Successfully restored data from cloud ($lastBackup)!")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring cloud backup data", e)
            Result.failure(e)
        }
    }
}
