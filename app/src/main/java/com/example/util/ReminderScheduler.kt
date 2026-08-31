package com.example.util

import android.content.Context
import com.example.data.AppDatabase
import java.util.Calendar

/**
 * Re-registers AlarmManager alarms for all stored reminders.
 * Used by BootReceiver after a device restart so care reminders survive reboot.
 * Safe to call repeatedly: alarms are keyed by reminder id and use
 * FLAG_UPDATE_CURRENT / setExactAndAllowWhileIdle, which replace (not duplicate)
 * any previously registered alarm for the same id.
 */
object ReminderScheduler {

    suspend fun rescheduleAll(context: Context) {
        val db = AppDatabase.getDatabase(context.applicationContext)
        val helper = NotificationHelper(context.applicationContext)
        val reminders = db.reminderDao().getAllRemindersSync()
        val now = System.currentTimeMillis()

        for (reminder in reminders) {
            try {
                if (!reminder.isEnabled) continue
                val cal = Calendar.getInstance().apply { timeInMillis = reminder.timeMillis }

                if (reminder.isRecurring) {
                    // Recurring daily reminders: roll forward to the next future
                    // occurrence of the stored time-of-day.
                    while (cal.timeInMillis <= now) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    helper.scheduleNotification(reminder.id, reminder.title, cal.timeInMillis)
                } else if (reminder.timeMillis > now) {
                    helper.scheduleNotification(reminder.id, reminder.title, reminder.timeMillis)
                }
                // Past one-shot reminders are intentionally dropped.
            } catch (e: Exception) {
                android.util.Log.e("ReminderScheduler", "Failed to reschedule reminder ${reminder.id}", e)
            }
        }
    }
}
