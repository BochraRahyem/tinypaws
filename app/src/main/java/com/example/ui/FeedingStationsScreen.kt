package com.example.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun FeedingStationsScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val onboardedName by viewModel.onboardedName.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("browse_stations") } // "browse_stations" or "report_station"
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE) }

    val feedingSpots = remember {
        mutableStateListOf<FeedingSpot>().apply {
            val jsonStr = sharedPrefs.getString("saved_feeding_spots", null)
            if (jsonStr != null) {
                try {
                    val arr = JSONArray(jsonStr)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        add(
                            FeedingSpot(
                                id = obj.getInt("id"),
                                name = obj.getString("name"),
                                latitude = obj.getDouble("latitude"),
                                longitude = obj.getDouble("longitude"),
                                icon = obj.optString("icon", "🥣")
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                add(FeedingSpot(1, "Sidi Bou Said Feeding Station", userLocation.first + 0.003, userLocation.second - 0.002, "🥣"))
                add(FeedingSpot(2, "La Marsa Port Feeding Station", userLocation.first - 0.002, userLocation.second + 0.003, "🐟"))
                add(FeedingSpot(3, "Refuge Garden Feed Bowl", userLocation.first + 0.001, userLocation.second + 0.001, "🏡"))
            }
        }
    }

    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val wineColor = Wine
    val inkColor = Ink

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                modifier = Modifier.testTag("feeding_stations_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cats_back_home),
                    tint = burgundyColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.hi_user, onboardedName.ifEmpty { "Caregiver" }),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = wineColor.copy(alpha = 0.7f),
                    fontFamily = QuicksandFontFamily
                )
                Text(
                    text = stringResource(R.string.nav_feeding_stations_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = burgundyColor
                    )
                )
            }
        }

        // Sub-tab selectors (Browse Feeding Stations vs Report Feeding Station)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(creamColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .border(1.dp, mauveColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TabButton(
                text = "🍽️ Browse Stations",
                isActive = activeTab == "browse_stations",
                onClick = com.example.ui.theme.rememberHapticOnClick { activeTab = "browse_stations" },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "➕ Report Station",
                isActive = activeTab == "report_station",
                onClick = com.example.ui.theme.rememberHapticOnClick { activeTab = "report_station" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area based on activeTab
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "FeedingStationsTabs"
        ) { tab ->
            if (tab == "browse_stations") {
                BrowseFeedingStationsSection(
                    viewModel = viewModel,
                    userLat = userLocation.first,
                    userLng = userLocation.second,
                    feedingSpots = feedingSpots
                )
            } else {
                ReportFeedingStationForm(
                    viewModel = viewModel,
                    userLat = userLocation.first,
                    userLng = userLocation.second,
                    onStationAdded = { newSpot ->
                        feedingSpots.add(newSpot)
                        // Save to sharedPrefs
                        try {
                            val arr = JSONArray()
                            feedingSpots.forEach { spot ->
                                arr.put(JSONObject().apply {
                                    put("id", spot.id)
                                    put("name", spot.name)
                                    put("latitude", spot.latitude)
                                    put("longitude", spot.longitude)
                                    put("icon", spot.icon)
                                })
                            }
                            sharedPrefs.edit().putString("saved_feeding_spots", arr.toString()).apply()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        activeTab = "browse_stations"
                        Toast.makeText(context, "Feeding station successfully registered! 🥣", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun BrowseFeedingStationsSection(
    viewModel: TinyPawsViewModel,
    userLat: Double,
    userLng: Double,
    feedingSpots: List<FeedingSpot>
) {
    var radiusKm by remember { mutableFloatStateOf(15f) }
    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val inkColor = Ink
    val whiteColor = White

    val filteredSpots = remember(feedingSpots, radiusKm, userLat, userLng) {
        feedingSpots.map { spot ->
            val dist = viewModel.getDistanceInKm(userLat, userLng, spot.latitude, spot.longitude)
            spot to dist
        }.filter { it.second <= radiusKm }
         .sortedBy { it.second }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Radius Filter Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteColor),
                border = BorderStroke(1.dp, mauveColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search Radius",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = burgundyColor
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(burgundyColor, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${"%.1f".format(radiusKm)} km",
                                color = creamColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = burgundyColor,
                            activeTrackColor = burgundyColor
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "Nearby Feeding Stations (${filteredSpots.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = burgundyColor
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (filteredSpots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No feeding stations found within ${"%.1f".format(radiusKm)} km.",
                        color = inkColor.copy(alpha = 0.6f),
                        fontFamily = QuicksandFontFamily
                    )
                }
            }
        } else {
            items(filteredSpots) { (spot, dist) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feeding_station_card_${spot.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = whiteColor),
                    border = BorderStroke(1.dp, mauveColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(creamColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = spot.icon, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spot.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = burgundyColor,
                                fontFamily = FrauncesFontFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📍 GPS Coordinates: %.4f, %.4f".format(spot.latitude, spot.longitude),
                                fontSize = 11.sp,
                                color = inkColor.copy(alpha = 0.7f),
                                fontFamily = QuicksandFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚡ Real Distance: ${"%.2f".format(dist)} km away",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Wine,
                                fontFamily = QuicksandFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFeedingStationForm(
    viewModel: TinyPawsViewModel,
    userLat: Double,
    userLng: Double,
    onStationAdded: (FeedingSpot) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var stationName by remember { mutableStateOf("") }
    var stationDesc by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🥣") }

    val icons = listOf("🥣", "🐟", "🏡", "🥛", "🌿")
    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val whiteColor = White
    val inkColor = Ink

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("report_feeding_station_form"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = creamColor.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, mauveColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🍽️", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Register a Feeding Station",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = burgundyColor
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Add local feeding bowls so nearby caregivers can coordinate food replenishment.",
                            fontSize = 11.sp,
                            color = inkColor.copy(alpha = 0.8f),
                            fontFamily = QuicksandFontFamily
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Feeding Station Name",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stationName,
                            onValueChange = { stationName = it },
                            placeholder = { Text("e.g. Marina Cat Bowl Station") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("station_name_input"),
                            shape = CircleShape,
                            singleLine = true
                        )
                    }

                    Column {
                        Text(
                            text = "Station Icon / Emoji",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            icons.forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedIcon == icon) burgundyColor else creamColor)
                                        .clickable { selectedIcon = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = icon, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "Description / Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = stationDesc,
                            onValueChange = { stationDesc = it },
                            placeholder = { Text("e.g. Placed under the shaded olive tree, replenished daily at 8am.") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("station_desc_input"),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SoftGray.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = burgundyColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Live GPS Location Attached",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = inkColor
                        )
                        Text(
                            text = "Lat: %.4f, Lng: %.4f".format(userLat, userLng),
                            fontSize = 10.sp,
                            color = inkColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    if (stationName.isNotBlank()) {
                        val newSpot = FeedingSpot(
                            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                            name = stationName.trim(),
                            latitude = userLat + (Math.random() - 0.5) * 0.002,
                            longitude = userLng + (Math.random() - 0.5) * 0.002,
                            icon = selectedIcon
                        )
                        viewModel.logActivity("add_feeding_station", "Registered feeding station: ${stationName.trim()}")
                        onStationAdded(newSpot)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_feeding_station_btn"),
                enabled = stationName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = burgundyColor,
                    contentColor = creamColor
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Register Feeding Station",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
