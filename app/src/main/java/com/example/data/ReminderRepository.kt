package com.example.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun saveReminder(title: String, timeMillis: Long) {
        reminderDao.insertReminder(Reminder(title = title, timeMillis = timeMillis))
    }

    suspend fun deleteReminder(id: Int) {
        reminderDao.deleteReminderById(id)
    }
}
