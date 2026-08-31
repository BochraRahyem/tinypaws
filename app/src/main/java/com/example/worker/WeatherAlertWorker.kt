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

            // 2. Validation Check: Confirm location services are active.
            // Skip this cycle instead of endless retries: the periodic job will
            // simply run again at the next 2h tick, so retrying only burns
            // battery and pushes real executions further out via backoff.
            if (!isLocationServiceActive(appContext)) {
                android.util.Log.w("WeatherAlertWorker", "doWork: Location services disabled on device, skipping this cycle")
                return@withContext Result.success()
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
            try {
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

                val currentWeather = json.optJSONObject("current_weather")
                val currentTemp = currentWeather?.optDouble("temperature", if (maxTemp > -900 && minTemp < 900) (maxTemp + minTemp) / 2.0 else 22.0) ?: 22.0
                val weatherCode = currentWeather?.optInt("weathercode", 0) ?: 0
                val windSpeed = currentWeather?.optDouble("windspeed", 0.0) ?: 0.0
                val tempDelta = if (maxTemp > -900 && minTemp < 900) (maxTemp - minTemp) else 0.0

                android.util.Log.d("WeatherAlertWorker", "doWork: Successfully parsed forecast data. MaxTemp: $maxTemp, MinTemp: $minTemp, CurrentTemp: $currentTemp, Code: $weatherCode, Wind: $windSpeed")
                val prefs = appContext.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
                val isWeatherAlertsEnabled = prefs.getBoolean("extreme_weather_notifications", true)
                if (!isWeatherAlertsEnabled) {
                    android.util.Log.d("WeatherAlertWorker", "doWork: Weather alerts disabled by user in settings. Skipping notifications.")
                    return@withContext Result.success()
                }

                val savedLang = prefs.getString("user_lang", "en") ?: "en"
                val notificationHelper = NotificationHelper(appContext)

                val category = com.example.util.WeatherNotificationResolver.determineCategory(
                    currentTemp = currentTemp,
                    maxTemp = maxTemp,
                    minTemp = minTemp,
                    weatherCode = weatherCode,
                    windSpeed = windSpeed,
                    tempChangeDelta = tempDelta
                )

                val notificationData = com.example.util.WeatherNotificationResolver.getRotatingNotification(
                    context = appContext,
                    category = category,
                    cityName = cityName,
                    currentTemp = currentTemp,
                    maxTemp = maxTemp,
                    minTemp = minTemp,
                    languageCode = savedLang
                )

                // Only push a notification when there is a genuine weather
                // condition that needs the user's attention. Mild/"comfortable"
                // weather is not an alert event, so we must not send a generic
                // "lovely day" message on every background cycle.
                if (category == com.example.util.WeatherNotificationResolver.WeatherConditionCategory.COMFORTABLE) {
                    android.util.Log.d("WeatherAlertWorker", "doWork: Weather is comfortable/mild; no alert condition to notify. Skipping notification.")
                    return@withContext Result.success()
                }

                android.util.Log.d("WeatherAlertWorker", "doWork: Triggering rotating weather notification: category=$category, title='${notificationData.title}'")
                notificationHelper.triggerWeatherAlert(
                    title = notificationData.title,
                    message = notificationData.message,
                    notificationId = 3001
                )

                android.util.Log.d("WeatherAlertWorker", "doWork: Work execution finished successfully.")
                Result.success()
            } else {
                android.util.Log.w("WeatherAlertWorker", "doWork: API returned HTTP ${conn.responseCode}, scheduling retry")
                Result.retry()
            }
            } finally {
                conn.disconnect()
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
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWork
                )
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
                WorkManager.getInstance(context).enqueue(oneTimeWork)
                android.util.Log.i("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Successfully enqueued one-time weather check job.")
            } catch (e: Exception) {
                android.util.Log.e("WeatherAlertWorker", "triggerImmediateBackgroundCheck: Failed to enqueue one-time work due to exception", e)
            }
        }
    }
}
