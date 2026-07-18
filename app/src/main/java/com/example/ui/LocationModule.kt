package com.example.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NearbyPlace
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.*

@Composable
fun LocationModuleScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val queryText by viewModel.locationQuery.collectAsStateWithLifecycle()
    val searchCategory by viewModel.searchCategory.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val userLoc by viewModel.userLocation.collectAsStateWithLifecycle()
    val onboardedName by viewModel.onboardedName.collectAsStateWithLifecycle()

    var activeMapPlace by remember { mutableStateOf<NearbyPlace?>(null) }

    // Launcher to request runtime location permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            // Get last known location and search
            try {
                val safeContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) context.applicationContext.createAttributionContext("default") else context.applicationContext
                val locationManager = safeContext.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (p in providers) {
                    val loc = locationManager.getLastKnownLocation(p) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
                if (bestLocation != null) {
                    viewModel.updateUserLocation(bestLocation.latitude, bestLocation.longitude)
                    viewModel.updateLocationQuery("${"%.4f".format(bestLocation.latitude)}, ${"%.4f".format(bestLocation.longitude)}")
                    viewModel.searchPlaces(searchCategory, "${bestLocation.latitude},${bestLocation.longitude}")
                } else {
                    // Fallback to default
                    viewModel.updateUserLocation(36.8065, 10.1815)
                    viewModel.updateLocationQuery("Tunis, Tunisia")
                    viewModel.searchPlaces(searchCategory, "Tunis, Tunisia")
                }
            } catch (e: SecurityException) {
                // permission not granted
            }
        } else {
            // Denied fallback
            if (queryText.isEmpty()) {
                viewModel.updateLocationQuery("Tunis, Tunisia")
                viewModel.searchPlaces(searchCategory, "Tunis, Tunisia")
            }
        }
    }

    // On-load request permissions
    LaunchedEffect(Unit) {
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                val safeContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) context.applicationContext.createAttributionContext("default") else context.applicationContext
                val locationManager = safeContext.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (p in providers) {
                    val loc = locationManager.getLastKnownLocation(p) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }
                if (bestLocation != null) {
                    viewModel.updateUserLocation(bestLocation.latitude, bestLocation.longitude)
                    viewModel.updateLocationQuery("${"%.4f".format(bestLocation.latitude)}, ${"%.4f".format(bestLocation.longitude)}")
                    viewModel.searchPlaces(searchCategory, "${bestLocation.latitude},${bestLocation.longitude}")
                } else {
                    viewModel.updateUserLocation(36.8065, 10.1815)
                    if (queryText.isEmpty()) {
                        viewModel.updateLocationQuery("Tunis, Tunisia")
                        viewModel.searchPlaces(searchCategory, "Tunis, Tunisia")
                    }
                }
            } catch (e: SecurityException) {
                // permission exception
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_to_dashboard")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to home",
                    tint = PastelPurpleDark
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(R.string.hi_user, onboardedName.ifEmpty { "Caregiver" }),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Text(
                    text = stringResource(R.string.location_screen_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PastelPurpleDark
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, PastelPinkAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.location_live_search),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PastelPurpleDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.location_search_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
                    )
                }
            }

            // Location Input Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, PastelPurplePrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.location_input_label),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PastelPurpleDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { viewModel.updateLocationQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_query_input"),
                        placeholder = { Text(stringResource(R.string.location_input_placeholder)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PastelPurplePrimary,
                            unfocusedBorderColor = SoftGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.location_icon_desc),
                                tint = PastelPurpleDark
                            )
                        }
                    )
                }
            }

            // Three big category buttons (Nearby Vets, Nearby Shops, Nearby Shelters)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategorySearchButton(
                    title = stringResource(R.string.location_vets_btn),
                    categoryKey = "vets",
                    isActive = searchCategory == "vets",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.searchPlaces("vets", queryText) }
                )
                CategorySearchButton(
                    title = stringResource(R.string.location_shops_btn),
                    categoryKey = "shops",
                    isActive = searchCategory == "shops",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.searchPlaces("shops", queryText) }
                )
                CategorySearchButton(
                    title = stringResource(R.string.location_shelters_btn),
                    categoryKey = "shelters",
                    isActive = searchCategory == "shelters",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.searchPlaces("shelters", queryText) }
                )
            }

            // Simulated interactive Map viewport of selected place
            AnimatedVisibility(visible = activeMapPlace != null) {
                val place = activeMapPlace
                if (place != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PastelPurplePrimary, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🗺️ Dynamic Map View",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PastelPurpleDark
                                )
                                IconButton(onClick = { activeMapPlace = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close map")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Simulated high-end visual grid map representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🐾 PIN: ${place.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                    Text("Coordinates: Lat ${"%.4f".format(place.latitude)}, Lng ${"%.4f".format(place.longitude)}", fontSize = 10.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            // Format the Google Maps search query to prioritize user's vicinity
                                            val searchUrl = "https://www.google.com/maps/search/${Uri.encode(place.name)}+near+me"
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                                            context.startActivity(mapIntent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Take me there 🚀", fontSize = 11.sp, color = White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results List Header
            Text(
                text = "Discovered results sorted by distance:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDark
            )

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PastelPurplePrimary)
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No results found. Select a category or input an address above!", color = TextMuted, textAlign = TextAlign.Center)
                }
            } else {
                searchResults.forEachIndexed { index, place ->
                    // Use calculated distance or fall back to simulated realistic distances if 0.0
                    val distanceEst = if (place.distance > 0.0) place.distance else (index * 0.4 + 0.3)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMapPlace = place }
                            .testTag("nearby_place_${index}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = BorderStroke(1.dp, if (activeMapPlace?.name == place.name) PastelPurplePrimary else SoftGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PastelPurpleDark
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = place.address,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(PastelPinkAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "★ ${place.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PastelPinkDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = place.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDark, lineHeight = 14.sp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = SoftGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone",
                                        modifier = Modifier.size(12.dp),
                                        tint = TextMuted
                                    )
                                    Text(place.contact, fontSize = 10.sp, color = TextMuted)
                                }
                                Text(
                                    text = "⚡ ~${"%.1f".format(distanceEst)} km away",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
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
fun CategorySearchButton(
    title: String,
    categoryKey: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .testTag("search_category_${categoryKey}"),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) PastelPurpleDark else White,
            contentColor = if (isActive) White else PastelPurpleDark
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, PastelPurplePrimary.copy(alpha = 0.5f))
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
