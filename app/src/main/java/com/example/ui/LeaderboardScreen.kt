package com.example.ui

import androidx.compose.ui.res.stringResource

import com.example.R

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Reporting Champions, 1: Rescue Champions
    val reportingChampions by viewModel.reportingChampions.collectAsStateWithLifecycle()
    val rescueChampions by viewModel.rescueChampions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.leaderboard_title), fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.my_cat_back_alt), tint = DeepBurgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(stringResource(R.string.leaderboard_tab_reporting), stringResource(R.string.leaderboard_tab_rescue)).forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DeepBurgundy else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isSelected) Cream else DeepBurgundy
                        )
                    }
                }
            }

            // Leaderboard content
            if (selectedTab == 0) {
                // Reporting Champions
                if (reportingChampions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🐱",
                                fontSize = 48.sp
                            )
                            Text(
                                text = stringResource(R.string.leaderboard_no_reporting),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(reportingChampions) { index, user ->
                            ChampionRow(
                                rank = index + 1,
                                name = user.displayName,
                                scoreText = stringResource(R.string.leaderboard_cats_reported, user.reportedCatsCount),
                                countryCode = user.country,
                                icon = { Icon(Icons.Default.Pets, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            } else {
                // Rescue Champions
                if (rescueChampions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⭐",
                                fontSize = 48.sp
                            )
                            Text(
                                text = stringResource(R.string.leaderboard_no_rescue),
                                fontFamily = QuicksandFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(rescueChampions) { index, user ->
                            ChampionRow(
                                rank = index + 1,
                                name = user.displayName,
                                scoreText = stringResource(R.string.leaderboard_rescue_spirit, user.rescueStars),
                                countryCode = user.country,
                                icon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChampionRow(
    rank: Int,
    name: String,
    scoreText: String,
    countryCode: String,
    icon: @Composable () -> Unit
) {
    val rankEmoji = when (rank) {
        1 -> "🥇 "
        2 -> "🥈 "
        3 -> "🥉 "
        else -> "#$rank "
    }

    val flagEmoji = when (countryCode.uppercase()) {
        "US" -> "🇺🇸"
        "FR" -> "🇫🇷"
        "ES" -> "🇪🇸"
        "SA", "AR" -> "🇸🇦"
        "CA" -> "🇨🇦"
        "GB" -> "🇬🇧"
        else -> "🌍"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("champion_row_$rank"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = rankEmoji,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = DeepBurgundy
                )
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = name.ifBlank { stringResource(R.string.leaderboard_anonymous) },
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Ink
                        )
                        Text(
                            text = flagEmoji,
                            fontSize = 16.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        icon()
                        Text(
                            text = scoreText,
                            fontFamily = QuicksandFontFamily,
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
