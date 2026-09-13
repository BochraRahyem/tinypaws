package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "tinypaws_reminder_v3"
        const val CHANNEL_NAME = "TinyPaws Reminders 🐾"
        
        const val CHANNEL_EXTREME_WEATHER = "tinypaws_extreme_weather_v3"
        const val CHANNEL_WEATHER_ALERT = "tinypaws_weather_alert_v3"
        const val CHANNEL_DUPLICATE_REPORT = "tinypaws_community_reports_v3"
        const val CHANNEL_FEEDING_STATIONS = "tinypaws_feeding_stations_v3"

        // Cooldown constants
        const val WEATHER_ALERT_COOLDOWN_MS = 4 * 60 * 60 * 1000L // 4 hours
        const val REPORT_ALERT_COOLDOWN_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val PREFS_NAME = "notification_cooldown_prefs"
        private const val KEY_LAST_WEATHER_ALERT = "last_weather_alert_time"

        val VIBRATION_PATTERN = longArrayOf(0, 250, 250, 250)
        private const val TAG = "NotificationHelper"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSoundUri(): Uri {
        return Uri.parse("android.resource://${context.packageName}/${R.raw.meow_chime}")
    }

    private fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = getSoundUri()
            val audioAttributes = getAudioAttributes()
            val manager = context.getSystemService(NotificationManager::class.java) ?: return

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for feeding and caring for cats"
                },
                NotificationChannel(
                    CHANNEL_EXTREME_WEATHER,
                    "TinyPaws Extreme Weather Alerts ⚠️",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for extreme weather conditions in marked cat feeding areas."
                },
                NotificationChannel(
                    CHANNEL_WEATHER_ALERT,
                    "TinyPaws Weather & Heatwave Alerts 🌤️",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for extreme high and low weather warnings."
                },
                NotificationChannel(
                    CHANNEL_DUPLICATE_REPORT,
                    "TinyPaws Community Cat Reports 🐱",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Updates when nearby users report duplicate or updated stray cat sightings."
                },
                NotificationChannel(
                    CHANNEL_FEEDING_STATIONS,
                    "TinyPaws Feeding Station Alerts 🥣",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications and status updates for community feeding stations."
                }
            )

            for (ch in channels) {
                ch.setSound(soundUri, audioAttributes)
                ch.enableVibration(true)
                ch.vibrationPattern = VIBRATION_PATTERN
                ch.enableLights(true)
                ch.lightColor = android.graphics.Color.parseColor("#FF69B4")
                manager.createNotificationChannel(ch)
            }
        }
    }

    /**
     * Returns true if POST_NOTIFICATIONS permission is granted (or device < Android 13).
     */
    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * Posts a notification only if permission is granted. Returns false if blocked.
     */
    private fun safeNotify(manager: NotificationManager, id: Int, notification: android.app.Notification): Boolean {
        if (!hasNotificationPermission()) {
            android.util.Log.w(TAG, "POST_NOTIFICATIONS permission not granted — skipping notification $id")
            return false
        }
        try {
            manager.notify(id, notification)
            return true
        } catch (e: SecurityException) {
            android.util.Log.e(TAG, "SecurityException posting notification $id", e)
            return false
        }
    }
    fun cancelOrphanedDailyReminders() {
        cancelNotification(8001)
        cancelNotification(8002)
    }

    fun scheduleNotification(reminderId: Int, title: String, timeMillis: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            android.util.Log.w("NotificationHelper", "Exact alarm permission missing, falling back to inexact alarm", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        }
    }

    fun scheduleDailyRecurringNotification(reminderId: Int, title: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelNotification(reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Check if 4 hours have elapsed since the last weather alert.
     */
    fun canSendWeatherAlert(): Boolean {
        val lastTime = prefs.getLong(KEY_LAST_WEATHER_ALERT, 0L)
        val now = System.currentTimeMillis()
        return (now - lastTime) >= WEATHER_ALERT_COOLDOWN_MS
    }

    fun getRemainingWeatherCooldownMs(): Long {
        val lastTime = prefs.getLong(KEY_LAST_WEATHER_ALERT, 0L)
        val elapsed = System.currentTimeMillis() - lastTime
        return if (elapsed < WEATHER_ALERT_COOLDOWN_MS) WEATHER_ALERT_COOLDOWN_MS - elapsed else 0L
    }

    /**
     * Check if 24 hours have elapsed since the last duplicate report notification for this report.
     */
    fun canSendReportAlert(reportId: String): Boolean {
        val lastTime = prefs.getLong("last_report_alert_$reportId", 0L)
        val now = System.currentTimeMillis()
        return (now - lastTime) >= REPORT_ALERT_COOLDOWN_MS
    }

    private fun isNotificationActive(manager: NotificationManager, notificationId: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNotifs = manager.activeNotifications
            return activeNotifs.any { it.id == notificationId }
        }
        return false
    }

    fun triggerWeatherAlert(
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt(),
        bypassCooldownForTesting: Boolean = false
    ): Boolean {
        val channelId = CHANNEL_WEATHER_ALERT
        
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Check undismissed active notification
        if (!bypassCooldownForTesting && isNotificationActive(manager, notificationId)) {
            android.util.Log.d("NotificationHelper", "Weather alert skipped: Active undismissed alert present")
            return false
        }

        // 2. Check 4-hour cooldown
        if (!bypassCooldownForTesting && !canSendWeatherAlert()) {
            android.util.Log.d("NotificationHelper", "Weather alert suppressed: 4-hour cooldown active")
            return false
        }

        createNotificationChannels()
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(getSoundUri())
            .setVibrate(VIBRATION_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            
        android.util.Log.d("NotificationHelper", "WeatherAlert build check: channelId = $channelId")
        val builtNotification = builder.build()
        android.util.Log.d("NotificationHelper", "WeatherAlert build successful. Posting via NotificationManager with ID $notificationId...")
        val posted = safeNotify(manager, notificationId, builtNotification)

        // Only save cooldown if notification was actually posted
        if (posted) {
            prefs.edit().putLong(KEY_LAST_WEATHER_ALERT, System.currentTimeMillis()).apply()
        }
        return posted
    }

    /**
     * Trigger duplicate stray cat report alert with 24-hour cooldown per report ID.
     */
    fun triggerDuplicateReportAlert(
        reportId: String,
        title: String,
        message: String,
        bypassCooldownForTesting: Boolean = false
    ): Boolean {
        val channelId = CHANNEL_DUPLICATE_REPORT
        val notifId = (reportId.hashCode() and 0x7FFFFFFF)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Active notification check
        if (isNotificationActive(manager, notifId)) {
            android.util.Log.d("NotificationHelper", "Report alert skipped: Notification already active/undismissed")
            return false
        }

        // 2. 24-hour report cooldown check
        if (!bypassCooldownForTesting && !canSendReportAlert(reportId)) {
            android.util.Log.d("NotificationHelper", "Report alert suppressed: 24-hour cooldown active for report $reportId")
            return false
        }

        createNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(getSoundUri())
            .setVibrate(VIBRATION_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        android.util.Log.d("NotificationHelper", "DuplicateReport build check: channelId = $channelId")
        val builtNotification = builder.build()
        android.util.Log.d("NotificationHelper", "DuplicateReport build successful. Posting via NotificationManager with ID $notifId...")
        safeNotify(manager, notifId, builtNotification)

        prefs.edit().putLong("last_report_alert_$reportId", System.currentTimeMillis()).apply()
        pruneReportAlertTimestamps()
        return true
    }

    /**
     * Keeps the cooldown SharedPreferences file bounded by evicting the oldest
     * per-report timestamps once the map grows past 100 entries.
     */
    private fun pruneReportAlertTimestamps() {
        try {
            val entries = prefs.all.entries
                .filter { it.key.startsWith("last_report_alert_") }
                .mapNotNull { (k, v) -> (v as? Long)?.let { k to it } }
            if (entries.size <= 100) return
            val editor = prefs.edit()
            entries.sortedBy { it.second }
                .take(entries.size - 80)
                .forEach { (key, _) -> editor.remove(key) }
            editor.apply()
        } catch (e: Exception) {
            android.util.Log.w("NotificationHelper", "Failed pruning report alert timestamps", e)
        }
    }

}

