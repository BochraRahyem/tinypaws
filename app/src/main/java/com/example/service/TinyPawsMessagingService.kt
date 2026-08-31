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
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val pendingIntent = PendingIntent.getActivity(
            this, notifId, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Channel is created centrally by NotificationHelper (with sound/vibration).
        // Creating it again here with different settings would be ignored by the OS.
        val channelId = com.example.util.NotificationHelper.CHANNEL_DUPLICATE_REPORT
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_paw_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifId, notificationBuilder.build())
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Persist the rotated token so proximity notifications keep working.
        // - Signed-in: write straight to the user's Firestore profile.
        // - No session yet: stage it locally; the ViewModel pushes it on next sign-in.
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val current = auth.currentUser
            if (current != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(current.uid)
                    .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                    .addOnFailureListener { e ->
                        android.util.Log.e("TinyPawsMessaging", "Failed to persist refreshed FCM token", e)
                        stagePendingToken(token)
                    }
            } else {
                stagePendingToken(token)
            }
        } catch (e: Exception) {
            android.util.Log.e("TinyPawsMessaging", "Error handling new FCM token", e)
            stagePendingToken(token)
        }
    }

    private fun stagePendingToken(token: String) {
        runCatching {
            getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
                .edit().putString("pending_fcm_token", token).apply()
        }
    }
}
