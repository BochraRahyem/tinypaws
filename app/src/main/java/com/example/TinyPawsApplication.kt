package com.example

import android.app.Application
import com.example.ui.TranslationManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings

class TinyPawsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Configure Firestore for offline persistence (capped so the cache
        // cannot grow without bound on disk; LRU eviction handles the rest).
        // Guarded: on devices where Firebase is unavailable (no Play services,
        // or google-services.json missing), getInstance() throws - the app must
        // still start and serve local-only features.
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(100L * 1024 * 1024) // 100 MB
                        .build()
                )
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            android.util.Log.w("TinyPawsApp", "Firestore offline cache not configured: ${e.message}")
        }

        TranslationManager.load(this)
        com.example.util.NotificationHelper(this).cancelOrphanedDailyReminders()
    }
}
