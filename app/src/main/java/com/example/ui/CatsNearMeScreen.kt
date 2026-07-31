package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.StrayReport
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class FeedingSpot(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val icon: String = "🥣"
)

// Private constant theme color that doesn't require Composable resolution
private val Coral = Color(0xFFD97D7D)

@Composable
fun CatsNearMeScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val activeTab by viewModel.activeCatsNearMeTab.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val allReports by viewModel.allStrayReports.collectAsStateWithLifecycle()
    val onboardedName by viewModel.onboardedName.collectAsStateWithLifecycle()

    var permissionStatusMessage by remember { mutableStateOf<String?>(null) }
    var showPermissionAlert by remember { mutableStateOf(false) }

    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }
    val feedingSpots = remember {
        mutableStateListOf<FeedingSpot>().apply {
            val jsonStr = sharedPrefs.getString("saved_feeding_spots", null)
            if (jsonStr != null) {
                try {
                    val arr = org.json.JSONArray(jsonStr)
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

    // Resolve @Composable colors safely in parent composable scope
    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val wineColor = Wine
    val inkColor = Ink
    val whiteColor = White
    val textDarkColor = TextDark
    val pastelPinkAccentColor = PastelPinkAccent
    val pastelPinkDarkColor = PastelPinkDark



    var locationTriggered by remember { mutableStateOf(false) }

    com.example.util.LocationPermissionGate(
        onPermissionGranted = { lat, lon ->
            viewModel.updateUserLocation(lat, lon)
            permissionStatusMessage = context.getString(R.string.cats_loc_granted)
            showPermissionAlert = false
        },
        onPermissionDenied = {
            permissionStatusMessage = context.getString(R.string.cats_loc_denied)
            showPermissionAlert = true
        }
    ) { requestPermission ->
        // Run permission check on load
        LaunchedEffect(locationTriggered) {
            android.util.Log.d("LocationPermissionUI", "Location trigger check in CatsNearMeScreen: locationTriggered = $locationTriggered")
            if (!locationTriggered) {
                locationTriggered = true
                android.util.Log.d("LocationPermissionUI", "Triggering location permission request now via PermissionManager")
                requestPermission()
            }
        }

        Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        // Top back bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                modifier = Modifier.testTag("cats_near_me_back_btn")
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
                    text = stringResource(R.string.community_cooperative),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = wineColor.copy(alpha = 0.7f),
                    fontFamily = QuicksandFontFamily
                )
                Text(
                    text = stringResource(R.string.nav_cats_near_me_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = burgundyColor
                    )
                )
            }
        }

        // Sub-tab selectors (Browse vs Report vs Feeding Stations)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(creamColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .border(1.dp, mauveColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val checkAndSwitchTab = { tabKey: String ->
                viewModel.updateCatsNearMeTab(tabKey)
                val hasLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasLocationPermission) {
                    Toast.makeText(context, "Location permission required. Requesting now...", Toast.LENGTH_SHORT).show()
                    requestPermission()
                }
            }

            TabButton(
                text = stringResource(R.string.browse_nearby),
                isActive = activeTab == "browse",
                onClick = com.example.ui.theme.rememberHapticOnClick { checkAndSwitchTab("browse") },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = stringResource(R.string.report_stray),
                isActive = activeTab == "report",
                onClick = com.example.ui.theme.rememberHapticOnClick { checkAndSwitchTab("report") },
                modifier = Modifier.weight(1f)
            )
        }

        // Animated permission alert if denied or missing
        AnimatedVisibility(
            visible = showPermissionAlert,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = pastelPinkAccentColor.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, pastelPinkDarkColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.cats_loc_warning),
                            tint = pastelPinkDarkColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.simulated_mode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = pastelPinkDarkColor,
                                fontFamily = QuicksandFontFamily
                            )
                            Text(
                                text = stringResource(R.string.location_denied_desc),
                                fontSize = 11.sp,
                                color = textDarkColor,
                                fontFamily = QuicksandFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = com.example.ui.theme.rememberHapticOnClick {
                                try {
                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Open app settings on device", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, pastelPinkDarkColor),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(stringResource(R.string.open_settings), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = pastelPinkDarkColor)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                Toast.makeText(context, "Location permission requested...", Toast.LENGTH_SHORT).show()
                                requestPermission()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pastelPinkDarkColor),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(stringResource(R.string.cats_grant), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = whiteColor)
                        }
                    }
                }
            }
        }

        // Active View
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "Sub-tab animation"
        ) { targetTab ->
            when (targetTab) {
                "browse" -> {
                    BrowseNearbySection(
                        viewModel = viewModel,
                        userLat = userLocation.first,
                        userLng = userLocation.second,
                        feedingSpots = feedingSpots
                    )
                }
                "report" -> {
                    ReportStrayForm(viewModel = viewModel, userLat = userLocation.first, userLng = userLocation.second)
                }
            }
        }
    }
}
}

@Composable
fun TabButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val burgundyColor = DeepBurgundy
    val creamColor = Cream

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) burgundyColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) creamColor else burgundyColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = QuicksandFontFamily
        )
    }
}

@Composable
fun BrowseNearbySection(
    viewModel: TinyPawsViewModel,
    userLat: Double,
    userLng: Double,
    feedingSpots: androidx.compose.runtime.snapshots.SnapshotStateList<FeedingSpot>
) {

    val allReports by viewModel.allStrayReports.collectAsStateWithLifecycle()
    var radiusKm by remember { mutableFloatStateOf(10f) }
    var activeMapMode by remember { mutableStateOf("reports") } // "reports" or "feeding_spots"
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    val filteredFeedingSpots = remember(feedingSpots, radiusKm, userLat, userLng) {
        feedingSpots.map { spot ->
            val dist = viewModel.getDistanceInKm(userLat, userLng, spot.latitude, spot.longitude)
            spot to dist
        }.filter { it.second <= radiusKm }
         .sortedBy { it.second }
    }

    var selectedFeedingSpotForHighlight by remember { mutableStateOf<FeedingSpot?>(null) }

    // Resolve @Composable colors safely in parent composable scope
    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val inkColor = Ink
    val whiteColor = White
    val softGrayColor = SoftGray
    val blushPinkColor = BlushPink

    val density = LocalDensity.current
    val dotRadius = remember(density) { with(density) { 6.dp.toPx() } }
    val pulseRadius = remember(density) { with(density) { 12.dp.toPx() } }
    val ringStrokeWidth = remember(density) { with(density) { 1.5.dp.toPx() } }

    // Filter reports based on radius
    val filteredReports = remember(allReports, radiusKm, userLat, userLng) {
        allReports.map { report ->
            val dist = viewModel.getDistanceInKm(userLat, userLng, report.latitude, report.longitude)
            report to dist
        }.filter { it.second <= radiusKm }
         .sortedBy { it.second }
    }

    var selectedReportForHighlight by remember { mutableStateOf<StrayReport?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Radius Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .glassyCard(shape = RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.radius_filter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FrauncesFontFamily,
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
                                text = stringResource(R.string.radius_km, "%.1f".format(radiusKm)),
                                color = creamColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = QuicksandFontFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.radius_desc),
                        fontSize = 11.sp,
                        color = inkColor.copy(alpha = 0.7f),
                        fontFamily = QuicksandFontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 5f..20f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = burgundyColor,
                            activeTrackColor = burgundyColor,
                            inactiveTrackColor = mauveColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("radius_slider")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5f, 10f, 20f).forEach { value ->
                            val isSelected = radiusKm == value
                            FilterChip(
                                selected = isSelected,
                                onClick = com.example.ui.theme.rememberHapticOnClick { radiusKm = value },
                                label = { Text("${value.toInt()}km", fontFamily = QuicksandFontFamily, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = burgundyColor,
                                    selectedLabelColor = creamColor,
                                    containerColor = burgundyColor.copy(alpha = 0.05f),
                                    labelColor = burgundyColor
                                ),
                                modifier = Modifier.testTag("radius_chip_${value.toInt()}")
                            )
                        }
                    }
                }
            }
        }

        // Beautiful Interactive Radar Map View
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassyCard(shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.cats_radar_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = burgundyColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode Toggle Buttons (Reports vs Feeding Spots)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                activeMapMode = "reports" 
                                selectedReportForHighlight = null
                                selectedFeedingSpotForHighlight = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeMapMode == "reports") burgundyColor else burgundyColor.copy(alpha = 0.1f),
                                contentColor = if (activeMapMode == "reports") creamColor else burgundyColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("map_mode_reports_btn")
                        ) {
                            Text(stringResource(R.string.map_mode_reports), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = QuicksandFontFamily)
                        }
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                activeMapMode = "feeding_spots" 
                                selectedReportForHighlight = null
                                selectedFeedingSpotForHighlight = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeMapMode == "feeding_spots") blushPinkColor else blushPinkColor.copy(alpha = 0.2f),
                                contentColor = burgundyColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("map_mode_feeding_spots_btn")
                        ) {
                            Text(stringResource(R.string.map_mode_feeding_spots), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = QuicksandFontFamily)
                        }
                    }

                    // Radar Canvas Drawing
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.05f) else blushPinkColor.copy(alpha = 0.12f), CircleShape)
                            .border(1.5.dp, if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.2f) else blushPinkColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = size.width / 2

                            // Rings for 1/3, 2/3, and full radius
                            drawCircle(
                                color = if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.15f) else blushPinkColor.copy(alpha = 0.35f),
                                radius = maxRadius * 0.33f,
                                center = center,
                                style = Stroke(width = 3f)
                            )
                            drawCircle(
                                color = if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.15f) else blushPinkColor.copy(alpha = 0.35f),
                                radius = maxRadius * 0.66f,
                                center = center,
                                style = Stroke(width = 3f)
                            )
                            drawCircle(
                                color = if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.3f) else blushPinkColor.copy(alpha = 0.5f),
                                radius = maxRadius,
                                center = center,
                                style = Stroke(width = 4f)
                            )

                            // Crosshairs
                            drawLine(
                                color = if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.15f) else blushPinkColor.copy(alpha = 0.35f),
                                start = Offset(0f, center.y),
                                end = Offset(size.width, center.y),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.15f) else blushPinkColor.copy(alpha = 0.35f),
                                start = Offset(center.x, 0f),
                                end = Offset(center.x, size.height),
                                strokeWidth = 1f
                            )

                            // User center dot (refuge or actual)
                            drawCircle(
                                color = burgundyColor,
                                radius = dotRadius,
                                center = center
                            )
                            drawCircle(
                                color = if (activeMapMode == "reports") burgundyColor.copy(alpha = 0.3f) else blushPinkColor.copy(alpha = 0.5f),
                                radius = pulseRadius,
                                center = center,
                                style = Stroke(width = ringStrokeWidth)
                            )
                        }

                        if (activeMapMode == "reports") {
                            // Floating Cat Icons on Radar Map
                            filteredReports.forEach { (report, dist) ->
                                // Map the coordinate offset to radar visual coordinates
                                val angle = (report.id * 73) % 360 // Pseudo-random fixed angle for consistency
                                val angleRad = Math.toRadians(angle.toDouble())
                                val fraction = (dist / radiusKm).coerceIn(0.1, 1.0).toFloat()
                                val distanceDp = 90.dp * fraction

                                val offsetX = distanceDp * cos(angleRad).toFloat()
                                val offsetY = distanceDp * sin(angleRad).toFloat()

                                val isSelected = selectedReportForHighlight?.id == report.id

                                val animatedSize by androidx.compose.animation.core.animateDpAsState(
                                    targetValue = if (isSelected) 36.dp else 26.dp,
                                    label = "report_marker_size"
                                )

                                val pinShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 2.dp)

                                Box(
                                    modifier = Modifier
                                        .offset(x = offsetX, y = offsetY)
                                        .size(animatedSize)
                                        .background(if (isSelected) blushPinkColor else burgundyColor, pinShape)
                                        .border(
                                            BorderStroke(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) burgundyColor else blushPinkColor
                                            ),
                                            pinShape
                                        )
                                        .clickable { selectedReportForHighlight = report },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (report.photoUri) {
                                            "cat_orange" -> "🍊"
                                            "cat_black" -> "🐈‍⬛"
                                            "cat_grey" -> "🩶"
                                            "cat_calico" -> "🐱"
                                            else -> "🐾"
                                        },
                                        fontSize = if (isSelected) 15.sp else 11.sp
                                    )
                                }
                            }
                        } else {
                            // Floating Feeding Spot Icons on Radar Map (Green square pins)
                            filteredFeedingSpots.forEach { (spot, dist) ->
                                val angle = (spot.id * 109) % 360 // Pseudo-random fixed angle for consistency
                                val angleRad = Math.toRadians(angle.toDouble())
                                val fraction = (dist / radiusKm).coerceIn(0.1, 1.0).toFloat()
                                val distanceDp = 90.dp * fraction

                                val offsetX = distanceDp * cos(angleRad).toFloat()
                                val offsetY = distanceDp * sin(angleRad).toFloat()

                                val isSelected = selectedFeedingSpotForHighlight?.id == spot.id

                                val animatedSize by androidx.compose.animation.core.animateDpAsState(
                                    targetValue = if (isSelected) 36.dp else 26.dp,
                                    label = "spot_marker_size"
                                )

                                val spotShape = CircleShape

                                Box(
                                    modifier = Modifier
                                        .offset(x = offsetX, y = offsetY)
                                        .size(animatedSize)
                                        .background(if (isSelected) burgundyColor else blushPinkColor, spotShape)
                                        .border(
                                            BorderStroke(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) blushPinkColor else burgundyColor
                                            ),
                                            spotShape
                                        )
                                        .clickable { selectedFeedingSpotForHighlight = spot },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = spot.icon,
                                        fontSize = if (isSelected) 15.sp else 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Selected radar item callout
                    AnimatedVisibility(visible = activeMapMode == "reports" && selectedReportForHighlight != null) {
                        val selReport = selectedReportForHighlight
                        if (selReport != null) {
                            val dist = viewModel.getDistanceInKm(userLat, userLng, selReport.latitude, selReport.longitude)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(blushPinkColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, burgundyColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.selected_on_radar, selReport.needs),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = burgundyColor,
                                        fontFamily = QuicksandFontFamily
                                    )
                                    Text(
                                        text = stringResource(R.string.dist_away, "%.2f".format(dist)),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = burgundyColor,
                                        fontFamily = QuicksandFontFamily
                                    )
                                }
                                Text(
                                    text = selReport.description,
                                    fontSize = 11.sp,
                                    color = inkColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontFamily = QuicksandFontFamily
                                )
                            }
                        }
                    }

                    // Selected feeding spot callout
                    AnimatedVisibility(visible = activeMapMode == "feeding_spots" && selectedFeedingSpotForHighlight != null) {
                        val selSpot = selectedFeedingSpotForHighlight
                        if (selSpot != null) {
                            val dist = viewModel.getDistanceInKm(userLat, userLng, selSpot.latitude, selSpot.longitude)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(blushPinkColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, burgundyColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📍 ${selSpot.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = burgundyColor,
                                        fontFamily = QuicksandFontFamily
                                    )
                                    Text(
                                        text = "~${"%.2f".format(dist)} km",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = burgundyColor,
                                        fontFamily = QuicksandFontFamily
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }

        // Sub-title for lists
        item {
            Text(
                text = stringResource(R.string.stray_reports_count, filteredReports.size),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = burgundyColor,
                fontFamily = FrauncesFontFamily,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Empty state
        if (filteredReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = creamColor.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, mauveColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏡🐱💤", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.all_cats_accounted),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = burgundyColor
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.no_stray_reports, radiusKm),
                            fontSize = 12.sp,
                            color = inkColor.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontFamily = QuicksandFontFamily
                        )
                    }
                }
            }
        } else {
            items(filteredReports, key = { it.first.id }) { (report, distance) ->
                val isSelectedOnRadar = selectedReportForHighlight?.id == report.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(
                                if (isSelectedOnRadar) 2.dp else 0.dp,
                                if (isSelectedOnRadar) Coral else Color.Transparent
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .testTag("stray_report_card_${report.id}")
                        .clickable { selectedReportForHighlight = report },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = whiteColor)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Cat Sighting Photo / Presets
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(creamColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (report.photoUri.startsWith("content://") || report.photoUri.startsWith("file://")) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = Uri.parse(report.photoUri)),
                                    contentDescription = stringResource(R.string.cats_img_desc),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Template drawings
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            when (report.photoUri) {
                                                "cat_orange" -> Color(0xFFFFCC80)
                                                "cat_black" -> Color(0xFF424242)
                                                "cat_grey" -> Color(0xFFB0BEC5)
                                                "cat_calico" -> Color(0xFFFFE0B2)
                                                else -> mauveColor.copy(alpha = 0.5f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (report.photoUri) {
                                            "cat_orange" -> "🍊🐈"
                                            "cat_black" -> "🐈‍⬛🖤"
                                            "cat_grey" -> "🩶🐈"
                                            "cat_calico" -> "🐱✨"
                                            else -> "🐱🐾"
                                        },
                                        fontSize = 28.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Sighting details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (report.needs) {
                                                stringResource(R.string.cats_food_water) -> Color(0xFFE8F5E9)
                                                stringResource(R.string.cats_medical) -> Color(0xFFFFEBEE)
                                                stringResource(R.string.cats_shelter) -> Color(0xFFE3F2FD)
                                                else -> creamColor
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = report.needs,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = when (report.needs) {
                                            stringResource(R.string.cats_food_water) -> Color(0xFF2E7D32)
                                            stringResource(R.string.cats_medical) -> Color(0xFFC62828)
                                            stringResource(R.string.cats_shelter) -> Color(0xFF1565C0)
                                            else -> burgundyColor
                                        },
                                        fontFamily = QuicksandFontFamily
                                    )
                                }

                                Text(
                                    text = stringResource(R.string.dist_away, "%.1f".format(distance)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32),
                                    fontFamily = QuicksandFontFamily
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = report.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = inkColor,
                                    fontFamily = QuicksandFontFamily,
                                    lineHeight = 15.sp
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.reported_by, report.reporterName),
                                    fontSize = 10.sp,
                                    color = inkColor.copy(alpha = 0.6f),
                                    fontFamily = QuicksandFontFamily
                                )

                                val dateText = remember(report.timestamp) {
                                    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(report.timestamp))
                                }
                                Text(
                                    text = stringResource(R.string.today_at, dateText),
                                    fontSize = 9.sp,
                                    color = inkColor.copy(alpha = 0.5f),
                                    fontFamily = QuicksandFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStrayForm(
    viewModel: TinyPawsViewModel,
    userLat: Double,
    userLng: Double
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var description by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    val defaultNeed = stringResource(R.string.cats_food_water)
    var selectedNeed by remember { mutableStateOf(defaultNeed) }
    var selectedPhotoTemplate by remember { mutableStateOf("cat_orange") }
    var customPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Resolve @Composable colors safely in parent composable scope
    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val inkColor = Ink
    val whiteColor = White
    val softGrayColor = SoftGray

    // Launcher for custom photo picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            customPhotoUri = uri
            selectedPhotoTemplate = "custom"
        }
    }

    val needsOptions = listOf(stringResource(R.string.cats_food_water), stringResource(R.string.cats_medical), stringResource(R.string.cats_shelter), stringResource(R.string.cats_blanket))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("report_stray_form"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Welcome Tip Card
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
                    Text("📢", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.register_stray_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = burgundyColor
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.register_stray_desc),
                            fontSize = 11.sp,
                            color = inkColor.copy(alpha = 0.8f),
                            fontFamily = QuicksandFontFamily
                        )
                    }
                }
            }
        }

        // Form Fields
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
                    // Reporter Name
                    Column {
                        Text(
                            text = stringResource(R.string.your_name_optional),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = reporterName,
                            onValueChange = { reporterName = it },
                            placeholder = { Text(stringResource(R.string.cats_enter_name), fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reporter_name_input"),
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = burgundyColor,
                                unfocusedBorderColor = mauveColor.copy(alpha = 0.6f)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // Selected Needs Dropdown / Chips
                    Column {
                        Text(
                            text = stringResource(R.string.needs_most_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            needsOptions.take(2).forEach { option ->
                                NeedChip(
                                    text = option,
                                    isSelected = selectedNeed == option,
                                    onClick = com.example.ui.theme.rememberHapticOnClick {  selectedNeed = option },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            needsOptions.drop(2).forEach { option ->
                                NeedChip(
                                    text = option,
                                    isSelected = selectedNeed == option,
                                    onClick = com.example.ui.theme.rememberHapticOnClick {  selectedNeed = option },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Sighting Description
                    Column {
                        Text(
                            text = stringResource(R.string.sighting_details),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.cats_describe),
                                    fontSize = 12.sp,
                                    fontFamily = QuicksandFontFamily
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("sighting_description_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = burgundyColor,
                                unfocusedBorderColor = mauveColor.copy(alpha = 0.6f)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }
            }
        }

        // Cat Sighting Photo Selector Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.add_photo_sighting),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = burgundyColor,
                        fontFamily = QuicksandFontFamily
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.add_photo_desc),
                        fontSize = 11.sp,
                        color = inkColor.copy(alpha = 0.6f),
                        fontFamily = QuicksandFontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row of template icons/avatars or upload option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TemplateAvatarItem(
                            emoji = "🍊",
                            label = stringResource(R.string.cats_orange),
                            isSelected = selectedPhotoTemplate == "cat_orange",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                selectedPhotoTemplate = "cat_orange"
                                customPhotoUri = null
                            }
                        )
                        TemplateAvatarItem(
                            emoji = "🐈‍⬛",
                            label = stringResource(R.string.cats_black),
                            isSelected = selectedPhotoTemplate == "cat_black",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                selectedPhotoTemplate = "cat_black"
                                customPhotoUri = null
                            }
                        )
                        TemplateAvatarItem(
                            emoji = "🩶",
                            label = stringResource(R.string.cats_grey),
                            isSelected = selectedPhotoTemplate == "cat_grey",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                selectedPhotoTemplate = "cat_grey"
                                customPhotoUri = null
                            }
                        )
                        TemplateAvatarItem(
                            emoji = "🐱",
                            label = stringResource(R.string.cats_calico),
                            isSelected = selectedPhotoTemplate == "cat_calico",
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                selectedPhotoTemplate = "cat_calico"
                                customPhotoUri = null
                            }
                        )
                        TemplateAvatarItem(
                            emoji = "🖼️",
                            label = stringResource(R.string.cats_device),
                            isSelected = selectedPhotoTemplate == "custom" && customPhotoUri != null,
                            onClick = com.example.ui.theme.rememberHapticOnClick {  imagePickerLauncher.launch("image/*") }
                        )
                    }

                    // Display active preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(creamColor.copy(alpha = 0.5f))
                            .border(1.dp, mauveColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (customPhotoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = customPhotoUri),
                                contentDescription = stringResource(R.string.cats_custom_photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(stringResource(R.string.cats_img_loaded), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (selectedPhotoTemplate) {
                                        "cat_orange" -> "🍊"
                                        "cat_black" -> "🐈‍⬛"
                                        "cat_grey" -> "🩶"
                                        "cat_calico" -> "🐱"
                                        else -> "🐱"
                                    },
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.active_preset, selectedPhotoTemplate.substringAfter("cat_").uppercase()),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = burgundyColor,
                                    fontFamily = QuicksandFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        // Location status & trigger button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = softGrayColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.cats_loc_pin),
                        tint = burgundyColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.location_capture),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = inkColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Text(
                            text = stringResource(R.string.captured_coords, "%.4f".format(userLat), "%.4f".format(userLng)),
                            fontSize = 10.sp,
                            color = inkColor.copy(alpha = 0.6f),
                            fontFamily = QuicksandFontFamily
                        )
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    if (description.trim().isNotEmpty()) {
                        val photoPath = customPhotoUri?.toString() ?: selectedPhotoTemplate
                        viewModel.submitStrayReport(
                            description = description,
                            needs = selectedNeed,
                            latitude = userLat,
                            longitude = userLng,
                            photoUri = photoPath,
                            reporterName = reporterName.trim()
                        )
                        // Trigger daily activity points for reporting a cat!
                        val ctx = context
                        val strRes = ctx.getString(R.string.cats_reported_needing, selectedNeed)
                        viewModel.logActivity("rescue_cat", strRes)
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_stray_report_btn"),
                enabled = description.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = burgundyColor,
                    contentColor = creamColor,
                    disabledContainerColor = mauveColor.copy(alpha = 0.5f)
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cats_add_icon), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.submit_sighting),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                description = ""
                reporterName = ""
                customPhotoUri = null
                viewModel.updateCatsNearMeTab("browse")
            },
            confirmButton = {
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        showSuccessDialog = false
                        description = ""
                        reporterName = ""
                        customPhotoUri = null
                        viewModel.updateCatsNearMeTab("browse")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = burgundyColor)
                ) {
                    Text(stringResource(R.string.cats_super_browse), fontFamily = QuicksandFontFamily, color = creamColor)
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.report_registered),
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = burgundyColor
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.report_registered_desc),
                    fontFamily = QuicksandFontFamily,
                    color = inkColor
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = creamColor
        )
    }
}

@Composable
fun NeedChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inkColor = Ink
    val softGrayColor = SoftGray
    val mauveColor = Mauve
    val creamColor = Cream

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(if (isSelected) Coral else softGrayColor.copy(alpha = 0.2f))
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) Coral else mauveColor.copy(alpha = 0.3f)
                ),
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cats_selected),
                    tint = creamColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = if (isSelected) creamColor else inkColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = QuicksandFontFamily
            )
        }
    }
}

@Composable
fun TemplateAvatarItem(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val inkColor = Ink
    val softGrayColor = SoftGray
    val creamColor = Cream

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(if (isSelected) Coral.copy(alpha = 0.2f) else creamColor.copy(alpha = 0.4f), CircleShape)
                .border(
                    BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Coral else softGrayColor
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Coral else inkColor,
            fontFamily = QuicksandFontFamily
        )
    }
}

@Composable
fun FeedingStationsForm(
    viewModel: TinyPawsViewModel,
    userLat: Double,
    userLng: Double,
    feedingSpots: androidx.compose.runtime.snapshots.SnapshotStateList<FeedingSpot>,
    onSpotAdded: (FeedingSpot) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sharedPrefs = remember { context.getSharedPreferences("tinypaws_prefs", android.content.Context.MODE_PRIVATE) }

    var name by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🥣") }
    var caregiverName by remember { mutableStateOf("") }
    var radiusKm by remember { mutableFloatStateOf(10f) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val burgundyColor = DeepBurgundy
    val creamColor = Cream
    val mauveColor = Mauve
    val inkColor = Ink
    val whiteColor = White
    val softGrayColor = SoftGray

    val filteredSpots = remember(feedingSpots, radiusKm, userLat, userLng) {
        feedingSpots.map { spot ->
            val dist = viewModel.getDistanceInKm(userLat, userLng, spot.latitude, spot.longitude)
            spot to dist
        }.filter { it.second <= radiusKm }
         .sortedBy { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feeding_stations_form"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Welcome & Info Card
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
                    Text("🥣", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.feeding_station_welcome_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = burgundyColor,
                                fontFamily = QuicksandFontFamily
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.feeding_station_welcome_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = inkColor.copy(alpha = 0.8f),
                                fontFamily = QuicksandFontFamily
                            )
                        )
                    }
                }
            }
        }

        // Form fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteColor),
                border = BorderStroke(1.dp, mauveColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Station Name input
                    Column {
                        Text(
                            text = stringResource(R.string.feeding_station_name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.feeding_station_placeholder),
                                    fontSize = 12.sp,
                                    fontFamily = QuicksandFontFamily
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feeding_station_name_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = burgundyColor,
                                unfocusedBorderColor = mauveColor.copy(alpha = 0.6f)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // Station Details / Description input
                    Column {
                        Text(
                            text = stringResource(R.string.feeding_station_desc),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = details,
                            onValueChange = { details = it },
                            placeholder = {
                                Text(
                                    stringResource(R.string.feeding_station_desc_placeholder),
                                    fontSize = 12.sp,
                                    fontFamily = QuicksandFontFamily
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("feeding_station_desc_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = burgundyColor,
                                unfocusedBorderColor = mauveColor.copy(alpha = 0.6f)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                    }

                    // Caregiver/Reporter Name
                    Column {
                        Text(
                            text = stringResource(R.string.cats_enter_name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = caregiverName,
                            onValueChange = { caregiverName = it },
                            placeholder = {
                                Text(
                                    "Your name or anonymous caregiver",
                                    fontSize = 12.sp,
                                    fontFamily = QuicksandFontFamily
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feeding_station_caregiver_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = burgundyColor,
                                unfocusedBorderColor = mauveColor.copy(alpha = 0.6f)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }

                    // Icon / Marker emoji selector
                    Column {
                        Text(
                            text = "Choose Map Marker Icon",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val icons = listOf("🥣", "🐟", "🏡", "🐾", "🥫")
                            icons.forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedIcon == icon) burgundyColor else creamColor)
                                        .clickable { selectedIcon = icon }
                                        .testTag("station_icon_$icon"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(icon, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live location capture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = softGrayColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.cats_loc_pin),
                        tint = burgundyColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Live Location Requested",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = inkColor,
                            fontFamily = QuicksandFontFamily
                        )
                        Text(
                            text = stringResource(R.string.captured_coords, "%.4f".format(userLat), "%.4f".format(userLng)),
                            fontSize = 10.sp,
                            color = inkColor.copy(alpha = 0.6f),
                            fontFamily = QuicksandFontFamily
                        )
                    }
                }
            }
        }

        // Log Station Submit Button
        item {
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick { 
                    if (name.trim().isNotEmpty()) {
                        val newId = (feedingSpots.maxOfOrNull { it.id } ?: 0) + 1
                        val newSpot = FeedingSpot(
                            id = newId,
                            name = name.trim(),
                            latitude = userLat,
                            longitude = userLng,
                            icon = selectedIcon
                        )
                        feedingSpots.add(newSpot)

                        try {
                            val arr = org.json.JSONArray()
                            feedingSpots.forEach { spot ->
                                arr.put(org.json.JSONObject().apply {
                                    put("id", spot.id)
                                    put("name", spot.name)
                                    put("latitude", spot.latitude)
                                    put("longitude", spot.longitude)
                                    put("icon", spot.icon)
                                })
                            }
                            sharedPrefs.edit().putString("saved_feeding_spots", arr.toString()).apply()
                            
                            // Log daily activity
                            val caregiver = caregiverName.trim().ifEmpty { "Anonymous Caregiver" }
                            viewModel.logActivity("add_feeding_spot", "Logged new feeding spot: ${name.trim()} by $caregiver")
                            
                            showSuccessDialog = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_feeding_station_btn"),
                enabled = name.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = burgundyColor,
                    contentColor = creamColor,
                    disabledContainerColor = mauveColor.copy(alpha = 0.5f)
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Station", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Log Feeding Station",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Search Feeding Stations Near You Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteColor),
                border = BorderStroke(1.dp, mauveColor.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Search Feeding Stations Near You",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = burgundyColor,
                            fontFamily = QuicksandFontFamily
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search Radius: ${"%.1f".format(radiusKm)} km",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = inkColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuicksandFontFamily
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = burgundyColor,
                            activeTrackColor = burgundyColor,
                            inactiveTrackColor = mauveColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("station_radius_slider")
                    )

                    Text(
                        text = stringResource(R.string.stray_feeding_stations_count, filteredSpots.size),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = inkColor,
                            fontFamily = QuicksandFontFamily
                        )
                    )

                    if (filteredSpots.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_feeding_stations_reports, radiusKm),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = inkColor.copy(alpha = 0.6f),
                                fontFamily = QuicksandFontFamily
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        filteredSpots.forEach { (spot, dist) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(creamColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(spot.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = spot.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor,
                                            fontFamily = QuicksandFontFamily
                                        )
                                    )
                                    Text(
                                        text = "Distance: ${"%.2f".format(dist)} km",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = inkColor.copy(alpha = 0.6f),
                                            fontFamily = QuicksandFontFamily
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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSpotAdded(feedingSpots.last())
            },
            confirmButton = {
                Button(
                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                        showSuccessDialog = false
                        onSpotAdded(feedingSpots.last())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = burgundyColor)
                ) {
                    Text("OK", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = creamColor)
                }
            },
            title = {
                Text(
                    text = "Station Logged! 🎉",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = burgundyColor
                )
            },
            text = {
                Text(
                    text = "Your neighborhood feeding station has been successfully recorded. It will now appear on the Interactive Stray Radar Map!",
                    fontFamily = QuicksandFontFamily,
                    color = inkColor
                )
            },
            containerColor = whiteColor
        )
    }
}
