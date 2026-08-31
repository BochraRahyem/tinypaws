package com.example.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.util.NotificationHelper

/**
 * FREE-TIER PROXIMITY ALERTS (no Cloud Functions / no FCM push required).
 *
 * Periodically checks the newest stray-cat reports and posts a LOCAL
 * notification for reports within [RADIUS_KM] of the device's last known
 * location. Delivery relies on Android's own scheduling instead of server
 * push, so it costs nothing and needs no external accounts.
 *
 * Deduplication/cooldowns are handled by NotificationHelper's per-report
 * 24-hour cooldown, so repeated runs never spam the user about the same cat.
 */
class NearbyReportAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 0) Respect the user's master switch used for community notifications.
            val prefs = applicationContext.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("nearby_report_alerts", true)) {
                return@withContext Result.success()
            }

            // 1) Need a location to measure "near".
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext Result.success() // silently skip; nothing to compare against
            }
            val fused = LocationServices.getFusedLocationProviderClient(applicationContext)
            val location = fused.lastLocation.await()
            if (location == null) {
                return@withContext Result.success()
            }

            // 2) Newest handful of active reports (bounded query).
            val snapshot = FirebaseFirestore.getInstance()
                .collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .await()

            val helper = NotificationHelper(applicationContext)
            var alerted = 0

            for (doc in snapshot.documents) {
                val lat = doc.getDouble("latitude") ?: continue
                val lon = doc.getDouble("longitude") ?: continue
                if (lat == 0.0 && lon == 0.0) continue
                val rescued = doc.getBoolean("rescued") ?: false
                val status = doc.getString("status") ?: "active"
                if (rescued || status == "adopted" || status == "rescued") continue

                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    location.latitude, location.longitude, lat, lon, results
                )
                val distanceKm = results[0] / 1000f
                if (distanceKm > RADIUS_KM) continue

                val description = doc.getString("description").orEmpty().ifBlank {
                    applicationContext.getString(com.example.R.string.rescue_stories_stray_cat)
                }
                val title = applicationContext.getString(com.example.R.string.nearby_report_alert_title)
                val message = applicationContext.getString(
                    com.example.R.string.nearby_report_alert_body,
                    description, String.format(java.util.Locale.US, "%.1f", distanceKm)
                )

                // Per-report 24h cooldown lives inside this helper.
                if (helper.triggerDuplicateReportAlert(
                        reportId = doc.id,
                        title = title,
                        message = message
                    )
                ) {
                    alerted++
                    if (alerted >= MAX_ALERTS_PER_RUN) break
                }
            }
            android.util.Log.d("NearbyReports", "Proximity check done: $alerted alert(s) within ${RADIUS_KM}km")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NearbyReports", "Proximity check failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.success()
        }
    }

    companion object {
        private const val WORK_NAME = "NearbyReportPeriodicWork"
        private const val RADIUS_KM = 10f
        private const val MAX_ALERTS_PER_RUN = 3

        fun schedulePeriodicCheck(context: Context) {
            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                androidx.work.PeriodicWorkRequestBuilder<NearbyReportAlertWorker>(6, java.util.concurrent.TimeUnit.HOURS)
                    .setConstraints(
                        androidx.work.Constraints.Builder()
                            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                            .build()
                    )
                    .setBackoffCriteria(
                        androidx.work.BackoffPolicy.EXPONENTIAL,
                        androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                    .build()
            )
        }

        fun cancel(context: Context) {
            androidx.work.WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
