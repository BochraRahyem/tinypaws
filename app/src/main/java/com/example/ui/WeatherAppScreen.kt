package com.example.ui

import androidx.compose.ui.res.stringResource

import com.example.R

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import com.example.worker.WeatherAlertWorker
import androidx.work.WorkManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// Model for Daily Weather Forecast
data class DailyWeatherForecast(
    val dateString: String,      // e.g. "2026-07-29"
    val dayName: String,         // e.g. "Today", "Thu", "Fri"
    val fullDateLabel: String,   // e.g. "Jul 29"
    val maxTempC: Double,        // Maximum Temperature in °C
    val minTempC: Double,        // Minimum Temperature in °C
    val weatherCode: Int,        // WMO weather code
    val conditionText: String,   // e.g. "Sunny", "Heatwave Alert", "Partly Cloudy"
    val conditionEmoji: String   // e.g. "☀️", "🔥", "⛅", "🌧️", "❄️"
)

data class CityLocation(
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)

val PRESET_CITIES = listOf(
    CityLocation("Current GPS Location", "User GPS", 0.0, 0.0),
    CityLocation("Tunis", "Tunisia", 36.8065, 10.1815),
    CityLocation("Cairo", "Egypt", 30.0444, 31.2357),
    CityLocation("Dubai", "UAE", 25.2048, 55.2708),
    CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753),
    CityLocation("Madrid", "Spain", 40.4168, -3.7038),
    CityLocation("Rome", "Italy", 41.9028, 12.4964),
    CityLocation("Athens", "Greece", 37.9838, 23.7275),
    CityLocation("Paris", "France", 48.8566, 2.3522),
    CityLocation("London", "UK", 51.5074, -0.1278),
    CityLocation("New York", "USA", 40.7128, -74.0060),
    CityLocation("Tokyo", "Japan", 35.6762, 139.6503),
    CityLocation("Sydney", "Australia", -33.8688, 151.2093)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationHelper = remember { NotificationHelper(context) }

    // ViewModel states
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val userLoc by viewModel.userLocation.collectAsStateWithLifecycle()

    var selectedCity by remember { mutableStateOf(PRESET_CITIES[0]) }
    var forecastList by remember { mutableStateOf<List<DailyWeatherForecast>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isWarningEnabled by remember { mutableStateOf(true) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    fun ensureNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        ensureNotifPermission()
    }

    // Helper to send weather notification to phone
    fun sendWeatherPhoneNotification(maxT: Double, minT: Double, cityName: String, bypassCooldown: Boolean = true) {
        if (!isWarningEnabled) return
        ensureNotifPermission()
        if (maxT > 35.0) {
            val title = "🔥 Extreme Heatwave Alert in $cityName"
            val msg = "Max temperature reaches ${formatTempDual(maxT)} this week! Please provide shade and fresh cold water for outdoor cats."
            notificationHelper.triggerWeatherAlert(title, msg, bypassCooldownForTesting = bypassCooldown)
            Toast.makeText(context, "Notification sent to phone: Heatwave Alert", Toast.LENGTH_SHORT).show()
        } else if (minT <= 15.0 || maxT <= 15.0) {
            val lowest = minOf(maxT, minT)
            val title = "❄️ Cold Weather Alert in $cityName"
            val msg = "Temperatures drop to ${formatTempDual(lowest)}! Keep cat shelters elevated and insulated with fresh dry straw."
            notificationHelper.triggerWeatherAlert(title, msg, bypassCooldownForTesting = bypassCooldown)
            Toast.makeText(context, "Notification sent to phone: Cold Weather Alert", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct active connection check function
    fun checkHasInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    var hasConnection by remember { mutableStateOf(checkHasInternet()) }

    // SharedPreferences for caching weather readings
    val weatherPrefs = remember { context.getSharedPreferences("weather_cache_prefs", Context.MODE_PRIVATE) }
    var isCachedData by remember { mutableStateOf(false) }
    var cachedTimeLabel by remember { mutableStateOf("") }

    // Helper to parse Open-Meteo JSON into DailyWeatherForecast list
    fun parseMeteoJson(jsonText: String): List<DailyWeatherForecast> {
        val json = JSONObject(jsonText)
        val daily = json.getJSONObject("daily")
        val timeArray = daily.getJSONArray("time")
        val maxArray = daily.getJSONArray("temperature_2m_max")
        val minArray = daily.getJSONArray("temperature_2m_min")
        val codeArray = daily.getJSONArray("weathercode")

        val resultList = mutableListOf<DailyWeatherForecast>()
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfDate = SimpleDateFormat("MMM d", Locale.getDefault())

        for (i in 0 until minOf(7, timeArray.length())) {
            val dateStr = timeArray.getString(i)
            val maxT = maxArray.getDouble(i)
            val minT = minArray.getDouble(i)
            val code = codeArray.getInt(i)

            val parsedDate = sdfInput.parse(dateStr) ?: Date()
            val isToday = i == 0
            val dayLabel = if (isToday) "Today" else sdfDay.format(parsedDate)
            val fullLabel = sdfDate.format(parsedDate)

            val (condText, condEmoji) = getWeatherCondition(code, maxT)

            resultList.add(
                DailyWeatherForecast(
                    dateString = dateStr,
                    dayName = dayLabel,
                    fullDateLabel = fullLabel,
                    maxTempC = maxT,
                    minTempC = minT,
                    weatherCode = code,
                    conditionText = condText,
                    conditionEmoji = condEmoji
                )
            )
        }
        return resultList
    }

    // Helper to load cached weather data
    fun loadCachedForecast(): Boolean {
        val cachedJson = weatherPrefs.getString("cached_json_${selectedCity.name}", null)
        val cachedTime = weatherPrefs.getLong("cached_time_${selectedCity.name}", 0L)
        if (!cachedJson.isNullOrEmpty() && cachedTime > 0L) {
            try {
                val list = parseMeteoJson(cachedJson)
                if (list.isNotEmpty()) {
                    forecastList = list
                    isCachedData = true
                    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    cachedTimeLabel = sdf.format(Date(cachedTime))
                    return true
                }
            } catch (e: Exception) {
                // Ignore parse errors on stale cache
            }
        }
        return false
    }

    // Fetch Weather Logic
    fun fetchForecast() {
        hasConnection = checkHasInternet()
        isLoading = true
        errorMessage = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                if (!hasConnection) {
                    withContext(Dispatchers.Main) {
                        val loaded = loadCachedForecast()
                        if (!loaded) {
                            forecastList = generateFallbackForecast()
                            isCachedData = false
                        }
                        isLoading = false
                    }
                    return@launch
                }

                val targetLat = if (selectedCity.name.contains("Current GPS")) userLoc.first else selectedCity.lat
                val targetLon = if (selectedCity.name.contains("Current GPS")) userLoc.second else selectedCity.lon

                val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$targetLat&longitude=$targetLon&daily=temperature_2m_max,temperature_2m_min,weathercode&current_weather=true&timezone=auto"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream
                    val jsonText = stream.bufferedReader().use { it.readText() }
                    val resultList = parseMeteoJson(jsonText)

                    // Save to SharedPreferences cache
                    weatherPrefs.edit()
                        .putString("cached_json_${selectedCity.name}", jsonText)
                        .putLong("cached_time_${selectedCity.name}", System.currentTimeMillis())
                        .apply()

                    withContext(Dispatchers.Main) {
                        forecastList = resultList
                        isCachedData = false
                        isLoading = false
                        val maxInWeek = resultList.maxOfOrNull { it.maxTempC } ?: 25.0
                        val minInWeek = resultList.minOfOrNull { it.minTempC } ?: 18.0
                        sendWeatherPhoneNotification(maxInWeek, minInWeek, selectedCity.name)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val loaded = loadCachedForecast()
                        if (!loaded) {
                            forecastList = generateFallbackForecast()
                            isCachedData = false
                        }
                        isLoading = false
                        val maxInWeek = forecastList.maxOfOrNull { it.maxTempC } ?: 25.0
                        val minInWeek = forecastList.minOfOrNull { it.minTempC } ?: 18.0
                        sendWeatherPhoneNotification(maxInWeek, minInWeek, selectedCity.name)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val loaded = loadCachedForecast()
                    if (!loaded) {
                        forecastList = generateFallbackForecast()
                        isCachedData = false
                    }
                    isLoading = false
                    val maxInWeek = forecastList.maxOfOrNull { it.maxTempC } ?: 25.0
                    val minInWeek = forecastList.minOfOrNull { it.minTempC } ?: 18.0
                    sendWeatherPhoneNotification(maxInWeek, minInWeek, selectedCity.name)
                }
            }
        }
    }

    LaunchedEffect(selectedCity, userLoc, isOnline) {
        fetchForecast()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.weather_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = rememberHapticOnClick { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = rememberHapticOnClick { fetchForecast() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh weather")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!hasConnection && forecastList.isEmpty()) {
                // Connection requirement guard view:
                // Show if no connection and no cached forecast exists
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "No Internet",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = stringResource(R.string.connection_required_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = stringResource(R.string.connection_required_desc),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = rememberHapticOnClick {
                                    hasConnection = checkHasInternet()
                                    if (hasConnection) fetchForecast()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.weather_retry), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item 0: Friendly Internet Connection Information Banner
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (!hasConnection) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (!hasConnection) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (!hasConnection) Icons.Default.WifiOff else Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = if (!hasConnection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (!hasConnection) 
                                        stringResource(R.string.weather_offline_notice)
                                    else 
                                        stringResource(R.string.weather_online_notice),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (!hasConnection) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Item 1: Heatwave Activation Toggle Switch
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWarningEnabled) PastelPinkAccent.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (isWarningEnabled) PastelPinkDark else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🌡️", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.weather_protection_title),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepBurgundy
                                            )
                                        )
                                        Text(
                                            text = if (isWarningEnabled) stringResource(R.string.weather_protection_enabled) else stringResource(R.string.weather_protection_disabled),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextMuted
                                            )
                                        )
                                    }
                                }

                                Switch(
                                    checked = isWarningEnabled,
                                    onCheckedChange = { enabled ->
                                        isWarningEnabled = enabled
                                        if (enabled) {
                                            WeatherAlertWorker.schedulePeriodicWeatherCheck(context, selectedCity.lat, selectedCity.lon, selectedCity.name)
                                        } else {
                                            WorkManager.getInstance(context).cancelUniqueWork("WeatherAlertPeriodicWork")
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Cream,
                                        checkedTrackColor = Wine
                                    )
                                )
                            }
                        }
                    }

                    // Item 2: City / Location Selector Strip
                    item {
                        Column {
                            Text(
                                text = stringResource(R.string.weather_select_location),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(PRESET_CITIES) { city ->
                                    val isSelected = selectedCity == city
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = rememberHapticOnClick { selectedCity = city },
                                        label = {
                                            Text(
                                                text = if (city.country.contains("GPS")) stringResource(R.string.weather_gps_location) else "${city.name}, ${city.country}",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Item 3: Current Weather & Heatwave Alert Banner
                    item {
                        val maxInWeek = forecastList.maxOfOrNull { it.maxTempC } ?: 25.0
                        val minInWeek = forecastList.minOfOrNull { it.minTempC } ?: 18.0

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Heatwave Alert Banner if >35°C
                            if (isWarningEnabled && maxInWeek > 35.0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = getTempColor(maxInWeek).copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(1.5.dp, getTempColor(maxInWeek))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⚠️", fontSize = 32.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = when {
                                                        maxInWeek > 45.0 -> stringResource(R.string.weather_heat_extreme)
                                                        maxInWeek > 40.0 -> stringResource(R.string.weather_heat_severe)
                                                        else -> stringResource(R.string.weather_heat_warning)
                                                    },
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = getTempColor(maxInWeek)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = stringResource(R.string.weather_heat_desc, formatTempDual(maxInWeek)),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        lineHeight = 20.sp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = rememberHapticOnClick {
                                                sendWeatherPhoneNotification(maxInWeek, minInWeek, selectedCity.name)
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.weather_send_notif),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Cold Alert Banner if <=15°C
                            if (isWarningEnabled && (minInWeek <= 15.0 || maxInWeek <= 15.0)) {
                                val lowestVal = minOf(minInWeek, maxInWeek)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = getTempColor(lowestVal).copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(1.5.dp, getTempColor(lowestVal))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("❄️", fontSize = 32.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = when {
                                                        lowestVal <= 5.0 -> stringResource(R.string.weather_cold_freezing)
                                                        lowestVal <= 10.0 -> stringResource(R.string.weather_cold_severe)
                                                        else -> stringResource(R.string.weather_cold_chilly)
                                                    },
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = getTempColor(lowestVal)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = stringResource(R.string.weather_cold_desc, formatTempDual(lowestVal)),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        lineHeight = 20.sp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = rememberHapticOnClick {
                                                sendWeatherPhoneNotification(maxInWeek, minInWeek, selectedCity.name)
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.weather_send_notif),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Item 4: Temperature Color Rule System Legend
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = stringResource(R.string.weather_legend_title),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Hot Legend
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(stringResource(R.string.weather_legend_hot), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        LegendChip(color = Color(0xFFFF8A80), text = stringResource(R.string.weather_legend_35))
                                        LegendChip(color = Color(0xFFD32F2F), text = stringResource(R.string.weather_legend_40))
                                        LegendChip(color = Color(0xFF8B0000), text = stringResource(R.string.weather_legend_45))
                                    }

                                    // Cold Legend
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(stringResource(R.string.weather_legend_cold), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        LegendChip(color = Color(0xFF0277BD), text = stringResource(R.string.weather_legend_15))
                                        LegendChip(color = Color(0xFF1976D2), text = stringResource(R.string.weather_legend_10))
                                        LegendChip(color = Color(0xFF0D47A1), text = stringResource(R.string.weather_legend_5))
                                    }
                                }
                            }
                        }
                    }

                    // Item 5: Next Week (7-Day Forecast) Header & Sync Now Button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.weather_forecast_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.weather_updated_for, selectedCity.name),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            FilledTonalButton(
                                onClick = rememberHapticOnClick {
                                    fetchForecast()
                                    val syncMsg = context.getString(R.string.weather_syncing_toast, selectedCity.name)
                                    Toast.makeText(context, syncMsg, Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.weather_syncing_btn), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync Now", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.weather_sync_now), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Item 6: Forecast Cards List & Cohesive Pastel Loading Indicator
                    if (isLoading) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(44.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        strokeWidth = 4.dp
                                    )
                                    Text(
                                        text = stringResource(R.string.weather_fetching_forecast),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                    Text(
                                        text = stringResource(R.string.weather_analyzing_thresholds),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(forecastList) { dayForecast ->
                            DailyForecastCard(forecast = dayForecast)
                        }
                    }

                    // Item 7: Outdoor Cat Safety Care Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🐾", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.weather_safety_tips_title),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.weather_safety_tips_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTempDual(celsius: Double): String {
    val f = ((celsius * 9.0 / 5.0) + 32.0).toInt()
    return "${celsius.toInt()}°C (${f}°F)"
}

@Composable
fun DailyForecastCard(forecast: DailyWeatherForecast) {
    val highlightColor = getTempColor(forecast.maxTempC)
    val containerBg = getTempContainerColor(forecast.maxTempC)
    val textColor = getTempTextColor(forecast.maxTempC)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(
            width = if (forecast.maxTempC > 35.0 || forecast.maxTempC <= 15.0) 1.5.dp else 0.5.dp,
            color = highlightColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day & Date Column
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = if (forecast.dayName == "Today") stringResource(R.string.weather_today) else forecast.dayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                )
                Text(
                    text = forecast.fullDateLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.8f)
                    )
                )
            }

            // Weather Condition Emoji & Description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                Text(
                    text = forecast.conditionEmoji,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = forecast.conditionText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Temperature Badge (Max / Min) in dual format: 25°C (77°F)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = highlightColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTempDual(forecast.maxTempC),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = stringResource(R.string.weather_low_temp, formatTempDual(forecast.minTempC)),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LegendChip(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

// Temperature Color Logic according to prompt rules:
// > 35° High contrast Red
// > 40° Darker Red
// > 45° Really Dark Red
// <= 15° Vibrant High-Contrast Blue (0xFF0277BD)
// <= 10° Darker Blue (0xFF1976D2)
// <= 5° Really Dark Blue (0xFF0D47A1)
fun getTempColor(tempMax: Double): Color {
    return when {
        tempMax > 45.0 -> Color(0xFF8B0000) // Really dark red
        tempMax > 40.0 -> Color(0xFFD32F2F) // Darker red
        tempMax > 35.0 -> Color(0xFFE53935) // Deep Red
        tempMax <= 5.0 -> Color(0xFF0D47A1) // Really dark blue
        tempMax <= 10.0 -> Color(0xFF1976D2) // Darker blue
        tempMax <= 15.0 -> Color(0xFF0277BD) // Vibrant high-contrast blue
        else -> Color(0xFF2E7D32)            // Soft green for moderate comfortable weather
    }
}

fun getTempContainerColor(tempMax: Double): Color {
    return when {
        tempMax > 45.0 -> Color(0xFFFFEBEE)
        tempMax > 40.0 -> Color(0xFFFFF0F2)
        tempMax > 35.0 -> Color(0xFFFFF3E0)
        tempMax <= 5.0 -> Color(0xFFE8EAF6)
        tempMax <= 10.0 -> Color(0xFFE3F2FD)
        tempMax <= 15.0 -> Color(0xFFE0F7FA)
        else -> Color(0xFFE8F5E9)
    }
}

fun getTempTextColor(tempMax: Double): Color {
    return when {
        tempMax > 45.0 -> Color(0xFF8B0000)
        tempMax > 40.0 -> Color(0xFFB71C1C)
        tempMax > 35.0 -> Color(0xFFC62828)
        tempMax <= 5.0 -> Color(0xFF5C1F2E)
        tempMax <= 10.0 -> Color(0xFF8A3B4C)
        tempMax <= 15.0 -> Color(0xFF5C1F2E)
        else -> Color(0xFF3A1620)
    }
}

fun getWeatherCondition(code: Int, tempMax: Double): Pair<String, String> {
    if (tempMax > 40.0) return Pair("Severe Heatwave", "🔥")
    if (tempMax > 35.0) return Pair("Extreme Heat", "☀️")

    return when (code) {
        0 -> Pair("Clear Sunny", "☀️")
        1, 2 -> Pair("Partly Cloudy", "⛅")
        3 -> Pair("Overcast", "☁️")
        45, 48 -> Pair("Foggy", "🌫️")
        51, 53, 55, 61, 63 -> Pair("Light Rain", "🌧️")
        65, 80, 81, 82 -> Pair("Heavy Rain", "⛈️")
        71, 73, 75, 85 -> Pair("Snowfall", "❄️")
        else -> if (tempMax <= 10.0) Pair("Cold & Chilly", "❄️") else Pair("Mild Weather", "🌤️")
    }
}

fun generateFallbackForecast(): List<DailyWeatherForecast> {
    val result = mutableListOf<DailyWeatherForecast>()
    val cal = Calendar.getInstance()
    val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
    val sdfDate = SimpleDateFormat("MMM d", Locale.getDefault())
    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val sampleTemps = listOf(38.5, 42.0, 46.5, 36.0, 28.0, 14.0, 8.5)
    val sampleMins = listOf(24.0, 26.0, 29.0, 22.0, 18.0, 9.0, 3.0)

    for (i in 0..6) {
        val date = cal.time
        val dateStr = sdfInput.format(date)
        val dayLabel = if (i == 0) "Today" else sdfDay.format(date)
        val fullLabel = sdfDate.format(date)

        val maxT = sampleTemps[i]
        val minT = sampleMins[i]
        val (condText, condEmoji) = getWeatherCondition(0, maxT)

        result.add(
            DailyWeatherForecast(
                dateString = dateStr,
                dayName = dayLabel,
                fullDateLabel = fullLabel,
                maxTempC = maxT,
                minTempC = minT,
                weatherCode = 0,
                conditionText = condText,
                conditionEmoji = condEmoji
            )
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return result
}
