package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vaccines
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
import com.example.data.Reminder
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("all") }

    val filteredReminders = remember(reminders, selectedCategoryFilter) {
        if (selectedCategoryFilter == "all") reminders
        else reminders.filter { it.type == selectedCategoryFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.reminders_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("reminders_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = DeepBurgundy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { showAddDialog = true },
                containerColor = DeepBurgundy,
                contentColor = Cream,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Reminder", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = BlushPink.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DeepBurgundy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Cream)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vet & Vaccine Alarms",
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DeepBurgundy
                        )
                        Text(
                            text = "Schedule local push notifications for upcoming vet visits, vaccination boosters, and medication.",
                            fontFamily = QuicksandFontFamily,
                            fontSize = 12.sp,
                            color = Ink.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf(
                    "all" to "All Reminders",
                    "vet_visit" to "🏥 Vet Visits",
                    "vaccination" to "💉 Vaccinations",
                    "medication" to "💊 Medication",
                    "general" to "✨ General Care"
                )
                items(categories) { (tag, label) ->
                    val isSelected = selectedCategoryFilter == tag
                    FilterChip(
                        selected = isSelected,
                        onClick = com.example.ui.theme.rememberHapticOnClick { selectedCategoryFilter = tag },
                        label = {
                            Text(
                                text = label,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepBurgundy,
                            selectedLabelColor = Cream,
                            containerColor = Color.White.copy(alpha = 0.4f),
                            labelColor = DeepBurgundy
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Mauve,
                            selectedBorderColor = DeepBurgundy,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            // Reminders List
            if (filteredReminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = Mauve,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = if (selectedCategoryFilter == "all") "No care reminders set yet.\nTap '+ New Reminder' below to schedule one!"
                            else "No reminders found for this category.",
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Wine.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredReminders, key = { it.id }) { reminder ->
                        ReminderItemCard(
                            reminder = reminder,
                            onDelete = {
                                viewModel.deleteReminder(reminder.id)
                                Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddReminderDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, timeMillis, type ->
                    viewModel.saveReminder(title, timeMillis, type)
                    val notifId = (System.currentTimeMillis() % 10000).toInt()
                    notificationHelper.scheduleNotification(notifId, title, timeMillis)
                    val dateFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timeMillis))
                    Toast.makeText(context, "🔔 Scheduled: '$title' for $dateFormatted", Toast.LENGTH_LONG).show()
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ReminderItemCard(
    reminder: Reminder,
    onDelete: () -> Unit
) {
    val dateStr = remember(reminder.timeMillis) {
        SimpleDateFormat("EEEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(reminder.timeMillis))
    }

    val (badgeText, badgeColor, icon) = when (reminder.type) {
        "vet_visit" -> Triple("🏥 Vet Visit", Color(0xFFD32F2F), Icons.Default.MedicalServices)
        "vaccination" -> Triple("💉 Vaccination", Color(0xFF7B1113), Icons.Default.Vaccines)
        "medication" -> Triple("💊 Medication", Color(0xFF1976D2), Icons.Default.Notifications)
        else -> Triple("✨ General Care", DeepBurgundy, Icons.Default.DateRange)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Mauve.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = reminder.title,
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DeepBurgundy
                    )
                    Text(
                        text = dateStr,
                        fontFamily = QuicksandFontFamily,
                        fontSize = 13.sp,
                        color = Ink.copy(alpha = 0.75f)
                    )
                }
            }
            IconButton(
                onClick = com.example.ui.theme.rememberHapticOnClick { onDelete() },
                modifier = Modifier.testTag("delete_reminder_${reminder.id}")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedError.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("vet_visit") }
    var timeMillis by remember { mutableStateOf(System.currentTimeMillis() + 3600_000L) }
    val context = LocalContext.current

    val presetTitles = when (selectedType) {
        "vet_visit" -> listOf("Annual Vet Checkup 🏥", "Dental Care Exam 🦷", "General Health Consultation 🩺")
        "vaccination" -> listOf("Rabies Vaccine Booster 💉", "FVRCP Vaccination 💉", "FeLV Vaccination 💉")
        "medication" -> listOf("Flea & Tick Prevention 💊", "Deworming Dose 💊", "Daily Antibiotics 💊")
        else -> listOf("Grooming & Nail Trim ✂️", "Litter Refresh 🧹", "Weight Check ⚖️")
    }

    LaunchedEffect(selectedType) {
        if (title.isBlank() || title in presetTitles) {
            title = presetTitles.first()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Schedule Care Reminder",
                fontFamily = FrauncesFontFamily,
                fontWeight = FontWeight.Bold,
                color = DeepBurgundy
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Select Category:",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Wine
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        "vet_visit" to "🏥 Vet Visit",
                        "vaccination" to "💉 Vaccine",
                        "medication" to "💊 Meds",
                        "general" to "✨ General"
                    )
                    types.forEach { (typeKey, typeLabel) ->
                        val isSel = selectedType == typeKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedType = typeKey }
                                .border(
                                    BorderStroke(1.dp, if (isSel) DeepBurgundy else Mauve),
                                    RoundedCornerShape(10.dp)
                                ),
                            color = if (isSel) DeepBurgundy else Color.White.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = typeLabel,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSel) Cream else DeepBurgundy,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title", fontFamily = QuicksandFontFamily) },
                    placeholder = { Text("e.g. Annual Vet Checkup", fontFamily = QuicksandFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBurgundy,
                        unfocusedBorderColor = Mauve
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date & Time Picker Button
                OutlinedButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = timeMillis
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                calendar.set(y, m, d)
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        calendar.set(Calendar.HOUR_OF_DAY, h)
                                        calendar.set(Calendar.MINUTE, min)
                                        timeMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Mauve)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = DeepBurgundy)
                        Text(
                            text = dateFormatter.format(Date(timeMillis)),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    if (title.isNotBlank()) {
                        onAdd(title, timeMillis, selectedType)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy, contentColor = Cream)
            ) {
                Text("Schedule Alarm", fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = com.example.ui.theme.rememberHapticOnClick { onDismiss() }) {
                Text("Cancel", fontFamily = QuicksandFontFamily, color = Wine)
            }
        }
    )
}
