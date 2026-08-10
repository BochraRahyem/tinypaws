package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class TinyPawsMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "TinyPaws Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "New rescue notification received"
        
        logFcmNotification(title, body, remoteMessage.data.toString())
        
        sendNotification(title, body)
    }

    private fun logFcmNotification(title: String, body: String, dataString: String) {
        try {
            val prefs = getSharedPreferences("tinypaws_fcm_logs", Context.MODE_PRIVATE)
            val existingJson = prefs.getString("fcm_logs_json", "[]") ?: "[]"
            val array = org.json.JSONArray(existingJson)
            
            val newEntry = org.json.JSONObject().apply {
                put("title", title)
                put("body", body)
                put("data", dataString)
                put("timestamp", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
            }
            
            val newArray = org.json.JSONArray()
            newArray.put(newEntry)
            for (i in 0 until minOf(4, array.length())) {
                newArray.put(array.get(i))
            }
            
            prefs.edit().putString("fcm_logs_json", newArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "cat_care_reminders"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use foreground for now
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cat Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // You could send this token to your server if you had one
    }
}
