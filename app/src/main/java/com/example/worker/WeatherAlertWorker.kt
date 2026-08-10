package com.example.worker

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.*
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class WeatherAlertWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val lat = inputData.getDouble("LATITUDE", 36.8065)
        val lon = inputData.getDouble("LONGITUDE", 10.1815)
        val cityName = inputData.getString("CITY_NAME") ?: "Local Area"
        android.util.Log.d("WeatherAlertWorker", "doWork: Started executing WeatherAlertWorker for $cityName at ($lat, $lon). Attempt count: $runAttemptCount")

        try {
            // 1. Validation Check: Confirm internet/network is active
            if (!isNetworkAvailable(appContext)) {
                android.util.Log.w("WeatherAlertWorker", "doWork: Network unavailable, requesting WorkManager retry")
                return@withContext Result.retry()
            }

            // 2. Validation Check: Confirm location services are active
            if (!isLocationServiceActive(appContext)) {
                android.util.Log.w("WeatherAlertWorker", "doWork: Location services disabled on device, requesting retry")
                return@withContext Result.retry()
            }

            // Validate coordinates range
            if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
                android.util.Log.e("WeatherAlertWorker", "doWork: Invalid coordinates: ($lat, $lon). Terminating work as failure.")
                return@withContext Result.failure()
            }

            android.util.Log.d("WeatherAlertWorker", "doWork: Fetching forecast data from Open-Meteo API...")
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&daily=temperature_2m_max,temperature_2m_min,weathercode&current_weather=true&timezone=auto"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val stream = conn.inputStream
                val jsonText = stream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonText)

                val daily = json.getJSONObject("daily")
                val maxArray = daily.getJSONArray("temperature_2m_max")
                val minArray = daily.getJSONArray("temperature_2m_min")

                var maxTemp = -999.0
                var minTemp = 999.0

                for (i in 0 until minOf(7, maxArray.length())) {
                    val maxT = maxArray.getDouble(i)
                    val minT = minArray.getDouble(i)
                    if (maxT > maxTemp) maxTemp = maxT
                    if (minT < minTemp) minTemp = minT
                }

                android.util.Log.d("WeatherAlertWorker", "doWork: Successfully parsed forecast data. MaxTemp: $maxTemp, MinTemp: $minTemp")
                val notificationHelper = NotificationHelper(appContext)

                if (maxTemp > 35.0) {
                    val title = "🔥 Heatwave Alert (>35°C) in $cityName"
                    val message = "Background weather check: Temperatures reach ${maxTemp.toInt()}°C! Provide shade & fresh water for outdoor cats."
                    android.util.Log.d("WeatherAlertWorker", "doWork: High temperature detected ($maxTemp > 35.0). Triggering Heatwave Notification alert.")
                    notificationHelper.triggerWeatherAlert(title, message, 3001)
                } else {
                    android.util.Log.d("WeatherAlertWorker", "doWork: MaxTemp ($maxTemp) is within safe limits (<=35.0). No heatwave notification triggered.")
                }

                if (minTemp <= 15.0 || maxTemp <= 15.0) {
                    val lowest = minOf(maxTemp, minTemp)
                    val title = "❄️ Cold Weather Alert (≤15°C) in $cityName"
                    val message = "Background weather check: Temperatures drop to ${lowest.toInt()}°C! Keep cat shelters dry & insulated."
                    android.util.Log.d("WeatherAlertWorker", "doWork: Low temperature detected ($lowest <= 15.0). Triggering Cold Weather Notification alert.")
                    notificationHelper.triggerWeatherAlert(title, message, 3002)
                } else {
                    android.util.Log.d("WeatherAlertWorker", "doWork: Min/MaxTemp ($minTemp/$maxTemp) are within warm limits (>15.0). No cold alert notification triggered.")
                }

                // Daily Weather-based Cat Care Notification (Once per 24 hours)
                try {
                    val prefs = appContext.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
                    val lastDailySentTime = prefs.getLong("last_daily_weather_care_sent_time", 0L)
                    val now = System.currentTimeMillis()
                    val isCooldownOver = (now - lastDailySentTime) >= 24 * 60 * 60 * 1000L // 24 hours

                    // Force TranslationManager to load
                    com.example.ui.TranslationManager.load(appContext)
                    val savedLang = prefs.getString("user_lang", "en") ?: "en"

                    val titleKey: String
                    val msgKey: String

                    if (maxTemp > 28.0) {
                        titleKey = "weather_care_hot_title"
                        msgKey = "weather_care_hot_msg"
                    } else if (minTemp < 15.0) {
                        titleKey = "weather_care_cold_title"
                        msgKey = "weather_care_cold_msg"
                    } else {
                        titleKey = "weather_care_mild_title"
                        msgKey = "weather_care_mild_msg"
                    }

                    val title = com.example.ui.TranslationManager.getString(savedLang, "reminder", titleKey)
                    val message = com.example.ui.TranslationManager.getString(savedLang, "reminder", msgKey)

                    android.util.Log.d("WeatherAlertWorker", "doWork: Localized daily weather care recommendation: title='$title', msg='$message', lang='$savedLang'")
                    if (isCooldownOver) {
                        notificationHelper.triggerWeatherAlert(title, message, 3003, bypassCooldownForTesting = true)
                        prefs.edit().putLong("last_daily_weather_care_sent_time", now).apply()
                        android.util.Log.d("WeatherAlertWorker", "doWork: Localized daily notification triggered successfully.")
                    } else {
                        android.util.Log.d("WeatherAlertWorker", "doWork: Localized daily notification skipped: 24h cooldown active.")
                    }
                } catch (ex: Exception) {
                    android.util.Log.e("WeatherAlertWorker", "doWork: Failed to trigger daily weather notification", ex)
                }

                android.util.Log.d("WeatherAlertWorker", "doWork: Work execution finished successfully.")
                Result.success()
            } else {
                android.util.Log.w("WeatherAlertWorker", "doWork: API returned HTTP ${conn.responseCode}, scheduling retry")
                Result.retry()
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherAlertWorker", "doWork: WorkManager task execution failed due to exception: ${e.message}", e)
            if (runAttemptCount < 3) {
                android.util.Log.d("WeatherAlertWorker", "doWork: Retry count ($runAttemptCount) is less than 3. Scheduling retry.")
                Result.retry()
            } else {
                android.util.Log.e("WeatherAlertWorker", "doWork: Max retry count reached. Returning failure.")
                Result.failure()
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isLocationServiceActive(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val isGpsEnabled = try { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
        val isNetEnabled = try { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }
        return isGpsEnabled || isNetEnabled
    }

    companion object {
        private const val WORK_NAME = "WeatherAlertPeriodicWork"

        fun schedulePeriodicWeatherCheck(context: Context, lat: Double = 36.8065, lon: Double = 10.1815, cityName: String = "Local Area") {
            android.util.Log.d("WeatherAlertWorker", "schedulePeriodicWeatherCheck: Preparing periodic work. City: $cityName, coordinates: ($lat, $lon)")
            
            val inputData = Data.Builder()
                .putDouble("LATITUDE", lat)
                .putDouble("LONGITUDE", lon)
                .putString("CITY_NAME", cityName)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWork = PeriodicWorkRequestBuilder<WeatherAlertWorker>(2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(inputData)
                // Linear / Exponential retry backoff policy for intermittent connectivity or location
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            android.util.Log.d("WeatherAlertWorker", "schedulePeriodicWeatherCheck: Enqueuing unique periodic work '$WORK_NAME' with UPDATE policy.")
            try {
                val operation = WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWork
                )
                operation.state.observeForever { state ->
                    android.util.Log.d("WeatherAlertWorker", "schedulePeriodicWeatherCheck: Operation state changed to $state")
                }
                android.util.Log.i("WeatherAlertWorker", "schedulePeriodicWeatherCheck: Successfully requested periodic weather check job scheduling.")
            } catch (e: Exception) {
                android.util.Log.e("WeatherAlertWorker", "schedulePeriodicWeatherCheck: Failed to schedule periodic check job due to exception", e)
            }
        }

        fun triggerImmediateBackgroundCheck(context: Context, lat: Double = 36.8065, lon: Double = 10.1815, cityName: String = "Local Area") {
            android.util.Log.d("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Preparing one-time diagnostic work. City: $cityName, coordinates: ($lat, $lon)")
            
            val inputData = Data.Builder()
                .putDouble("LATITUDE", lat)
                .putDouble("LONGITUDE", lon)
                .putString("CITY_NAME", cityName)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWork = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            android.util.Log.d("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Enqueuing one-time WorkRequest ID: ${oneTimeWork.id}")
            try {
                val operation = WorkManager.getInstance(context).enqueue(oneTimeWork)
                operation.state.observeForever { state ->
                    android.util.Log.d("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Operation state changed to $state")
                }
                android.util.Log.i("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Successfully enqueued one-time weather check job.")
            } catch (e: Exception) {
                android.util.Log.e("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Failed to enqueue one-time work due to exception", e)
            }
        }
    }
}
