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
    var locationTriggered by remember { mutableStateOf(false) }

    com.example.util.LocationPermissionGate(
        onPermissionGranted = { lat, lon, _ ->
            viewModel.updateUserLocation(lat, lon)
            viewModel.updateLocationQuery("${"%.4f".format(lat)}, ${"%.4f".format(lon)}")
            viewModel.searchPlaces(searchCategory, "$lat,$lon")
        },
        onPermissionDenied = {
            if (queryText.isEmpty()) {
                viewModel.updateLocationQuery(context.getString(R.string.loc_tunis))
                viewModel.searchPlaces(searchCategory, context.getString(R.string.loc_tunis))
            }
        }
    ) { requestPermission ->
        // On-load request permissions
        LaunchedEffect(locationTriggered) {
            android.util.Log.d("LocationPermissionUI", "Location trigger check in LocationModule: locationTriggered = $locationTriggered")
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
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                modifier = Modifier.testTag("back_to_dashboard")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.loc_back_to_home),
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
                            IconButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    android.widget.Toast.makeText(context, context.getString(R.string.cats_loc_req_toast), android.widget.Toast.LENGTH_SHORT).show()
                                    requestPermission()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = stringResource(R.string.location_icon_desc),
                                    tint = PastelPurpleDark
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick {
                            android.widget.Toast.makeText(context, context.getString(R.string.cats_loc_req_toast), android.widget.Toast.LENGTH_SHORT).show()
                            requestPermission()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PastelPurpleDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.loc_detect_loc), fontSize = 12.sp, color = PastelPurpleDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Three big category buttons (Nearby Vets, Nearby Shops, Nearby Shelters)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val checkAndSearch = { category: String ->
                    val hasLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (hasLocationPermission) {
                        viewModel.searchPlaces(category, queryText)
                    } else {
                        android.widget.Toast.makeText(context, context.getString(R.string.cats_loc_req_msg), android.widget.Toast.LENGTH_SHORT).show()
                        requestPermission()
                    }
                }

                CategorySearchButton(
                    title = stringResource(R.string.location_vets_btn),
                    categoryKey = "vets",
                    isActive = searchCategory == "vets",
                    modifier = Modifier.weight(1f),
                    onClick = com.example.ui.theme.rememberHapticOnClick { checkAndSearch("vets") }
                )
                CategorySearchButton(
                    title = stringResource(R.string.location_shops_btn),
                    categoryKey = "shops",
                    isActive = searchCategory == "shops",
                    modifier = Modifier.weight(1f),
                    onClick = com.example.ui.theme.rememberHapticOnClick { checkAndSearch("shops") }
                )
                CategorySearchButton(
                    title = stringResource(R.string.location_shelters_btn),
                    categoryKey = "shelters",
                    isActive = searchCategory == "shelters",
                    modifier = Modifier.weight(1f),
                    onClick = com.example.ui.theme.rememberHapticOnClick { checkAndSearch("shelters") }
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
                                    text = stringResource(R.string.loc_dynamic_map_title),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PastelPurpleDark
                                )
                                IconButton(onClick = com.example.ui.theme.rememberHapticOnClick {  activeMapPlace = null }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.loc_close_map))
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
                                    Text(stringResource(R.string.loc_pin_name, place.name), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                    Text(stringResource(R.string.loc_pin_coords, place.latitude, place.longitude), fontSize = 10.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = com.example.ui.theme.rememberHapticOnClick { 
                                            // Format the Google Maps search query to prioritize user's vicinity
                                            val searchUrl = "https://www.google.com/maps/search/${Uri.encode(place.name)}+near+me"
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                                            context.startActivity(mapIntent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(stringResource(R.string.loc_take_me_there), fontSize = 11.sp, color = White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results List Header
            Text(
                text = stringResource(R.string.loc_discovered_results),
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
                    Text(stringResource(R.string.loc_no_results), color = TextMuted, textAlign = TextAlign.Center)
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
                                        text = stringResource(R.string.loc_rating, place.rating),
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
                                        contentDescription = stringResource(R.string.loc_phone),
                                        modifier = Modifier.size(12.dp),
                                        tint = TextMuted
                                    )
                                    Text(place.contact, fontSize = 10.sp, color = TextMuted)
                                }
                                Text(
                                    text = stringResource(R.string.cats_dist_km_approx, distanceEst),
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
        onClick = com.example.ui.theme.rememberHapticOnClick { onClick() },
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
