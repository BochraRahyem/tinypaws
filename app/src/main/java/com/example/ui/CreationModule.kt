package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import java.io.File

@Composable
fun CreationModuleScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeCreationTab by remember { mutableStateOf("art") } // "art", "music"

    // Art generation states
    val defaultArtPrompt = stringResource(R.string.creation_art_default_prompt)
    var artPrompt by remember { mutableStateOf(defaultArtPrompt) }
    var selectProModel by remember { mutableStateOf(true) } // true: gemini-3-pro-image-preview, false: gemini-3.1-flash-image-preview
    var imageSize by remember { mutableStateOf("2K") } // "1K", "2K", "4K"

    val isGeneratingImage by viewModel.isGeneratingImage.collectAsStateWithLifecycle()
    val generatedBitmap by viewModel.generatedBitmap.collectAsStateWithLifecycle()

    // Music generation states
    val defaultMusicPrompt = stringResource(R.string.creation_music_default_prompt)
    var musicPrompt by remember { mutableStateOf(defaultMusicPrompt) }
    var useFullTrack by remember { mutableStateOf(false) } // false: lyria-3-clip-preview (30s), true: lyria-3-pro-preview

    val isGeneratingMusic by viewModel.isGeneratingMusic.collectAsStateWithLifecycle()
    val generatedMusicFile by viewModel.generatedMusicFile.collectAsStateWithLifecycle()

    // Media player state
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isMusicPlaying by remember { mutableStateOf(false) }

    // Clean up media player on leave
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
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
            Text(
                text = stringResource(R.string.creation_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PastelPurpleDark
                )
            )
        }

        // Sub Navigation Tabs
        TabRow(
            selectedTabIndex = if (activeCreationTab == "art") 0 else 1,
            containerColor = Color.Transparent,
            contentColor = PastelPurpleDark,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Tab(
                selected = activeCreationTab == "art",
                onClick = com.example.ui.theme.rememberHapticOnClick {  activeCreationTab = "art" },
                text = { Text(stringResource(R.string.creation_art_studio), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeCreationTab == "music",
                onClick = com.example.ui.theme.rememberHapticOnClick {  activeCreationTab = "music" },
                text = { Text(stringResource(R.string.creation_music_studio), fontWeight = FontWeight.Bold) }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeCreationTab == "art") {
                // ART STUDIO MODULE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, PastelPinkAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.creation_art_studio),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PastelPurpleDark
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.creation_art_desc),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
                        )
                    }
                }

                // Configuration Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, LightPurpleBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.creation_art_desc), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PastelPurpleDark)
                        OutlinedTextField(
                            value = artPrompt,
                            onValueChange = { artPrompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurplePrimary,
                                unfocusedBorderColor = SoftGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text(stringResource(R.string.creation_art_placeholder)) }
                        )

                        Text(stringResource(R.string.creation_model_label), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PastelPurpleDark)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModelSelectionButton(
                                title = stringResource(R.string.creation_model_pro_title),
                                subtitle = stringResource(R.string.creation_model_pro_sub),
                                active = selectProModel,
                                modifier = Modifier.weight(1f),
                                onClick = com.example.ui.theme.rememberHapticOnClick {  selectProModel = true }
                            )
                            ModelSelectionButton(
                                title = stringResource(R.string.creation_model_flash_title),
                                subtitle = stringResource(R.string.creation_model_flash_sub),
                                active = !selectProModel,
                                modifier = Modifier.weight(1f),
                                onClick = com.example.ui.theme.rememberHapticOnClick {  selectProModel = false }
                            )
                        }

                        Text(stringResource(R.string.creation_resolution_label), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PastelPurpleDark)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1K", "2K", "4K").forEach { size ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .background(
                                            color = if (imageSize == size) PastelPinkAccent else SoftGray,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { imageSize = size }
                                        .border(
                                            width = 1.dp,
                                            color = if (imageSize == size) PastelPinkDark else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = size, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.generateCatArt(artPrompt, selectProModel, imageSize) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_art_button"),
                            enabled = artPrompt.isNotEmpty() && !isGeneratingImage,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGeneratingImage) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(stringResource(R.string.creation_generate_btn), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Image Viewport
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, SoftGray)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isGeneratingImage) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = PastelPurplePrimary)
                                Text(stringResource(R.string.creation_generating), fontSize = 12.sp, color = TextMuted)
                            }
                        } else {
                            val bitmap = generatedBitmap
                            if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = stringResource(R.string.creation_art_alt),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🎨", fontSize = 48.sp)
                                        Text(stringResource(R.string.creation_art_placeholder), fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                }
                        }
                    }
                }
            } else {
                // MUSIC GENERATION MODULE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, PastelPinkAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.creation_music_studio),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PastelPurpleDark
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.creation_music_desc),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDark)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, LightPurpleBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.creation_music_desc), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PastelPurpleDark)
                        OutlinedTextField(
                            value = musicPrompt,
                            onValueChange = { musicPrompt = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurplePrimary,
                                unfocusedBorderColor = SoftGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text(stringResource(R.string.creation_music_placeholder)) }
                        )

                        Text(stringResource(R.string.creation_music_duration_label), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = PastelPurpleDark)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ModelSelectionButton(
                                title = stringResource(R.string.creation_music_short_title),
                                subtitle = stringResource(R.string.creation_music_short_sub),
                                active = !useFullTrack,
                                modifier = Modifier.weight(1f),
                                onClick = com.example.ui.theme.rememberHapticOnClick {  useFullTrack = false }
                            )
                            ModelSelectionButton(
                                title = stringResource(R.string.creation_music_full_title),
                                subtitle = stringResource(R.string.creation_music_full_sub),
                                active = useFullTrack,
                                modifier = Modifier.weight(1f),
                                onClick = com.example.ui.theme.rememberHapticOnClick {  useFullTrack = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = com.example.ui.theme.rememberHapticOnClick { 
                                viewModel.generateKittenMusic(context.applicationContext as Application, musicPrompt, useFullTrack)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_music_button"),
                            enabled = musicPrompt.isNotEmpty() && !isGeneratingMusic,
                            colors = ButtonDefaults.buttonColors(containerColor = PastelPurpleDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGeneratingMusic) {
                                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(stringResource(R.string.creation_generate_btn), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Playback Control Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, SoftGray)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(stringResource(R.string.creation_audio_control), fontWeight = FontWeight.Bold, color = PastelPurpleDark)

                        if (isGeneratingMusic) {
                            CircularProgressIndicator(color = PastelPurplePrimary)
                            Text(stringResource(R.string.creation_lyria_humming), fontSize = 11.sp, color = TextMuted)
                        } else {
                            val audioFile = generatedMusicFile
                            if (audioFile != null && audioFile.exists()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = com.example.ui.theme.rememberHapticOnClick { 
                                            try {
                                                val targetCtx = context
                                                val audioManager = targetCtx.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                                                
                                                if (isMusicPlaying) {
                                                    mediaPlayer?.pause()
                                                    isMusicPlaying = false
                                                    @Suppress("DEPRECATION")
                                                    try { audioManager.abandonAudioFocus { } } catch (e: Exception) { android.util.Log.e("CreationModule", "Failed to abandon audio focus", e) }
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    try {
                                                        audioManager.requestAudioFocus(
                                                            { focusChange ->
                                                                if (focusChange == android.media.AudioManager.AUDIOFOCUS_LOSS) {
                                                                    mediaPlayer?.pause()
                                                                    isMusicPlaying = false
                                                                }
                                                            },
                                                            android.media.AudioManager.STREAM_MUSIC,
                                                            android.media.AudioManager.AUDIOFOCUS_GAIN
                                                        )
                                                    } catch (e: Exception) { android.util.Log.e("CreationModule", "Failed to abandon audio focus", e) }
                                                    if (mediaPlayer == null) {
                                                        mediaPlayer = MediaPlayer().apply {
                                                            try {
                                                                val fis = java.io.FileInputStream(audioFile)
                                                                setDataSource(fis.fd)
                                                                fis.close()
                                                                prepare()
                                                                isLooping = true
                                                                setOnCompletionListener {
                                                                    isMusicPlaying = false
                                                                    @Suppress("DEPRECATION")
                                                                    try { audioManager.abandonAudioFocus { } } catch (e: Exception) { android.util.Log.e("CreationModule", "Failed to abandon audio focus", e) }
                                                                }
                                                            } catch (e: Exception) {
                                                                android.util.Log.e("CreationModule", "Failed to prepare MediaPlayer via FD", e)
                                                            }
                                                        }
                                                    }
                                                    mediaPlayer?.start()
                                                    isMusicPlaying = true
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("CreationModule", "Error controlling audio player", e)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(PastelPurplePrimary, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = stringResource(R.string.creation_play_pause_desc),
                                            tint = White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Column {
                                        Text(stringResource(R.string.creation_audio_filename), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(stringResource(R.string.creation_audio_success), fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    }
                                }
                            } else {
                                Icon(Icons.Default.MusicNote, contentDescription = stringResource(R.string.creation_music_icon_desc), modifier = Modifier.size(48.dp), tint = TextMuted)
                                Text(stringResource(R.string.creation_no_audio), fontSize = 12.sp, color = TextMuted)
                                Text(stringResource(R.string.creation_submit_prompt), fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSelectionButton(
    title: String,
    subtitle: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(
                color = if (active) LightPurpleBg else White,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.2.dp,
                color = if (active) PastelPurplePrimary else SoftGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (active) PastelPurpleDark else TextDark)
            Text(text = subtitle, fontSize = 9.sp, color = TextMuted)
        }
    }
}
