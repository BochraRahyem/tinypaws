package com.example.ui

import android.app.DatePickerDialog
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.util.shimmerEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.DiaryEntry
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryFeedScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkInLogs by viewModel.allCheckInLogs.collectAsStateWithLifecycle()
    val isDiaryLoading by viewModel.isDiaryLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("all") }
    var showAddDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // Search and filter logic
    val filteredLogs = remember(checkInLogs, searchQuery, selectedCategoryFilter) {
        checkInLogs.filter { log ->
            val matchesCategory = if (selectedCategoryFilter == "all") true else log.diaryEntryType == selectedCategoryFilter
            
            val formattedDate = dateFormatter.format(Date(log.date))
            val queryLower = searchQuery.trim().lowercase()

            val matchesSearch = if (queryLower.isBlank()) true else {
                log.notes.lowercase().contains(queryLower) ||
                log.mood.lowercase().contains(queryLower) ||
                log.diaryEntryType.lowercase().contains(queryLower) ||
                formattedDate.lowercase().contains(queryLower)
            }

            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.diary_feed_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("diary_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_desc),
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
                modifier = Modifier.testTag("add_diary_entry_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.diary_new_entry), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SEARCH FILTER BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diary_search_bar"),
                placeholder = {
                    Text(
                        stringResource(R.string.diary_search_placeholder),
                        fontFamily = QuicksandFontFamily,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = DeepBurgundy)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = com.example.ui.theme.rememberHapticOnClick { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Wine)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepBurgundy,
                    unfocusedBorderColor = Mauve,
                    focusedContainerColor = Color.White.copy(alpha = 0.4f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.25f)
                )
            )

            // CATEGORY FILTER CHIPS
            val filters = listOf(
                "all" to stringResource(R.string.diary_filter_all),
                "vet_visit" to stringResource(R.string.diary_filter_vet),
                "vaccination" to stringResource(R.string.diary_filter_vaccination),
                "grooming" to stringResource(R.string.diary_filter_grooming),
                "general" to stringResource(R.string.diary_filter_general)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { (tag, label) ->
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

            // Results Counter
            if (searchQuery.isNotBlank() || selectedCategoryFilter != "all") {
                Text(
                    text = "Showing ${filteredLogs.size} matching ${if (filteredLogs.size == 1) "entry" else "entries"}",
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Wine.copy(alpha = 0.8f)
                )
            }

            // DIARY FEED LIST
            if (isDiaryLoading) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) {
                        DiaryEntryShimmerItem()
                    }
                }
            } else if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = null,
                            tint = Mauve,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No diary entries match '$searchQuery'."
                            else "No diary entries recorded yet.\nTap '+ New Entry' to log a memory!",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        SwipeableDiaryEntry(
                            log = log,
                            onDelete = {
                                viewModel.deleteCheckInLog(log.id)
                                Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddDiaryEntryDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { date, mood, notes, weight, photosStr, category, scheduleReminder ->
                    viewModel.saveCheckInLog(
                        date = date,
                        mood = mood,
                        notes = notes,
                        weight = weight,
                        photos = photosStr,
                        diaryEntryType = category,
                        reminderTimeMillis = if (scheduleReminder) date else null
                    )
                    if (scheduleReminder) {
                        val title = when (category) {
                            "vet_visit" -> "🏥 Upcoming Vet Appointment"
                            "vaccination" -> "💉 Vaccination Booster Reminder"
                            else -> "🐾 Cat Care Reminder"
                        }
                        val newReminder = com.example.data.Reminder(
                            title = title,
                            timeMillis = date,
                            type = category,
                            catId = viewModel.selectedCatId.value,
                            isEnabled = true
                        )
                        viewModel.saveReminder(newReminder) { assignedId ->
                            notificationHelper.scheduleNotification(assignedId.toInt(), title, date)
                        }
                        Toast.makeText(context, "Local notification scheduled!", Toast.LENGTH_LONG).show()
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun DiaryEntryCard(
    log: com.example.data.DiaryEntry,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
    val dateStr = remember(log.date) { dateFormatter.format(Date(log.date)) }

    val photosList = remember(log.photos) {
        if (log.photos.isBlank()) emptyList()
        else log.photos.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    var previewImageUri by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Mauve.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Category Badge + Date + Mood + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (badgeLabel, badgeColor) = when (log.diaryEntryType) {
                        "vet_visit" -> "🏥 Vet Visit" to Color(0xFFD32F2F)
                        "vaccination" -> "💉 Vaccine" to Color(0xFF7B1113)
                        "grooming" -> "✂️ Grooming" to Color(0xFF1976D2)
                        else -> "📝 Note" to DeepBurgundy
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeLabel,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    val moodText = when (log.mood) {
                        "happy" -> stringResource(R.string.mood_happy)
                        "playful" -> stringResource(R.string.mood_playful)
                        "tired" -> stringResource(R.string.mood_relaxed)
                        "unwell" -> stringResource(R.string.mood_unwell)
                        else -> log.mood
                    }
                    Text(
                        text = moodText,
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Ink
                    )
                }

                IconButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick { onDelete() },
                    modifier = Modifier.size(28.dp).testTag("delete_diary_${log.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_desc), tint = RedError.copy(alpha = 0.8f))
                }
            }

            Text(
                text = dateStr,
                fontFamily = QuicksandFontFamily,
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.65f)
            )

            if (log.notes.isNotBlank()) {
                Text(
                    text = log.notes,
                    fontFamily = QuicksandFontFamily,
                    fontSize = 14.sp,
                    color = DeepBurgundy,
                    lineHeight = 20.sp
                )
            }

            // COIL IMAGE CAROUSEL FOR SAVED CAT PHOTOS
            if (photosList.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Wine, modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.diary_saved_photos, photosList.size),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Wine
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(photosList) { photoUri ->
                            CoilPhotoThumbnail(
                                photoUri = photoUri,
                                onClick = { previewImageUri = photoUri }
                            )
                        }
                    }
                }
            }
        }
    }

    // Full-screen Image Preview Dialog
    previewImageUri?.let { uri ->
        Dialog(onDismissRequest = { previewImageUri = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CoilPhotoLarge(photoUri = uri)
                    Button(
                        onClick = com.example.ui.theme.rememberHapticOnClick { previewImageUri = null },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy, contentColor = Cream)
                    ) {
                        Text(stringResource(R.string.diary_close), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CoilPhotoThumbnail(
    photoUri: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { onClick() })
            .border(BorderStroke(1.dp, Mauve), RoundedCornerShape(14.dp)),
        color = Color.Black.copy(alpha = 0.05f)
    ) {
        if (photoUri.startsWith("drawable/")) {
            val resName = photoUri.substringAfter("drawable/")
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Cat Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Mauve)
                }
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Saved Cat Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CoilPhotoLarge(
    photoUri: String
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri.startsWith("drawable/")) {
            val resName = photoUri.substringAfter("drawable/")
            val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId != 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Cat Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Saved Cat Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AddDiaryEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (Long, String, String, Float?, String, String, Boolean) -> Unit
) {
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedMood by remember { mutableStateOf("happy") }
    var selectedCategory by remember { mutableStateOf("general") }
    var notes by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var scheduleReminder by remember { mutableStateOf(false) }

    // Photos list selection (preset adorable cat photos)
    val availablePhotos = listOf(
        "drawable/cat_orange",
        "drawable/cat_calico",
        "drawable/cat_black",
        "drawable/cat_white",
        "drawable/ic_cat_orange"
    )
    val selectedPhotos = remember { mutableStateListOf<String>() }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.diary_add_title), fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Picker
                Text(stringResource(R.string.diary_category_label), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Wine)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val cats = listOf(
                        "general" to stringResource(R.string.diary_cat_general),
                        "vet_visit" to stringResource(R.string.diary_cat_vet),
                        "vaccination" to stringResource(R.string.diary_cat_vaccination),
                        "grooming" to stringResource(R.string.diary_cat_grooming)
                    )
                    cats.forEach { (catKey, catLabel) ->
                        val isSel = selectedCategory == catKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = catKey }
                                .border(BorderStroke(1.dp, if (isSel) DeepBurgundy else Mauve), RoundedCornerShape(8.dp)),
                            color = if (isSel) DeepBurgundy else Color.White.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = catLabel,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSel) Cream else DeepBurgundy,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Mood selector
                Text(stringResource(R.string.diary_mood_label), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Wine)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val moods = listOf(
                        "happy" to stringResource(R.string.mood_happy),
                        "playful" to stringResource(R.string.mood_playful),
                        "tired" to stringResource(R.string.mood_relaxed),
                        "unwell" to stringResource(R.string.mood_unwell)
                    )
                    moods.forEach { (mKey, mLabel) ->
                        val isSel = selectedMood == mKey
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedMood = mKey },
                            label = { Text(mLabel, fontFamily = QuicksandFontFamily, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.diary_notes_label), fontFamily = QuicksandFontFamily) },
                    placeholder = { Text(stringResource(R.string.diary_notes_placeholder), fontFamily = QuicksandFontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBurgundy,
                        unfocusedBorderColor = Mauve
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weightInput = it },
                    label = { Text(stringResource(R.string.diary_weight_label), fontFamily = QuicksandFontFamily) },
                    placeholder = { Text(stringResource(R.string.diary_weight_placeholder), fontFamily = QuicksandFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBurgundy,
                        unfocusedBorderColor = Mauve
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Select Cat Photos
                Text(stringResource(R.string.diary_attach_photos), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Wine)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availablePhotos) { photoTag ->
                        val isSel = photoTag in selectedPhotos
                        Surface(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isSel) selectedPhotos.remove(photoTag)
                                    else selectedPhotos.add(photoTag)
                                }
                                .border(BorderStroke(2.dp, if (isSel) DeepBurgundy else Color.Transparent), RoundedCornerShape(10.dp)),
                            color = Color.Black.copy(alpha = 0.05f)
                        ) {
                            CoilPhotoThumbnail(photoUri = photoTag, onClick = {})
                        }
                    }
                }

                // Schedule Reminder Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Schedule Push Notification Alarm",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = DeepBurgundy
                    )
                    Switch(
                        checked = scheduleReminder,
                        onCheckedChange = { scheduleReminder = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepBurgundy, checkedTrackColor = BlushPink)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = com.example.ui.theme.rememberHapticOnClick {
                    val weight = weightInput.toFloatOrNull()
                    onAdd(dateMillis, selectedMood, notes, weight, selectedPhotos.joinToString(","), selectedCategory, scheduleReminder)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy, contentColor = Cream)
            ) {
                Text(stringResource(R.string.diary_save_btn), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = com.example.ui.theme.rememberHapticOnClick { onDismiss() }) {
                Text(stringResource(R.string.cancel_btn), fontFamily = QuicksandFontFamily, color = Wine)
            }
        }
    )
}

@Composable
fun DiaryEntryShimmerItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Box(Modifier.width(80.dp).height(20.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                Box(Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            }
            Box(Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Box(Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
    }
}
