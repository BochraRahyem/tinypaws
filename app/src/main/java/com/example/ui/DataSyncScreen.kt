package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*
import com.example.util.CsvExportUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSyncScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val profile by viewModel.catProfile.collectAsStateWithLifecycle()
    val careLogs by viewModel.allCareLogs.collectAsStateWithLifecycle()
    val weightLogs by viewModel.allWeightLogs.collectAsStateWithLifecycle()
    val diaryLogs by viewModel.allCheckInLogs.collectAsStateWithLifecycle()
    val historyEntries by viewModel.allHistoryEntries.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val backupMessage by viewModel.cloudBackupState.collectAsStateWithLifecycle()

    var statusSnackBar by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.datasync_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("datasync_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Intro Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BlushPink.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📦", fontSize = 48.sp)
                    Text(
                        text = stringResource(R.string.datasync_intro_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepBurgundy,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.datasync_intro_desc),
                        fontFamily = QuicksandFontFamily,
                        fontSize = 13.sp,
                        color = Ink.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // CSV Export Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_csv_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DeepBurgundy.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.datasync_csv_title),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                text = stringResource(R.string.datasync_csv_desc),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 12.sp,
                                color = Ink.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick {
                            val file = CsvExportUtil.exportAndShareCsv(
                                context = context,
                                profile = profile,
                                careLogs = careLogs,
                                weightLogs = weightLogs,
                                diaryLogs = diaryLogs,
                                historyEntries = historyEntries
                            )
                            if (file == null) {
                                statusSnackBar = context.getString(R.string.datasync_csv_err)
                            } else {
                                Toast.makeText(context, context.getString(R.string.datasync_csv_success), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_export_csv"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Cream, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.datasync_csv_btn),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Cream
                        )
                    }
                }
            }

            // Cloud Sync Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_cloud_sync_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Wine.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Wine, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.datasync_cloud_title),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                text = stringResource(R.string.datasync_cloud_desc),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 12.sp,
                                color = Ink.copy(alpha = 0.75f)
                            )
                        }
                    }

                    if (isSyncing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DeepBurgundy,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(R.string.cloud_syncing_label),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 13.sp,
                                color = DeepBurgundy
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    viewModel.backupDataToCloud(context) { msg ->
                                        statusSnackBar = msg
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_cloud_backup"),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Cream, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.cloud_backup_btn), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = Cream, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    viewModel.restoreDataFromCloud(context) { msg ->
                                        statusSnackBar = msg
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_cloud_restore"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy),
                                border = BorderStroke(1.dp, DeepBurgundy),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.cloud_restore_btn), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy, fontSize = 13.sp)
                            }
                        }
                    }

                    if (backupMessage != null) {
                        Text(
                            text = backupMessage ?: "",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepBurgundy,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (statusSnackBar != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { statusSnackBar = null }) {
                            Text(stringResource(R.string.main_ok), color = Cream)
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
