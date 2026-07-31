package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
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

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    fun triggerExtremeWeatherNotification(areaName: String, bypassCooldownForTesting: Boolean = false): Boolean {
        val channelId = CHANNEL_EXTREME_WEATHER
        val notifId = 1002

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Check undismissed active notification
        if (!bypassCooldownForTesting && isNotificationActive(manager, notifId)) {
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
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alerts = listOf(
            "It is so hot! Please put some cold water for cats outside or build a shelter for them for the heat.",
            "It is so hot out today! Put some cold water for cats outside or build a shelter for them to beat the extreme heat.",
            "High Temperature Alert: It is so hot. Put some cold water for cats outside or build a shelter for them for the heat."
        )
        val alertMsg = alerts.random()
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentTitle("⚠️ Extreme Weather Alert")
            .setContentText("$areaName: $alertMsg")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Extreme Weather Alert at $areaName:\n\n$alertMsg\n\nPlease check on nearby street kitties and ensure their feeding spots are secure."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(getSoundUri())
            .setVibrate(VIBRATION_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            
        android.util.Log.d("NotificationHelper", "ExtremeWeather build check: context is null? ${context == null}, channelId = $channelId, pendingIntent is null? ${pendingIntent == null}")
        val builtNotification = builder.build()
        android.util.Log.d("NotificationHelper", "ExtremeWeather build successful. Posting via NotificationManager with ID $notifId...")
        try {
            manager.notify(notifId, builtNotification)
            android.util.Log.d("NotificationHelper", "ExtremeWeather notify call completed successfully for ID $notifId")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "ExtremeWeather notify call threw exception!", e)
        }
        
        // Save timestamp
        prefs.edit().putLong(KEY_LAST_WEATHER_ALERT, System.currentTimeMillis()).apply()
        return true
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
            
        android.util.Log.d("NotificationHelper", "WeatherAlert build check: context is null? ${context == null}, channelId = $channelId, pendingIntent is null? ${pendingIntent == null}")
        val builtNotification = builder.build()
        android.util.Log.d("NotificationHelper", "WeatherAlert build successful. Posting via NotificationManager with ID $notificationId...")
        try {
            manager.notify(notificationId, builtNotification)
            android.util.Log.d("NotificationHelper", "WeatherAlert notify call completed successfully for ID $notificationId")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "WeatherAlert notify call threw exception!", e)
        }

        // Save timestamp
        prefs.edit().putLong(KEY_LAST_WEATHER_ALERT, System.currentTimeMillis()).apply()
        return true
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

        android.util.Log.d("NotificationHelper", "DuplicateReport build check: context is null? ${context == null}, channelId = $channelId, pendingIntent is null? ${pendingIntent == null}")
        val builtNotification = builder.build()
        android.util.Log.d("NotificationHelper", "DuplicateReport build successful. Posting via NotificationManager with ID $notifId...")
        try {
            manager.notify(notifId, builtNotification)
            android.util.Log.d("NotificationHelper", "DuplicateReport notify call completed successfully for ID $notifId")
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "DuplicateReport notify call threw exception!", e)
        }

        prefs.edit().putLong("last_report_alert_$reportId", System.currentTimeMillis()).apply()
        return true
    }

    fun triggerFeedingStationNotification(
        stationName: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ): Boolean {
        val channelId = CHANNEL_FEEDING_STATIONS
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            .setContentTitle("🥣 Feeding Station Update: $stationName")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(getSoundUri())
            .setVibrate(VIBRATION_PATTERN)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        return try {
            manager.notify(notificationId, builder.build())
            true
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error notifying feeding station", e)
            false
        }
    }
}

