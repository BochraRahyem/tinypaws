package com.example.ui

import com.example.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.util.shimmerEffect
import com.example.data.CatReport
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescueStoriesScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit
) {
    val helpedCats by viewModel.helpedAndAdoptedCats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isRescueStoriesLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rescue_stories_title), fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Bold, color = DeepBurgundy) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.loc_back_to_home), tint = DeepBurgundy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        if (isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(4) {
                    RescueStoryShimmerItem()
                }
            }
        } else if (helpedCats.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.rescue_stories_empty),
                    fontFamily = QuicksandFontFamily,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(helpedCats) { cat ->
                    HelpedCatCard(cat)
                }
            }
        }
    }
}

@Composable
fun HelpedCatCard(report: CatReport) {
    val isAdopted = report.status == "adopted"
    val containerColor = if (isAdopted) Color(0xFFEDE7F6) else Color(0xFFFCE4EC) // 💜 Purple vs 🩷 Pink
    val headerBadgeColor = if (isAdopted) Color(0xFF512DA8) else Color(0xFFC2185B)
    val headerText = if (isAdopted) stringResource(R.string.rescue_stories_adopted) else stringResource(R.string.rescue_stories_helped)
    val recentlyText = stringResource(R.string.rescue_stories_recently)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("helped_cat_card_${report.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = headerBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = headerBadgeColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = QuicksandFontFamily
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                val ts = report.createdAt?.seconds ?: 0L
                val dateStr = remember(ts, recentlyText) {
                    if (ts > 0) java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(ts * 1000L)) else recentlyText
                }
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = QuicksandFontFamily
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    val photo = report.rescuedPhotoUrl ?: report.displayPhotoUrl
                    if (photo.startsWith("http") || photo.startsWith("content://") || photo.startsWith("file://")) {
                        AsyncImage(
                            model = photo,
                            contentDescription = stringResource(R.string.rescue_stories_cat_alt),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = when (photo) {
                                "cat_orange" -> "🍊🐈"
                                "cat_black" -> "🐈‍⬛🖤"
                                "cat_grey" -> "🩶🐈"
                                "cat_calico" -> "🐱✨"
                                else -> "🐱🐾"
                            },
                            fontSize = 32.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    val description = report.description.ifEmpty { stringResource(R.string.rescue_stories_stray_cat) }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                            fontFamily = QuicksandFontFamily
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val needsText = report.needs.ifEmpty { stringResource(R.string.rescue_stories_needs_default) }
                    Text(
                        text = stringResource(R.string.rescue_stories_needs, needsText),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerBadgeColor,
                        fontFamily = QuicksandFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun RescueStoryShimmerItem() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).shimmerEffect())
            Column(Modifier.padding(12.dp)) {
                Box(Modifier.width(60.dp).height(12.dp).shimmerEffect())
                Spacer(modifier = Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).shimmerEffect())
                Spacer(modifier = Modifier.height(2.dp))
                Box(Modifier.fillMaxWidth(0.7f).height(10.dp).shimmerEffect())
            }
        }
    }
}

@Composable
fun RescueStoryCard(report: CatReport) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = report.rescuedPhotoUrl ?: report.displayPhotoUrl,
                contentDescription = stringResource(R.string.rescue_stories_rescued_alt),
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.rescue_stories_rescued_badge),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = DeepBurgundy,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.rescuedDescription ?: report.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    fontFamily = QuicksandFontFamily
                )
            }
        }
    }
}
