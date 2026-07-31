package com.example.data

import kotlinx.coroutines.flow.Flow

class CatRepository(
    private val catProfileDao: CatProfileDao,
    private val catWeightLogDao: CatWeightLogDao,
    private val catCheckInLogDao: CatCheckInLogDao,
    private val reminderDao: ReminderDao
) {
    val catProfile: Flow<CatProfile?> = catProfileDao.getCatProfile()
    val allWeightLogs: Flow<List<CatWeightLog>> = catWeightLogDao.getAllWeightLogs()
    val allCheckInLogs: Flow<List<CatCheckInLog>> = catCheckInLogDao.getAllCheckInLogs()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun saveProfile(profile: CatProfile) {
        catProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun saveWeightLog(date: Long, weight: Float) {
        catWeightLogDao.insertWeightLog(CatWeightLog(date = date, weight = weight))
    }

    suspend fun deleteWeightLog(id: Int) {
        catWeightLogDao.deleteWeightLogById(id)
    }

    suspend fun saveCheckInLog(date: Long, mood: String, healthStatus: String) {
        catCheckInLogDao.insertCheckInLog(CatCheckInLog(date = date, mood = mood, healthStatus = healthStatus))
    }

    suspend fun deleteCheckInLog(id: Int) {
        catCheckInLogDao.deleteCheckInLogById(id)
    }

    suspend fun saveReminder(title: String, timeMillis: Long) {
        reminderDao.insertReminder(Reminder(title = title, timeMillis = timeMillis))
    }

    suspend fun deleteReminder(id: Int) {
        reminderDao.deleteReminderById(id)
    }
}
