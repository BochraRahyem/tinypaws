# ── Room ──────────────────────────────────────────────────────────────
# Keep Room entities (schema + DAO access)
-keep class com.example.data.CatProfile { *; }
-keep class com.example.data.LogEntry { *; }
-keep class com.example.data.CatCheckInLog { *; }
-keep class com.example.data.CatWeightLog { *; }
-keep class com.example.data.Reminder { *; }
-keep class com.example.data.DailyCareLog { *; }
-keep class com.example.data.CatHistoryEntry { *; }
-keep class com.example.data.FavoriteDiy { *; }
-keep class com.example.data.StrayReport { *; }

# Keep Room DAO interfaces and their generated _Impl classes
-keep class com.example.data.*Dao { *; }
-keep class com.example.data.*_Impl { *; }

# Keep AppDatabase and its generated _Impl (Room loads via reflection)
-keep class com.example.data.AppDatabase { *; }
-keep class com.example.data.AppDatabase_Impl { *; }
-keep class com.example.data.AppDatabase$Companion { *; }

# Keep all Room _Impl companions (accessed via reflection)
-keep class com.example.data.*_Impl$Companion { *; }

# ── Firestore data classes ────────────────────────────────────────────
-keep class com.example.data.UserProfile { *; }
-keep class com.example.data.CatReport { *; }
-keep class com.example.data.UserAction { *; }
-keep class com.example.data.FeedingStation { *; }
-keep class com.example.data.GlobalStats { *; }
-keep class com.example.data.GlobalStatistics { *; }
-keep class com.example.data.DiaryEntry { *; }
-keep class com.example.data.NearbyPlace { *; }

# Keep all data classes and their companions (safety net)
-keep class com.example.data.** { *; }
-keep class com.example.data.**$Companion { *; }

# ── Android resource classes ─────────────────────────────────────────
# Keep R$ inner classes — resource shrinking can strip them
-keep class com.example.R$* { *; }
-keep class com.example.R { *; }

# ── Compose ───────────────────────────────────────────────────────────
-dontwarn androidx.compose.**
# Keep Compose stability markers (R8 removes them as "unused" but Compose runtime needs them)
-keepclassmembers class ** {
    static final int $stable;
}
# Keep Compose lambda singletons
-keep class **$ComposableSingletons$* { *; }

# ── Kotlin ────────────────────────────────────────────────────────────
-keep class **$Companion { *; }
-keepnames class **$Companion { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── BuildConfig ───────────────────────────────────────────────────────
-keep class com.example.BuildConfig { *; }

# ── WorkManager workers ──────────────────────────────────────────────
-keep class com.example.worker.WeatherAlertWorker { *; }
-keep class com.example.worker.NearbyReportAlertWorker { *; }

# ── Firebase messaging ───────────────────────────────────────────────
-keep class com.example.service.TinyPawsMessagingService { *; }

# ── Broadcast receivers ──────────────────────────────────────────────
-keep class com.example.util.ReminderReceiver { *; }
-keep class com.example.util.BootReceiver { *; }

# ── TranslationManager ───────────────────────────────────────────────
-keep class com.example.ui.TranslationManager { *; }

# ── UI classes — safety net against Compose-related stripping ─────────
-keep class com.example.TinyPawsApplication { *; }
-keep class com.example.MainActivity { *; }
-keep class com.example.MainActivityKt { *; }
