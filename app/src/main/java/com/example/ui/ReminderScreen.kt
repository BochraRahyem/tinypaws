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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Reminder
import com.example.data.CatProfile
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: TinyPawsViewModel,
    initialCategory: String = "all",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val catProfiles by viewModel.allCatProfiles.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var showAddDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }
    var selectedCategoryFilter by remember(initialCategory) { mutableStateOf(initialCategory) }

    val filteredReminders = remember(reminders, selectedCategoryFilter) {
        if (selectedCategoryFilter == "all") reminders
        else reminders.filter { it.type == selectedCategoryFilter }
    }

    val catMeal = stringResource(R.string.reminder_type_meal)
    val catMed = stringResource(R.string.reminder_type_medication)
    val catVet = stringResource(R.string.reminder_type_vet)
    val catVac = stringResource(R.string.reminder_type_vaccine)
    val catGen = stringResource(R.string.reminder_type_general)

    val categories = remember(catMeal, catMed, catVet, catVac, catGen) {
        listOf(
            "all" to "All",
            "meal" to catMeal,
            "medication" to catMed,
            "vet_visit" to catVet,
            "vaccination" to catVac,
            "general" to catGen
        )
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
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    reminderToEdit = null
                    showAddDialog = true
                },
                containerColor = DeepBurgundy,
                contentColor = Cream,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.reminder_add_title), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
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
                            text = stringResource(R.string.hub_care_reminders_title),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DeepBurgundy
                        )
                        Text(
                            text = stringResource(R.string.hub_care_reminders_desc),
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
                            text = if (selectedCategoryFilter == "all") "No care reminders set yet.\nTap '+ Schedule Care Reminder' below!"
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
                            onToggleEnable = { enabled ->
                                val updated = reminder.copy(isEnabled = enabled)
                                viewModel.updateReminder(updated)
                                if (enabled) {
                                    scheduleAlarm(context, notificationHelper, updated, catProfiles)
                                } else {
                                    notificationHelper.cancelNotification(reminder.id)
                                }
                            },
                            onEdit = {
                                reminderToEdit = reminder
                                showAddDialog = true
                            },
                            onDelete = {
                                notificationHelper.cancelNotification(reminder.id)
                                viewModel.deleteReminder(reminder.id)
                                Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddOrEditReminderDialog(
                reminderToEdit = reminderToEdit,
                catProfiles = catProfiles,
                onDismiss = {
                    showAddDialog = false
                    reminderToEdit = null
                },
                onSave = { newOrUpdatedReminder ->
                    if (newOrUpdatedReminder.id == 0) {
                        viewModel.saveReminder(newOrUpdatedReminder) { assignedId ->
                            val finalReminder = newOrUpdatedReminder.copy(id = assignedId.toInt())
                            if (finalReminder.isEnabled) {
                                scheduleAlarm(context, notificationHelper, finalReminder, catProfiles)
                            }
                        }
                        Toast.makeText(context, "🔔 Scheduled: '${newOrUpdatedReminder.title}'", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateReminder(newOrUpdatedReminder)
                        if (newOrUpdatedReminder.isEnabled) {
                            scheduleAlarm(context, notificationHelper, newOrUpdatedReminder, catProfiles)
                        } else {
                            notificationHelper.cancelNotification(newOrUpdatedReminder.id)
                        }
                        Toast.makeText(context, "Updated: '${newOrUpdatedReminder.title}'", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                    reminderToEdit = null
                }
            )
        }
    }
}

private fun scheduleAlarm(
    context: android.content.Context, 
    helper: NotificationHelper, 
    reminder: Reminder, 
    catProfiles: List<CatProfile>
) {
    if (!reminder.isEnabled) return
    val cal = Calendar.getInstance().apply { timeInMillis = reminder.timeMillis }
    
    val catNames = if (reminder.catIds == "all") {
        "All My Cats"
    } else {
        val ids = reminder.catIds.split(",").mapNotNull { it.toIntOrNull() }
        val targetedCats = catProfiles.filter { ids.contains(it.id) }
        targetedCats.joinToString(", ") { it.name }
    }
    
    val displayTitle = if (catNames.isNotBlank()) "${reminder.title} ($catNames)" else reminder.title

    if (reminder.isRecurring) {
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        helper.scheduleDailyRecurringNotification(reminder.id, displayTitle, hour, min)
    } else {
        helper.scheduleNotification(reminder.id, displayTitle, reminder.timeMillis)
    }
}

@Composable
fun ReminderItemCard(
    reminder: Reminder,
    onToggleEnable: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(reminder.timeMillis, reminder.isRecurring) {
        if (reminder.isRecurring) {
            val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(reminder.timeMillis))
            "Daily at $formattedTime"
        } else {
            SimpleDateFormat("EEEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(reminder.timeMillis))
        }
    }

    val (badgeText, badgeColor, icon) = when (reminder.type) {
        "meal" -> Triple(stringResource(R.string.reminder_type_meal), Color(0xFFE65100), Icons.Default.Notifications)
        "medication" -> Triple(stringResource(R.string.reminder_type_medication), Color(0xFF1976D2), Icons.Default.Notifications)
        "vet_visit" -> Triple(stringResource(R.string.reminder_type_vet), Color(0xFFD32F2F), Icons.Default.MedicalServices)
        "vaccination" -> Triple(stringResource(R.string.reminder_type_vaccine), Color(0xFF7B1113), Icons.Default.Vaccines)
        else -> Triple(stringResource(R.string.reminder_type_general), DeepBurgundy, Icons.Default.DateRange)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, if (reminder.isEnabled) Mauve.copy(alpha = 0.5f) else Mauve.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
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

                    if (reminder.isRecurring) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DeepBurgundy.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(12.dp))
                                Text(
                                    text = stringResource(R.string.reminder_recurring_daily),
                                    fontFamily = QuicksandFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = DeepBurgundy
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggleEnable(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Cream,
                        checkedTrackColor = DeepBurgundy
                    ),
                    modifier = Modifier.scale(0.85f).testTag("reminder_toggle_${reminder.id}")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = reminder.title,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (reminder.isEnabled) DeepBurgundy else Ink.copy(alpha = 0.5f)
                        )
                        Text(
                            text = dateStr,
                            fontFamily = QuicksandFontFamily,
                            fontSize = 12.5.sp,
                            color = Ink.copy(alpha = 0.75f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onEdit() },
                        modifier = Modifier.testTag("edit_reminder_${reminder.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DeepBurgundy.copy(alpha = 0.8f))
                    }
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onDelete() },
                        modifier = Modifier.testTag("delete_reminder_${reminder.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_desc), tint = RedError.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

// Extension to scale composable slightly for switch sizing
private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)

@Composable
fun AddOrEditReminderDialog(
    reminderToEdit: Reminder? = null,
    catProfiles: List<com.example.data.CatProfile> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val defaultMeal = stringResource(R.string.reminder_meal_default_text)
    val defaultMed = stringResource(R.string.reminder_medication_default_text)

    var title by remember { mutableStateOf(reminderToEdit?.title ?: defaultMeal) }
    var selectedType by remember { mutableStateOf(reminderToEdit?.type ?: "meal") }
    var isRecurring by remember { mutableStateOf(reminderToEdit?.isRecurring ?: true) }
    var timeMillis by remember { mutableStateOf(reminderToEdit?.timeMillis ?: (System.currentTimeMillis() + 3600_000L)) }
    var leadTimeMinutes by remember { mutableStateOf(0) }
    var selectedCatIdsString by remember { mutableStateOf(reminderToEdit?.catIds ?: "all") }
    val context = LocalContext.current

    val presetTitles = when (selectedType) {
        "meal" -> listOf(defaultMeal, "Breakfast Feeding 🥣", "Dinner Feast 🐟", "Wet Food Snack 🥫")
        "medication" -> listOf(defaultMed, "Flea & Tick Dose 💊", "Deworming Dose 💊", "Daily Antibiotics 💊")
        "vet_visit" -> listOf("Annual Vet Checkup 🏥", "Dental Exam 🦷", "General Health Consultation 🩺")
        "vaccination" -> listOf("Rabies Vaccine Booster 💉", "FVRCP Vaccination 💉", "FeLV Vaccination 💉")
        else -> listOf("Grooming & Nail Trim ✂️", "Litter Box Refresh 🧹", "Weight Check ⚖️")
    }

    LaunchedEffect(selectedType) {
        if (reminderToEdit == null && (title.isBlank() || title in presetTitles || title == defaultMeal || title == defaultMed)) {
            title = presetTitles.first()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
    val timeOnlyFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val catMeal = stringResource(R.string.reminder_type_meal)
    val catMed = stringResource(R.string.reminder_type_medication)
    val catVet = stringResource(R.string.reminder_type_vet)
    val catVac = stringResource(R.string.reminder_type_vaccine)
    val catGen = stringResource(R.string.reminder_type_general)

    val types = remember(catMeal, catMed, catVet, catVac, catGen) {
        listOf(
            "meal" to catMeal,
            "medication" to catMed,
            "vet_visit" to catVet,
            "vaccination" to catVac,
            "general" to catGen
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (reminderToEdit != null) stringResource(R.string.reminder_edit_title) else stringResource(R.string.reminder_add_title),
                fontFamily = FrauncesFontFamily,
                fontWeight = FontWeight.Bold,
                color = DeepBurgundy
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type selector
                Text(
                    text = "Category:",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Wine
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(types) { (typeKey, typeLabel) ->
                        val isSel = selectedType == typeKey
                        Surface(
                            modifier = Modifier
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
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }

                // Target Cat Selector
                if (catProfiles.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.multi_cat_select_cats),
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Wine
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isAll = selectedCatIdsString == "all"
                            FilterChip(
                                selected = isAll,
                                onClick = { selectedCatIdsString = "all" },
                                label = { Text(stringResource(R.string.multi_cat_all_cats), fontSize = 11.sp) }
                            )
                        }
                        items(catProfiles) { cat ->
                            val activeList = selectedCatIdsString.split(",").filter { it.isNotBlank() && it != "all" }
                            val isSelected = activeList.contains(cat.id.toString())
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val currentList = activeList.toMutableList()
                                    if (isSelected) {
                                        currentList.remove(cat.id.toString())
                                    } else {
                                        currentList.add(cat.id.toString())
                                    }
                                    selectedCatIdsString = if (currentList.isEmpty()) "all" else currentList.joinToString(",")
                                },
                                label = { Text("🐱 ${cat.name}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_title_label), fontFamily = QuicksandFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBurgundy,
                        unfocusedBorderColor = Mauve
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Daily Recurring Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reminder_recurring_daily),
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = DeepBurgundy
                    )
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Cream,
                            checkedTrackColor = DeepBurgundy
                        )
                    )
                }

                // Date/Time Picker Button
                OutlinedButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = timeMillis
                        if (isRecurring) {
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
                        } else {
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
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Mauve)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = DeepBurgundy)
                        Text(
                            text = if (isRecurring) "Time: ${timeOnlyFormatter.format(Date(timeMillis))}" else dateFormatter.format(Date(timeMillis)),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy
                        )
                    }
                }

                // Lead Time Options
                Text(
                    text = "Notify Me:",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Wine
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val leadOptions = listOf(
                        0 to "Exact Time",
                        10 to "10m before",
                        20 to "20m before",
                        30 to "30m before"
                    )
                    leadOptions.forEach { (mins, label) ->
                        val isSel = leadTimeMinutes == mins
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { leadTimeMinutes = mins }
                                .border(
                                    BorderStroke(1.dp, if (isSel) DeepBurgundy else Mauve.copy(alpha = 0.5f)),
                                    RoundedCornerShape(8.dp)
                                ),
                            color = if (isSel) DeepBurgundy else Color.White.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = label,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                color = if (isSel) Cream else DeepBurgundy,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    if (title.isNotBlank()) {
                        val triggerTime = timeMillis - (leadTimeMinutes * 60_000L)
                        val reminder = Reminder(
                            id = reminderToEdit?.id ?: 0,
                            title = title,
                            timeMillis = triggerTime,
                            type = selectedType,
                            isRecurring = isRecurring,
                            isEnabled = true,
                            catIds = selectedCatIdsString
                        )
                        onSave(reminder)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy, contentColor = Cream)
            ) {
                Text(
                    text = if (reminderToEdit != null) "Save Changes" else "Schedule Reminder",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = com.example.ui.theme.rememberHapticOnClick { onDismiss() }) {
                Text(stringResource(R.string.cancel_btn), fontFamily = QuicksandFontFamily, color = Wine)
            }
        }
    )
}
