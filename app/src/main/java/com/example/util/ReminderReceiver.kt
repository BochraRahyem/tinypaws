package com.example.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        
        // Immediate check: cancel and drop legacy static automatic reminders (8001/8002)
        if (reminderId == 8001 || reminderId == 8002) {
            NotificationHelper(context).cancelNotification(reminderId)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminder = db.reminderDao().getReminderById(reminderId)
                
                // If reminder was deleted or disabled, do not post notification and cancel alarm
                if (reminder == null || !reminder.isEnabled) {
                    android.util.Log.d("ReminderReceiver", "Reminder $reminderId is disabled or not found. Canceling alarm.")
                    NotificationHelper(context).cancelNotification(reminderId)
                    return@launch
                }

                // Verify at least one of the reminder's linked cats still exists.
                // A reminder tied only to deleted cats must stop firing.
                if (reminder.catIds.isNotBlank() && reminder.catIds != "all") {
                    val linkedIds = reminder.catIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                    val existingIds = db.catProfileDao().getAllCatProfilesSync().map { it.id }.toSet()
                    val anyLinkedCatExists = linkedIds.any { it in existingIds }
                    if (!anyLinkedCatExists) {
                        android.util.Log.d("ReminderReceiver", "All cats linked to reminder ${reminder.id} were deleted. Canceling and disabling it.")
                        NotificationHelper(context).cancelNotification(reminderId)
                        db.reminderDao().updateReminder(reminder.copy(isEnabled = false))
                        return@launch
                    }
                } else if (db.catProfileDao().getCatCount() == 0) {
                    android.util.Log.d("ReminderReceiver", "No cats exist for reminder ${reminder.id}. Canceling alarm.")
                    NotificationHelper(context).cancelNotification(reminderId)
                    return@launch
                }

                val title = intent.getStringExtra("reminder_title") ?: reminder.title

                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.meow_chime}")

                val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_paw_notification)
                    .setContentTitle("TinyPaws Care Reminder 🐾")
                    .setContentText(title)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(title))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(soundUri)
                    .setVibrate(NotificationHelper.VIBRATION_PATTERN)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                try {
                    with(NotificationManagerCompat.from(context)) {
                        notify(reminderId, builder.build())
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("ReminderReceiver", "Permission missing for notification post", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderReceiver", "Error processing reminder in ReminderReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

