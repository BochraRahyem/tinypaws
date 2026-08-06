package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCatHubScreen(
    viewModel: TinyPawsViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val profile by viewModel.catProfile.collectAsStateWithLifecycle()
    val careLogs by viewModel.allCareLogs.collectAsStateWithLifecycle()
    val weightLogs by viewModel.allWeightLogs.collectAsStateWithLifecycle()
    val diaryLogs by viewModel.allCheckInLogs.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val backupMessage by viewModel.cloudBackupState.collectAsStateWithLifecycle()

    var statusSnackBar by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.hub_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("hub_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.hub_back),
                            tint = DeepBurgundy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HubButton(
                title = stringResource(R.string.hub_cat_profile),
                subtitle = "Manage cat details, coat color, and age",
                icon = Icons.Default.Info,
                tag = "hub_cat_profile_btn",
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("my_cat_profile") }
            )

            HubButton(
                title = "Daily Care Checklist",
                subtitle = "Toggle daily feeding, water, playing & meds",
                icon = Icons.Default.CheckCircle,
                tag = "hub_daily_checklist_btn",
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("daily_checklist") }
            )

            HubButton(
                title = "Cat Care Diary Feed",
                subtitle = "Searchable memories & Coil image photo carousel",
                icon = Icons.Default.Book,
                tag = "hub_diary_feed_btn",
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("diary_feed") }
            )

            HubButton(
                title = "Growth & Weight Tracking",
                subtitle = "Log historical weights & view trend line curves",
                icon = Icons.Default.DateRange,
                tag = "hub_weight_tracker_btn",
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("weight_tracker") }
            )

            HubButton(
                title = "Vet & Vaccination Alarms",
                subtitle = "Schedule push notifications for vet appointments",
                icon = Icons.Default.Notifications,
                tag = "hub_reminders_btn",
                onClick = com.example.ui.theme.rememberHapticOnClick { onNavigate("reminders") }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Mauve.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))

            // CSV Export Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_csv_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DeepBurgundy.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Export Health Logs to CSV",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                "Share growth chart, care logs, and diary history directly with your veterinarian.",
                                fontFamily = QuicksandFontFamily,
                                fontSize = 12.sp,
                                color = Ink.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick {
                            val file = com.example.util.CsvExportUtil.exportAndShareCsv(
                                context = context,
                                profile = profile,
                                careLogs = careLogs,
                                weightLogs = weightLogs,
                                diaryLogs = diaryLogs
                            )
                            if (file == null) {
                                statusSnackBar = "Unable to export CSV file."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_csv"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Cream, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Export & Share CSV",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Cream
                        )
                    }
                }
            }

            // Cloud Sync & Backup (Firebase Firestore)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_cloud_sync_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Wine.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Wine, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Cloud Sync & Backup",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                "Sync diary entries, care checklist & weight logs securely via Firebase Firestore.",
                                fontFamily = QuicksandFontFamily,
                                fontSize = 12.sp,
                                color = Ink.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isSyncing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DeepBurgundy,
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Syncing with cloud...",
                                fontFamily = QuicksandFontFamily,
                                fontSize = 13.sp,
                                color = DeepBurgundy
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    viewModel.backupDataToCloud { msg ->
                                        statusSnackBar = msg
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_cloud_backup"),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Cream, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = Cream, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    viewModel.restoreDataFromCloud { msg ->
                                        statusSnackBar = msg
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_cloud_restore"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy),
                                border = BorderStroke(1.dp, DeepBurgundy),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy, fontSize = 13.sp)
                            }
                        }
                    }

                    if (backupMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = backupMessage ?: "",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepBurgundy
                        )
                    }
                }
            }

            if (statusSnackBar != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { statusSnackBar = null }) {
                            Text("OK", color = Cream)
                        }
                    },
                    containerColor = DeepBurgundy,
                    contentColor = Cream,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(statusSnackBar ?: "", fontFamily = QuicksandFontFamily)
                }
            }
        }
    }
}

@Composable
fun HubButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = com.example.ui.theme.rememberHapticOnClick { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BlushPink.copy(alpha = 0.35f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DeepBurgundy
                )
                Text(
                    subtitle,
                    fontFamily = QuicksandFontFamily,
                    fontSize = 12.sp,
                    color = Ink.copy(alpha = 0.75f)
                )
            }
        }
    }
}
