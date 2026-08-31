package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction

class CatRepository(
    private val catProfileDao: CatProfileDao,
    private val catWeightLogDao: CatWeightLogDao,
    private val catCheckInLogDao: CatCheckInLogDao,
    private val reminderDao: ReminderDao,
    private val dailyCareLogDao: DailyCareLogDao,
    private val catHistoryEntryDao: CatHistoryEntryDao,
    /** Database handle used only for wrapping multi-step writes in a transaction. */
    private val database: androidx.room.RoomDatabase? = null
) {
    val allCatProfiles: Flow<List<CatProfile>> = catProfileDao.getAllCatProfiles()
    val catProfile: Flow<CatProfile?> = catProfileDao.getCatProfile()
    val allWeightLogs: Flow<List<CatWeightLog>> = catWeightLogDao.getAllWeightLogs()
    val allCheckInLogs: Flow<List<CatCheckInLog>> = catCheckInLogDao.getAllCheckInLogs()
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    val allCareLogs: Flow<List<DailyCareLog>> = dailyCareLogDao.getAllCareLogs()
    val allHistoryEntries: Flow<List<CatHistoryEntry>> = catHistoryEntryDao.getAllHistoryEntries()

    fun getCatProfileById(catId: Int): Flow<CatProfile?> = catProfileDao.getCatProfileById(catId)
    suspend fun getCatProfileByIdSync(catId: Int): CatProfile? = catProfileDao.getCatProfileByIdSync(catId)
    fun getWeightLogsForCat(catId: Int): Flow<List<CatWeightLog>> = catWeightLogDao.getWeightLogsForCat(catId)
    fun getCheckInLogsForCat(catId: Int): Flow<List<CatCheckInLog>> = catCheckInLogDao.getCheckInLogsForCat(catId)

    /**
     * Reminders linked to one cat: direct catId OR exact comma-list membership.
     * Exact parsing (split on ',') prevents cat 1 matching a shared reminder for cats 11/21.
     */
    fun getRemindersForCat(catId: Int): Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { all ->
            all.filter { reminder ->
                reminder.catId == catId ||
                    reminder.catIds.split(',')
                        .mapNotNull { it.trim().toIntOrNull() }
                        .contains(catId)
            }
        }
    fun getCareLogsForCat(catId: Int): Flow<List<DailyCareLog>> = dailyCareLogDao.getCareLogsForCat(catId)
    fun getHistoryEntriesForCat(catId: Int): Flow<List<CatHistoryEntry>> = catHistoryEntryDao.getHistoryEntriesForCat(catId)

    fun getCareLogForDate(dateStr: String): Flow<DailyCareLog?> = dailyCareLogDao.getCareLogForDate(dateStr)
    fun getCareLogForCatAndDate(catId: Int, dateStr: String): Flow<DailyCareLog?> = dailyCareLogDao.getCareLogForCatAndDate(catId, dateStr)

    suspend fun saveCareLog(careLog: DailyCareLog) {
        dailyCareLogDao.insertOrUpdateCareLog(careLog)
    }

    suspend fun saveProfile(profile: CatProfile): Long {
        return catProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun deleteCatProfile(catId: Int) {
        val cascade: suspend () -> Unit = {
            catProfileDao.deleteCatProfileById(catId)
            catWeightLogDao.deleteLogsForCat(catId)
            catCheckInLogDao.deleteLogsForCat(catId)
            dailyCareLogDao.deleteLogsForCat(catId)
            catHistoryEntryDao.deleteEntriesForCat(catId)

            // Safely update or delete shared reminders
            val allReminders = reminderDao.getAllRemindersSync()
            for (reminder in allReminders) {
                if (reminder.catIds == "all") {
                    // "all" automatically adjusts to remaining cats, no action needed
                    continue
                }
                if (reminder.catIds.isNotEmpty()) {
                    val ids = reminder.catIds.split(",").mapNotNull { it.trim().toIntOrNull() }.toMutableList()
                    if (ids.contains(catId)) {
                        ids.remove(catId)
                        if (ids.isEmpty()) {
                            reminderDao.deleteReminderById(reminder.id)
                        } else {
                            reminderDao.updateReminder(reminder.copy(catIds = ids.joinToString(",")))
                        }
                    }
                } else if (reminder.catId == catId) {
                    reminderDao.deleteReminderById(reminder.id)
                }
            }
        }

        // All-or-nothing: process death mid-cascade can no longer leave orphaned rows.
        val db = database
        if (db != null) {
            db.withTransaction { cascade() }
        } else {
            cascade()
        }
    }

    suspend fun saveWeightLog(date: Long, weight: Float, id: Int = 0, catId: Int = 1) {
        catWeightLogDao.insertWeightLog(CatWeightLog(id = id, catId = catId, date = date, weight = weight))
    }

    /** Upsert preserving the original row id - used by backup restore to avoid duplicates. */
    suspend fun restoreWeightLog(log: CatWeightLog) {
        catWeightLogDao.insertWeightLog(log)
    }

    suspend fun deleteWeightLog(id: Int) {
        catWeightLogDao.deleteWeightLogById(id)
    }

    suspend fun saveHistoryEntry(entry: CatHistoryEntry) {
        catHistoryEntryDao.insertHistoryEntry(entry)
    }

    suspend fun deleteHistoryEntry(id: Int) {
        catHistoryEntryDao.deleteHistoryEntryById(id)
    }

    suspend fun saveCheckInLog(
        date: Long,
        mood: String,
        notes: String,
        weight: Float? = null,
        photos: String = "",
        diaryEntryType: String = "general",
        reminderTimeMillis: Long? = null,
        catId: Int = 1
    ) {
        catCheckInLogDao.insertCheckInLog(
            CatCheckInLog(
                catId = catId,
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

    /** Upsert preserving the original row id - used by backup restore to avoid duplicates. */
    suspend fun restoreCheckInLog(log: CatCheckInLog) {
        catCheckInLogDao.insertCheckInLog(log)
    }

    /** Upsert preserving the original row id - used by backup restore to avoid duplicates. */
    suspend fun restoreHistoryEntry(entry: CatHistoryEntry) {
        catHistoryEntryDao.insertHistoryEntry(entry)
    }

    suspend fun saveReminder(title: String, timeMillis: Long, type: String = "general", catId: Int = 1, catIds: String = ""): Long {
        return reminderDao.insertReminder(Reminder(catId = catId, catIds = catIds, title = title, timeMillis = timeMillis, type = type))
    }

    suspend fun saveReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(id: Int) {
        reminderDao.deleteReminderById(id)
    }
}
