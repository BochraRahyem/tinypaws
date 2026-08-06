package com.example.data

import kotlinx.coroutines.flow.Flow

class CatRepository(
    private val catProfileDao: CatProfileDao,
    private val catWeightLogDao: CatWeightLogDao,
    private val catCheckInLogDao: CatCheckInLogDao,
    private val reminderDao: ReminderDao,
    private val dailyCareLogDao: DailyCareLogDao
) {
    val catProfile: Flow<CatProfile?> = catProfileDao.getCatProfile()
    val allWeightLogs: Flow<List<CatWeightLog>> = catWeightLogDao.getAllWeightLogs()
    val allCheckInLogs: Flow<List<CatCheckInLog>> = catCheckInLogDao.getAllCheckInLogs()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allCareLogs: Flow<List<DailyCareLog>> = dailyCareLogDao.getAllCareLogs()

    fun getCareLogForDate(dateStr: String): Flow<DailyCareLog?> = dailyCareLogDao.getCareLogForDate(dateStr)

    suspend fun saveCareLog(careLog: DailyCareLog) {
        dailyCareLogDao.insertOrUpdateCareLog(careLog)
    }

    suspend fun saveProfile(profile: CatProfile) {
        catProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun saveWeightLog(date: Long, weight: Float) {
        catWeightLogDao.insertWeightLog(CatWeightLog(date = date, weight = weight))
    }

    suspend fun deleteWeightLog(id: Int) {
        catWeightLogDao.deleteWeightLogById(id)
    }

    suspend fun saveCheckInLog(
        date: Long,
        mood: String,
        notes: String,
        weight: Float? = null,
        photos: String = "",
        diaryEntryType: String = "general",
        reminderTimeMillis: Long? = null
    ) {
        catCheckInLogDao.insertCheckInLog(
            CatCheckInLog(
                date = date,
                mood = mood,
                healthStatus = notes,
                weight = weight,
                photos = photos,
                category = diaryEntryType,
                reminderTimeMillis = reminderTimeMillis
            )
        )
    }

    suspend fun deleteCheckInLog(id: Int) {
        catCheckInLogDao.deleteCheckInLogById(id)
    }

    suspend fun saveReminder(title: String, timeMillis: Long, type: String = "general") {
        reminderDao.insertReminder(Reminder(title = title, timeMillis = timeMillis, type = type))
    }

    suspend fun deleteReminder(id: Int) {
        reminderDao.deleteReminderById(id)
    }
}
