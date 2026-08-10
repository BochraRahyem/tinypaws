package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {
    @SuppressLint("MissingPermission")
    fun getHighAccuracyLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationReceived(null)
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            
            // Request fresh high accuracy location using newer Play Services API
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { freshLoc: Location? ->
                if (freshLoc != null) {
                    onLocationReceived(freshLoc)
                } else {
                    // Fallback to last known location if GPS getCurrentLocation returns null
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                onLocationReceived(lastLoc)
                            } else {
                                onLocationReceived(getSystemLocation(context))
                            }
                        }.addOnFailureListener {
                            onLocationReceived(getSystemLocation(context))
                        }
                    } catch (ex: Exception) {
                        onLocationReceived(getSystemLocation(context))
                    }
                }
            }.addOnFailureListener {
                // Fallback to last known location
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            onLocationReceived(lastLoc)
                        } else {
                            onLocationReceived(getSystemLocation(context))
                        }
                    }.addOnFailureListener {
                        onLocationReceived(getSystemLocation(context))
                    }
                } catch (ex: Exception) {
                    onLocationReceived(getSystemLocation(context))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationHelper", "GMS FusedLocationProvider failed, falling back to System LocationManager", e)
            onLocationReceived(getSystemLocation(context))
        }
    }

    private fun getSystemLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            ?: return null
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
            return bestLocation
        } catch (e: SecurityException) {
            android.util.Log.e("LocationHelper", "SecurityException getting system location", e)
        } catch (e: Exception) {
            android.util.Log.e("LocationHelper", "Exception getting system location", e)
        }
        return null
    }
}

@Composable
fun LocationPermissionGate(
    onPermissionGranted: (latitude: Double, longitude: Double) -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showDeniedDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        android.util.Log.d("LocationPermission", "Permission launcher callback received, fineGranted = $fineGranted, coarseGranted = $coarseGranted")
        if (fineGranted || coarseGranted) {
            LocationHelper.getHighAccuracyLocation(context) { location ->
                android.util.Log.d("LocationPermission", "Location helper returned: $location")
                if (location != null) {
                    onPermissionGranted(location.latitude, location.longitude)
                } else {
                    // Safe default fallback when permission is granted but GPS is temporarily unavailable
                    android.util.Log.d("LocationPermission", "GPS returned null, using fallback location Tunis")
                    onPermissionGranted(36.8065, 10.1815)
                }
            }
        } else {
            android.util.Log.d("LocationPermission", "Permission denied by user. Showing friendly settings fallback dialog.")
            showDeniedDialog = true
            onPermissionDenied()
        }
    }

    val requestPermissionFlow = {
        android.util.Log.d("LocationPermission", "requestPermissionFlow invoked")
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        android.util.Log.d("LocationPermission", "Current location permission status: hasFine = $hasFine, hasCoarse = $hasCoarse")
        if (hasFine || hasCoarse) {
            LocationHelper.getHighAccuracyLocation(context) { location ->
                android.util.Log.d("LocationPermission", "Location helper returned: $location")
                if (location != null) {
                    onPermissionGranted(location.latitude, location.longitude)
                } else {
                    // Safe default fallback when permission is granted but GPS is temporarily unavailable
                    android.util.Log.d("LocationPermission", "GPS returned null, using fallback location Tunis")
                    onPermissionGranted(36.8065, 10.1815)
                }
            }
        } else {
            android.util.Log.d("LocationPermission", "Permission ACCESS_FINE_LOCATION/COARSE not yet granted. Requesting permission directly from system.")
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // 2. Friendly Denied Dialog with Open Settings option
    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Location Required", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Text(
                    text = "Without location permission, TinyPaws cannot find nearby street cats or veterinarians automatically. Please enable location services in your Android device settings.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeniedDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Safe fallback
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeniedDialog = false }
                ) {
                    Text("Later", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    content(requestPermissionFlow)
}


