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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.Context
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCatScreen(
    viewModel: TinyPawsViewModel,
    mode: String = "profile",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catProfileState by viewModel.catProfile.collectAsStateWithLifecycle()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val isDarkMode = LocalIsDarkMode.current
    var isEditing by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Form states
    var nameInput by remember { mutableStateOf("") }
    var ageYearsInput by remember { mutableStateOf("") }
    var ageMonthsInput by remember { mutableStateOf("") }
    var selectedCoatColor by remember { mutableStateOf("orange") }
    var personalityInput by remember { mutableStateOf("") }
    var adoptionPhotoUrlInput by remember { mutableStateOf("") }
    var foodPreferencesInput by remember { mutableStateOf("") }
    var chronicConditionsInput by remember { mutableStateOf("") }

    // Profile photo media states
    var photoUrlInput by remember { mutableStateOf<String?>(null) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<java.io.File?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            val savedFile = saveImageToInternalStorage(context, uri)
            if (savedFile != null) {
                photoUrlInput = savedFile.absolutePath
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val file = tempPhotoFile
            if (file != null && file.exists()) {
                photoUrlInput = file.absolutePath
            }
        }
    }

    // History Log states
    val historyEntries by viewModel.allHistoryEntries.collectAsStateWithLifecycle()
    val sortedHistoryEntries = remember(historyEntries) { historyEntries.sortedByDescending { it.date } }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var editingHistoryEntry by remember { mutableStateOf<com.example.data.CatHistoryEntry?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogCategory by remember { mutableStateOf("Vet") }
    var dialogDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var dialogNotes by remember { mutableStateOf("") }

    // Synchronize form states when editing starts or profile changes
    LaunchedEffect(catProfileState, isEditing) {
        catProfileState?.let {
            if (nameInput.isEmpty() && ageYearsInput.isEmpty() && ageMonthsInput.isEmpty()) {
                nameInput = it.name
                ageYearsInput = it.ageYears.toString()
                ageMonthsInput = it.ageMonths.toString()
                selectedCoatColor = it.coatColor
                personalityInput = it.personality
                photoUrlInput = it.photoUrl
                adoptionPhotoUrlInput = it.adoptionPhotoUrl ?: ""
                foodPreferencesInput = it.foodPreferences
                chronicConditionsInput = it.chronicConditions ?: ""
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
                        text = if (mode == "profile") stringResource(R.string.my_cat_profile_title) else stringResource(R.string.my_cat_growth_title),
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
                            contentDescription = stringResource(R.string.my_cat_back_alt),
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

            if (mode == "profile") {
                if (!hasProfile || isEditing) {
                // Edit/Create Profile Mode
                Text(
                    text = if (hasProfile) stringResource(R.string.my_cat_edit_profile) else stringResource(R.string.my_cat_create_profile),
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
                        // Profile Photo Selector
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(BlushPink.copy(alpha = 0.4f))
                                    .border(BorderStroke(2.dp, DeepBurgundy), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!photoUrlInput.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = photoUrlInput,
                                        contentDescription = stringResource(R.string.my_cat_preview_photo_alt),
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    val emoji = when (selectedCoatColor) {
                                        "orange" -> "🍊🐱"
                                        "gray" -> "🩶🐱"
                                        "white" -> "🤍🐱"
                                        "black" -> "🖤🐱"
                                        "orange & white" -> "🍊🤍🐱"
                                        "black & white" -> "🖤🤍🐱"
                                        "tabby" -> "🐯🐱"
                                        else -> "🐱"
                                    }
                                    Text(text = emoji, fontSize = 44.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = com.example.ui.theme.rememberHapticOnClick {
                                        galleryLauncher.launch("image/*")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Mauve.copy(alpha = 0.3f)),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("gallery_picker_btn")
                                ) {
                                    Text(stringResource(R.string.hub_choose_gallery), fontFamily = QuicksandFontFamily, fontSize = 12.sp, color = DeepBurgundy)
                                }

                                Button(
                                    onClick = com.example.ui.theme.rememberHapticOnClick {
                                        try {
                                            val file = java.io.File(context.filesDir, "cat_profile_${System.currentTimeMillis()}.jpg")
                                            tempPhotoFile = file
                                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            tempPhotoUri = uri
                                            cameraLauncher.launch(uri)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MyCatScreen", "Failed to launch camera", e)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Mauve.copy(alpha = 0.3f)),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("camera_picker_btn")
                                ) {
                                    Text(stringResource(R.string.hub_take_photo), fontFamily = QuicksandFontFamily, fontSize = 12.sp, color = DeepBurgundy)
                                }

                                 if (!photoUrlInput.isNullOrBlank()) {
                                    IconButton(
                                        onClick = com.example.ui.theme.rememberHapticOnClick {
                                            photoUrlInput = null
                                        },
                                        modifier = Modifier.size(36.dp).background(Mauve.copy(alpha = 0.3f), CircleShape).testTag("remove_photo_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Photo",
                                            tint = Wine,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

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
                            text = stringResource(R.string.my_cat_coat_pattern_title),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepBurgundy
                        )

                        val coatColors = listOf(
                            "orange" to stringResource(R.string.coat_orange),
                            "gray" to stringResource(R.string.coat_gray),
                            "white" to stringResource(R.string.coat_white),
                            "black" to stringResource(R.string.coat_black),
                            "orange & white" to stringResource(R.string.coat_orange_white),
                            "black & white" to stringResource(R.string.coat_black_white),
                            "tabby" to stringResource(R.string.coat_tabby),
                            "caliby" to stringResource(R.string.coat_caliby),
                            "calico" to stringResource(R.string.coat_calico),
                            "tortoiseshell" to stringResource(R.string.coat_tortoiseshell),
                            "siamese" to stringResource(R.string.coat_siamese),
                            "persian" to stringResource(R.string.coat_persian),
                            "maine_coon" to stringResource(R.string.coat_maine_coon),
                            "ragdoll" to stringResource(R.string.coat_ragdoll),
                            "bengal" to stringResource(R.string.coat_bengal),
                            "mixed_breed" to stringResource(R.string.coat_mixed),
                            "other" to stringResource(R.string.coat_other)
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

                        // Personality Input
                        OutlinedTextField(
                            value = personalityInput,
                            onValueChange = { personalityInput = it },
                            label = { Text(stringResource(R.string.cat_personality_label), fontFamily = QuicksandFontFamily) },
                            placeholder = { Text(stringResource(R.string.my_cat_personality_hint), fontFamily = QuicksandFontFamily) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve,
                                focusedLabelColor = DeepBurgundy,
                                unfocusedLabelColor = Wine
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("cat_personality_input")
                        )

                        // Adoption/Kitten Photo URL Input
                        OutlinedTextField(
                            value = adoptionPhotoUrlInput,
                            onValueChange = { adoptionPhotoUrlInput = it },
                            label = { Text(stringResource(R.string.cat_adoption_photo_label), fontFamily = QuicksandFontFamily) },
                            placeholder = { Text(stringResource(R.string.my_cat_adoption_photo_hint), fontFamily = QuicksandFontFamily) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve,
                                focusedLabelColor = DeepBurgundy,
                                unfocusedLabelColor = Wine
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("cat_adoption_photo_input")
                        )

                        // Food Preferences Input
                        OutlinedTextField(
                            value = foodPreferencesInput,
                            onValueChange = { foodPreferencesInput = it },
                            label = { Text(stringResource(R.string.cat_food_pref_label), fontFamily = QuicksandFontFamily) },
                            placeholder = { Text(stringResource(R.string.my_cat_food_pref_hint), fontFamily = QuicksandFontFamily) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve,
                                focusedLabelColor = DeepBurgundy,
                                unfocusedLabelColor = Wine
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("cat_food_pref_input")
                        )

                        // Chronic Conditions Input
                        OutlinedTextField(
                            value = chronicConditionsInput,
                            onValueChange = { chronicConditionsInput = it },
                            label = { Text(stringResource(R.string.cat_chronic_cond_label), fontFamily = QuicksandFontFamily) },
                            placeholder = { Text(stringResource(R.string.my_cat_chronic_cond_hint), fontFamily = QuicksandFontFamily) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = Mauve,
                                focusedLabelColor = DeepBurgundy,
                                unfocusedLabelColor = Wine
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("cat_chronic_cond_input")
                        )

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
                                        viewModel.saveCatProfile(
                                            name = nameInput,
                                            ageYears = ageYears,
                                            ageMonths = ageMonths,
                                            coatColor = selectedCoatColor,
                                            photoUrl = photoUrlInput,
                                            personality = personalityInput,
                                            adoptionPhotoUrl = adoptionPhotoUrlInput.ifBlank { null },
                                            foodPreferences = foodPreferencesInput,
                                            chronicConditions = chronicConditionsInput.ifBlank { null }
                                        )
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

                // Display photos row (Main + Adoption photo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Main Avatar/Photo
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(BlushPink.copy(alpha = 0.4f))
                                .border(BorderStroke(2.dp, DeepBurgundy), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile.photoUrl.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = stringResource(R.string.my_cat_main_photo_alt),
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
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
                                Text(text = emoji, fontSize = 44.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.my_cat_current_photo_label), fontSize = 12.sp, fontFamily = QuicksandFontFamily, color = Wine, fontWeight = FontWeight.Bold)
                    }

                    if (!profile.adoptionPhotoUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(BlushPink.copy(alpha = 0.4f))
                                    .border(BorderStroke(2.dp, Mauve), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = profile.adoptionPhotoUrl,
                                    contentDescription = stringResource(R.string.my_cat_adoption_photo_alt),
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.my_cat_adoption_photo_label), fontSize = 12.sp, fontFamily = QuicksandFontFamily, color = Wine, fontWeight = FontWeight.Bold)
                        }
                    }
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
                                    text = stringResource(R.string.my_cat_age_label),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                            }
                            Text(
                                text = stringResource(R.string.my_cat_age_value, profile.ageYears, profile.ageMonths),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

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
                                    text = stringResource(R.string.my_cat_coat_pattern_label),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                            }
                            Text(
                                text = when (profile.coatColor.lowercase().trim()) {
                                    "orange" -> stringResource(R.string.coat_orange)
                                    "gray" -> stringResource(R.string.coat_gray)
                                    "white" -> stringResource(R.string.coat_white)
                                    "black" -> stringResource(R.string.coat_black)
                                    "orange & white" -> stringResource(R.string.coat_orange_white)
                                    "black & white" -> stringResource(R.string.coat_black_white)
                                    "tabby" -> stringResource(R.string.coat_tabby)
                                    "caliby", "calico & tabby", "calico + tabby" -> stringResource(R.string.coat_caliby)
                                    "calico" -> stringResource(R.string.coat_calico)
                                    "tortoiseshell", "tortie" -> stringResource(R.string.coat_tortoiseshell)
                                    "siamese" -> stringResource(R.string.coat_siamese)
                                    "persian" -> stringResource(R.string.coat_persian)
                                    "maine_coon", "maine coon" -> stringResource(R.string.coat_maine_coon)
                                    "ragdoll" -> stringResource(R.string.coat_ragdoll)
                                    "bengal" -> stringResource(R.string.coat_bengal)
                                    "mixed_breed", "mixed" -> stringResource(R.string.coat_mixed)
                                    "other", "unknown" -> stringResource(R.string.coat_other)
                                    else -> profile.coatColor.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                },
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Ink
                            )
                        }

                        if (profile.personality.isNotBlank()) {
                            HorizontalDivider(color = Mauve.copy(alpha = 0.3f))
                            Column {
                                Text(
                                    text = stringResource(R.string.my_cat_personality_title),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepBurgundy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.personality,
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                            }
                        }

                        if (profile.foodPreferences.isNotBlank()) {
                            HorizontalDivider(color = Mauve.copy(alpha = 0.3f))
                            Column {
                                Text(
                                    text = stringResource(R.string.my_cat_food_pref_title),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepBurgundy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.foodPreferences,
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                            }
                        }

                        if (!profile.chronicConditions.isNullOrBlank()) {
                            HorizontalDivider(color = Mauve.copy(alpha = 0.3f))
                            Column {
                                Text(
                                    text = stringResource(R.string.my_cat_chronic_cond_title),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepBurgundy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.chronicConditions,
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                nameInput = profile.name
                                ageYearsInput = profile.ageYears.toString()
                                ageMonthsInput = profile.ageMonths.toString()
                                selectedCoatColor = profile.coatColor
                                personalityInput = profile.personality
                                adoptionPhotoUrlInput = profile.adoptionPhotoUrl ?: ""
                                foodPreferencesInput = profile.foodPreferences
                                chronicConditionsInput = profile.chronicConditions ?: ""
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

                Spacer(modifier = Modifier.height(20.dp))

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.hub_history_log_title),
                                    fontFamily = FrauncesFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DeepBurgundy
                                )
                                Text(
                                    text = stringResource(R.string.hub_history_log_desc),
                                    fontFamily = QuicksandFontFamily,
                                    fontSize = 12.sp,
                                    color = Wine
                                )
                            }

                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                    editingHistoryEntry = null
                                    dialogTitle = ""
                                    dialogCategory = "Vet"
                                    dialogDateMillis = System.currentTimeMillis()
                                    dialogNotes = ""
                                    showHistoryDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_history_entry_btn")
                            ) {
                                Text(stringResource(R.string.hub_history_add_entry), fontFamily = QuicksandFontFamily, fontSize = 12.sp, color = Cream, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = Mauve.copy(alpha = 0.3f))

                        if (sortedHistoryEntries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.hub_history_empty),
                                    fontFamily = QuicksandFontFamily,
                                    color = Wine.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                sortedHistoryEntries.forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val categoryIcon = when (entry.category) {
                                            "Vet" -> "🩺"
                                            "Vaccination" -> "💉"
                                            "Medication" -> "💊"
                                            "Milestone" -> "🎉"
                                            else -> "🐾"
                                        }

                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(text = categoryIcon, fontSize = 24.sp)

                                            Column {
                                                Text(
                                                    text = entry.title,
                                                    fontFamily = QuicksandFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = Ink
                                                )
                                                Text(
                                                    text = dateFormatter.format(Date(entry.date)),
                                                    fontFamily = QuicksandFontFamily,
                                                    fontSize = 12.sp,
                                                    color = Wine,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (entry.notes.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = entry.notes,
                                                        fontFamily = QuicksandFontFamily,
                                                        fontSize = 13.sp,
                                                        color = Ink.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                                    editingHistoryEntry = entry
                                                    dialogTitle = entry.title
                                                    dialogCategory = entry.category
                                                    dialogDateMillis = entry.date
                                                    dialogNotes = entry.notes
                                                    showHistoryDialog = true
                                                },
                                                modifier = Modifier.size(28.dp).testTag("edit_history_btn_${entry.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = DeepBurgundy.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                                    viewModel.deleteHistoryEntry(entry.id)
                                                },
                                                modifier = Modifier.size(28.dp).testTag("delete_history_btn_${entry.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = RedError.copy(alpha = 0.7f),
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
        if (mode == "weight") {
                if (!hasProfile) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .border(BorderStroke(1.dp, Mauve.copy(alpha = 0.3f)), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("🐱", fontSize = 48.sp)
                            Text(
                                text = "Create Cat Profile First",
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DeepBurgundy
                            )
                            Text(
                                text = "To log growth, weights, and view development trends, please set up your cat's profile first.",
                                fontFamily = QuicksandFontFamily,
                                fontSize = 14.sp,
                                color = Ink.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
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
                var editingWeightLogId by remember { mutableStateOf<Int?>(null) }
                var showAllWeights by remember { mutableStateOf(false) }

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = com.example.ui.theme.rememberHapticOnClick { 
                                    val weightVal = weightInput.toFloatOrNull()
                                    if (weightVal != null && weightVal > 0f) {
                                        viewModel.saveWeightLog(weightDateMillis, weightVal, editingWeightLogId ?: 0)
                                        weightInput = ""
                                        editingWeightLogId = null
                                        focusManager.clearFocus()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_weight_btn"),
                                enabled = weightInput.toFloatOrNull() != null
                            ) {
                                Text(
                                    text = if (editingWeightLogId != null) stringResource(R.string.my_cat_update_weight_btn) else stringResource(R.string.log_weight_btn),
                                    fontFamily = QuicksandFontFamily,
                                    color = Cream,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (editingWeightLogId != null) {
                                OutlinedButton(
                                    onClick = com.example.ui.theme.rememberHapticOnClick { 
                                        weightInput = ""
                                        editingWeightLogId = null
                                        focusManager.clearFocus()
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy),
                                    border = BorderStroke(1.dp, Mauve),
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .height(48.dp)
                                        .testTag("cancel_edit_weight_btn")
                                ) {
                                    Text(stringResource(R.string.my_cat_cancel_btn), fontFamily = QuicksandFontFamily, fontSize = 13.sp)
                                }
                            }
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
                                text = stringResource(R.string.my_cat_weigh_in_history),
                                fontFamily = FrauncesFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Wine,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            val displayWeights = if (showAllWeights) sortedWeightLogsDesc else sortedWeightLogsDesc.take(5)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                displayWeights.forEach { log ->
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
                                                onClick = com.example.ui.theme.rememberHapticOnClick {
                                                    editingWeightLogId = log.id
                                                    weightInput = log.weight.toString()
                                                    weightDateMillis = log.date
                                                },
                                                modifier = Modifier.size(24.dp).testTag("edit_weight_btn_${log.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = stringResource(R.string.my_cat_edit_desc),
                                                    tint = DeepBurgundy.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.deleteWeightLog(log.id) },
                                                modifier = Modifier.size(24.dp).testTag("delete_weight_btn_${log.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.my_cat_delete_desc),
                                                    tint = RedError.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (sortedWeightLogsDesc.size > 5) {
                                    TextButton(
                                        onClick = { showAllWeights = !showAllWeights },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(
                                            text = if (showAllWeights) stringResource(R.string.my_cat_show_less) else stringResource(R.string.my_cat_see_all, sortedWeightLogsDesc.size),
                                            fontFamily = QuicksandFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepBurgundy
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

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Text(
                    text = if (editingHistoryEntry != null) stringResource(R.string.hub_history_edit_entry) else stringResource(R.string.hub_history_add_entry),
                    fontFamily = FrauncesFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepBurgundy
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title Input
                    OutlinedTextField(
                        value = dialogTitle,
                        onValueChange = { dialogTitle = it },
                        label = { Text(stringResource(R.string.hub_history_title_label), fontFamily = QuicksandFontFamily) },
                        placeholder = { Text(stringResource(R.string.my_cat_history_title_hint), fontFamily = QuicksandFontFamily) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBurgundy,
                            unfocusedBorderColor = Mauve,
                            focusedLabelColor = DeepBurgundy,
                            unfocusedLabelColor = Wine
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("history_title_input")
                    )

                    // Category Picker
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.hub_history_category),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBurgundy
                        )

                        val categories = listOf(
                            "Vet" to R.string.hub_history_cat_vet,
                            "Vaccination" to R.string.hub_history_cat_vac,
                            "Medication" to R.string.hub_history_cat_med,
                            "Milestone" to R.string.hub_history_cat_milestone,
                            "Other" to R.string.hub_history_cat_other
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            categories.forEach { (catValue, catResId) ->
                                val isSelected = dialogCategory == catValue
                                val bg = if (isSelected) DeepBurgundy else BlushPink.copy(alpha = 0.2f)
                                val tc = if (isSelected) Cream else Ink

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg)
                                        .border(BorderStroke(1.dp, if (isSelected) DeepBurgundy else Mauve.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                        .clickable { dialogCategory = catValue }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("history_category_$catValue"),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = stringResource(catResId),
                                        fontFamily = QuicksandFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = tc,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Date Picker Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.hub_history_date),
                            fontFamily = FrauncesFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DeepBurgundy
                        )

                        OutlinedButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = dialogDateMillis
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val cal = Calendar.getInstance()
                                        cal.set(year, month, dayOfMonth)
                                        dialogDateMillis = cal.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy),
                            border = BorderStroke(1.dp, Mauve),
                            modifier = Modifier.testTag("history_date_picker_btn")
                        ) {
                            Text(
                                text = dateFormatter.format(Date(dialogDateMillis)),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Notes Input
                    OutlinedTextField(
                        value = dialogNotes,
                        onValueChange = { dialogNotes = it },
                        label = { Text(stringResource(R.string.hub_history_notes_label), fontFamily = QuicksandFontFamily) },
                        placeholder = { Text("e.g. Weight: 4.2kg, next due in 1 year", fontFamily = QuicksandFontFamily) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepBurgundy,
                            unfocusedBorderColor = Mauve,
                            focusedLabelColor = DeepBurgundy,
                            unfocusedLabelColor = Wine
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("history_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogTitle.isNotBlank()) {
                            val entry = com.example.data.CatHistoryEntry(
                                id = editingHistoryEntry?.id ?: 0,
                                title = dialogTitle,
                                date = dialogDateMillis,
                                category = dialogCategory,
                                notes = dialogNotes
                            )
                            viewModel.saveHistoryEntry(entry)
                            showHistoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                    shape = CircleShape,
                    enabled = dialogTitle.isNotBlank(),
                    modifier = Modifier.testTag("history_dialog_save_btn")
                ) {
                    Text(stringResource(R.string.hub_history_save), fontFamily = QuicksandFontFamily, color = Cream, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showHistoryDialog = false },
                    modifier = Modifier.testTag("history_dialog_cancel_btn")
                ) {
                    Text(stringResource(R.string.hub_cancel_btn), fontFamily = QuicksandFontFamily, color = DeepBurgundy, fontWeight = FontWeight.Bold)
                }
            }
        )
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

fun saveImageToInternalStorage(context: android.content.Context, uri: android.net.Uri): java.io.File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = java.io.File(context.filesDir, "cat_profile_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        android.util.Log.e("MyCatScreen", "Failed to save image to internal storage", e)
        null
    }
}
