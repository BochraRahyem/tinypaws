package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE catId = :catId OR catIds LIKE '%' || :catId || '%' ORDER BY timeMillis ASC")
    fun getRemindersForCat(catId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY timeMillis ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): Reminder?

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersSync(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    @Query("DELETE FROM reminders WHERE catId = :catId")
    suspend fun deleteRemindersForCat(catId: Int)
}
