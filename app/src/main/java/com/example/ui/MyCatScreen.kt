package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CatProfile
import com.example.data.CatWeightLog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCatScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catProfileState by viewModel.catProfile.collectAsStateWithLifecycle()
    val isDarkMode = LocalIsDarkMode.current
    var isEditing by remember { mutableStateOf(false) }

    // Form states
    var nameInput by remember { mutableStateOf("") }
    var ageYearsInput by remember { mutableStateOf("") }
    var ageMonthsInput by remember { mutableStateOf("") }
    var selectedCoatColor by remember { mutableStateOf("orange") }

    // Synchronize form states when editing starts or profile changes
    LaunchedEffect(catProfileState, isEditing) {
        catProfileState?.let {
            if (nameInput.isEmpty() && ageYearsInput.isEmpty() && ageMonthsInput.isEmpty()) {
                nameInput = it.name
                ageYearsInput = it.ageYears.toString()
                ageMonthsInput = it.ageMonths.toString()
                selectedCoatColor = it.coatColor
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Cat Profile",
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepBurgundy
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = com.example.ui.theme.rememberHapticOnClick { onBack() },
                        modifier = Modifier.testTag("my_cat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepBurgundy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val hasProfile = catProfileState != null

            if (!hasProfile || isEditing) {
                // Edit/Create Profile Mode
                Text(
                    text = if (hasProfile) "Edit Profile" else "Create Cat Profile",
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = DeepBurgundy,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassyCard(shape = RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Cat Name Input
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(stringResource(R.string.hub_cat_name_label), fontFamily = QuicksandFontFamily) },
                            placeholder = { Text(stringResource(R.string.hub_cat_name_placeholder), fontFamily = QuicksandFontFamily) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve,
                                focusedLabelColor = DeepBurgundy,
                                unfocusedLabelColor = Wine
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("cat_name_input")
                        )

                        // Cat Age Input
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ageYearsInput,
                                onValueChange = { ageYearsInput = it },
                                label = { Text(stringResource(R.string.hub_years_label), fontFamily = QuicksandFontFamily) },
                                placeholder = { Text(stringResource(R.string.hub_years_placeholder), fontFamily = QuicksandFontFamily) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepBurgundy,
                                    unfocusedBorderColor = Mauve,
                                    focusedLabelColor = DeepBurgundy,
                                    unfocusedLabelColor = Wine
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f).testTag("cat_age_years_input")
                            )
                            OutlinedTextField(
                                value = ageMonthsInput,
                                onValueChange = { ageMonthsInput = it },
                                label = { Text(stringResource(R.string.hub_months_label), fontFamily = QuicksandFontFamily) },
                                placeholder = { Text(stringResource(R.string.hub_months_placeholder), fontFamily = QuicksandFontFamily) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepBurgundy,
                                    unfocusedBorderColor = Mauve,
                                    focusedLabelColor = DeepBurgundy,
                                    unfocusedLabelColor = Wine
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.weight(1f).testTag("cat_age_months_input")
                            )
                        }

                        // Coat pattern/color picker
                        Text(
                            text = "Coat Pattern & Color",
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepBurgundy
                        )

                        val coatColors = listOf(
                            "orange" to "Orange 🍊",
                            "gray" to "Gray 🩶",
                            "white" to "White 🤍",
                            "black" to "Black 🖤",
                            "orange & white" to "Orange & White 🍊🤍",
                            "black & white" to "Black & White 🖤🤍",
                            "tabby" to "Tabby 🐯"
                        )

                        // Beautiful Grid or Row layout for selection
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            coatColors.chunked(2).forEach { rowColors ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowColors.forEach { (colorValue, colorLabel) ->
                                        val isSelected = selectedCoatColor == colorValue
                                        val chipBg = if (isSelected) DeepBurgundy else BlushPink.copy(alpha = 0.25f)
                                        val chipText = if (isSelected) Cream else Ink

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(chipBg)
                                                .border(
                                                    BorderStroke(
                                                        1.dp,
                                                        if (isSelected) DeepBurgundy else Mauve.copy(alpha = 0.5f)
                                                    ),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    selectedCoatColor = colorValue
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                                .testTag("coat_color_$colorValue"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = colorLabel,
                                                    fontFamily = QuicksandFontFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 14.sp,
                                                    color = chipText
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Cream,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Empty spacer if odd number of items
                                    if (rowColors.size < 2) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (hasProfile) {
                                Button(
                                    onClick = com.example.ui.theme.rememberHapticOnClick {  isEditing = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, Mauve),
                                    shape = CircleShape,
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(stringResource(R.string.hub_cancel_btn), fontFamily = QuicksandFontFamily, color = DeepBurgundy)
                                }
                            }

                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick { 
                                    val ageYears = ageYearsInput.toIntOrNull() ?: 0
                                    val ageMonths = ageMonthsInput.toIntOrNull() ?: 0
                                    if (nameInput.isNotBlank()) {
                                        viewModel.saveCatProfile(nameInput, ageYears, ageMonths, selectedCoatColor)
                                        isEditing = false
                                        focusManager.clearFocus()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_profile_btn"),
                                enabled = nameInput.isNotBlank()
                            ) {
                                Text(stringResource(R.string.hub_save_profile_btn), fontFamily = QuicksandFontFamily, color = Cream, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Profile Display Mode
                val profile = catProfileState!!

                // Display avatar based on selection
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(BlushPink.copy(alpha = 0.4f))
                        .border(BorderStroke(2.dp, DeepBurgundy), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = when (profile.coatColor) {
                        "orange" -> "🍊🐱"
                        "gray" -> "🩶🐱"
                        "white" -> "🤍🐱"
                        "black" -> "🖤🐱"
                        "orange & white" -> "🍊🤍🐱"
                        "black & white" -> "🖤🤍🐱"
                        "tabby" -> "🐯🐱"
                        else -> "🐱"
                    }
                    Text(text = emoji, fontSize = 48.sp)
                }

                Text(
                    text = profile.name,
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = DeepBurgundy,
                    textAlign = TextAlign.Center
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassyCard(shape = RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = Wine,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Age:",
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                            }
                            Text(
                                text = "${profile.ageYears} years, ${profile.ageMonths} months",
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        Divider(color = Mauve.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = Wine,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Coat Pattern:",
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                            }
                            Text(
                                text = profile.coatColor.replaceFirstChar { it.uppercase() },
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                nameInput = profile.name
                                ageYearsInput = profile.ageYears.toString()
                                ageMonthsInput = profile.ageMonths.toString()
                                selectedCoatColor = profile.coatColor
                                isEditing = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Wine),
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("edit_profile_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Cream, modifier = Modifier.size(18.dp))
                                Text(stringResource(R.string.hub_edit_profile_btn), fontFamily = QuicksandFontFamily, color = Cream, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // FEATURE 2: WEIGHT TRACKER SECTION
                Text(
                    text = stringResource(R.string.weight_tracker_title),
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = DeepBurgundy,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textAlign = TextAlign.Start
                )

                // Weights state from ViewModel
                val weightLogs by viewModel.allWeightLogs.collectAsStateWithLifecycle()
                val sortedWeightLogsDesc = remember(weightLogs) { weightLogs.sortedByDescending { it.date } }

                // Local states for logging weight
                val context = LocalContext.current
                var weightInput by remember { mutableStateOf("") }
                var weightDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
                val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassyCard(shape = RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.log_weight_title),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DeepBurgundy
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date Picker Trigger Button
                            OutlinedButton(
                                onClick = com.example.ui.theme.rememberHapticOnClick { 
                                    val calendar = Calendar.getInstance()
                                    calendar.timeInMillis = weightDateMillis
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val cal = Calendar.getInstance()
                                            cal.set(year, month, dayOfMonth)
                                            weightDateMillis = cal.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy),
                                border = BorderStroke(1.dp, Mauve),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(56.dp)
                                    .testTag("weight_date_picker_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = dateFormatter.format(Date(weightDateMillis)),
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Weight Text Input
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text(stringResource(R.string.weight_input_label), fontFamily = QuicksandFontFamily, fontSize = 12.sp) },
                                placeholder = { Text(stringResource(R.string.hub_weight_placeholder), fontFamily = QuicksandFontFamily) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DeepBurgundy,
                                    unfocusedBorderColor = Mauve,
                                    focusedLabelColor = DeepBurgundy,
                                    unfocusedLabelColor = Wine
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("weight_input_field")
                            )
                        }

                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                val weightVal = weightInput.toFloatOrNull()
                                if (weightVal != null && weightVal > 0f) {
                                    viewModel.saveWeightLog(weightDateMillis, weightVal)
                                    weightInput = ""
                                    focusManager.clearFocus()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_weight_btn"),
                            enabled = weightInput.toFloatOrNull() != null
                        ) {
                            Text(stringResource(R.string.log_weight_btn), fontFamily = QuicksandFontFamily, color = Cream, fontWeight = FontWeight.Bold)
                        }

                        // WEIGHT DIFFERENCE BOX
                        val weightDiffText = if (sortedWeightLogsDesc.size >= 2) {
                            val latest = sortedWeightLogsDesc[0].weight
                            val previous = sortedWeightLogsDesc[1].weight
                            val diff = latest - previous
                            val formattedDiff = String.format(Locale.US, "%.2f", java.lang.Math.abs(diff))
                            if (diff > 0f) {
                                stringResource(R.string.weight_gained, formattedDiff)
                            } else if (diff < 0f) {
                                stringResource(R.string.weight_lost, formattedDiff)
                            } else {
                                stringResource(R.string.weight_no_change)
                            }
                        } else {
                            stringResource(R.string.weight_tracker_info)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(BlushPink.copy(alpha = 0.2f))
                                .border(BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = weightDiffText,
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DeepBurgundy,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().testTag("weight_difference_text")
                            )
                        }

                        // NATIVE COMPOSE CHART
                        WeightHistoryChart(logs = weightLogs, modifier = Modifier.fillMaxWidth())

                        // Weight Log History List
                        if (sortedWeightLogsDesc.isNotEmpty()) {
                            Text(
                                text = "Weigh-in History",
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Wine,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                sortedWeightLogsDesc.take(5).forEach { log ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = dateFormatter.format(Date(log.date)),
                                                fontFamily = QuicksandFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Ink
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${log.weight} kg",
                                                fontFamily = QuicksandFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = DeepBurgundy
                                            )

                                            IconButton(
                                                onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.deleteWeightLog(log.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = RedError.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FEATURE 3: DAILY CHECK-IN HISTORY SECTION
                Text(
                    text = "Daily Check-in History",
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = DeepBurgundy,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textAlign = TextAlign.Start
                )

                val checkInLogs by viewModel.allCheckInLogs.collectAsStateWithLifecycle()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassyCard(shape = RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (checkInLogs.isEmpty()) {
                            Text(
                                text = "No daily check-ins logged yet. You can log them from the home dashboard!",
                                fontFamily = QuicksandFontFamily,
                                color = Wine.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            )
                        } else {
                            checkInLogs.take(10).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val moodText = when (log.mood) {
                                                "happy" -> "😊 Happy"
                                                "tired" -> "😴 Tired"
                                                "unwell" -> "🤒 Unwell"
                                                else -> log.mood
                                            }
                                            Text(
                                                text = moodText,
                                                fontFamily = QuicksandFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = DeepBurgundy
                                            )
                                            Text(
                                                text = "• " + dateFormatter.format(Date(log.date)),
                                                fontFamily = QuicksandFontFamily,
                                                fontSize = 12.sp,
                                                color = Ink.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (log.notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = log.notes,
                                                fontFamily = QuicksandFontFamily,
                                                fontSize = 13.sp,
                                                color = Ink
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.deleteCheckInLog(log.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = RedError.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
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
}

@Composable
fun WeightHistoryChart(
    logs: List<CatWeightLog>,
    modifier: Modifier = Modifier
) {

    if (logs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No weight data points recorded yet.",
                fontFamily = QuicksandFontFamily,
                color = Wine.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val lineColor = DeepBurgundy
    val gridColor = Mauve.copy(alpha = 0.15f)
    val fillGradientColors = listOf(BlushPink.copy(alpha = 0.4f), Color.Transparent)
    val dotCenterColor = Cream

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Mauve.copy(alpha = 0.2f)), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Weight Trend (kg)",
                fontFamily = FrauncesFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Wine,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val width = size.width
                val height = size.height

                val sortedLogs = logs.sortedBy { it.date }
                val minDate = sortedLogs.first().date
                val maxDate = sortedLogs.last().date
                val minWeight = sortedLogs.minOf { it.weight }
                val maxWeight = sortedLogs.maxOf { it.weight }

                val paddingX = 40f
                val paddingY = 20f

                val chartWidth = width - 2 * paddingX
                val chartHeight = height - 2 * paddingY

                // Draw horizontal grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = paddingY + chartHeight * i / gridLines
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(paddingX, y),
                        end = androidx.compose.ui.geometry.Offset(width - paddingX, y),
                        strokeWidth = 1f
                    )
                }

                // Generate points
                val points = sortedLogs.map { log ->
                    val x = if (maxDate == minDate) {
                        paddingX + chartWidth / 2f
                    } else {
                        paddingX + chartWidth * (log.date - minDate).toFloat() / (maxDate - minDate).toFloat()
                    }

                    val y = if (maxWeight == minWeight) {
                        paddingY + chartHeight / 2f
                    } else {
                        paddingY + chartHeight - chartHeight * (log.weight - minWeight) / (maxWeight - minWeight)
                    }
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                // Draw gradient area under curve
                if (points.size > 1) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(points.first().x, height - paddingY)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height - paddingY)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = fillGradientColors
                        )
                    )
                }

                // Draw the line connecting points
                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.5.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                } else if (points.size == 1) {
                    // Draw horizontal line for single point
                    drawLine(
                        color = lineColor,
                        start = androidx.compose.ui.geometry.Offset(paddingX, points[0].y),
                        end = androidx.compose.ui.geometry.Offset(width - paddingX, points[0].y),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                // Draw data points
                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = lineColor,
                        radius = 4.5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = dotCenterColor,
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }
            }

            // Draw simple axis labels below chart
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (logs.isNotEmpty()) {
                    val sorted = logs.sortedBy { it.date }
                    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
                    Text(
                        text = sdf.format(Date(sorted.first().date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Wine,
                        fontSize = 11.sp
                    )
                    if (sorted.size > 1) {
                        Text(
                            text = sdf.format(Date(sorted.last().date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Wine,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
