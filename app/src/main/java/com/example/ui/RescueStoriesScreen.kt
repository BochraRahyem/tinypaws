package com.example.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val stories by viewModel.rescueStories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isRescueStoriesLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rescue Stories 💖", fontFamily = FrauncesFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        containerColor = Cream
    ) { padding ->
        if (isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(6) {
                    RescueStoryShimmerItem()
                }
            }
        } else if (stories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No rescue stories yet. Be the first to rescue! 🐾", fontFamily = QuicksandFontFamily, color = TextMuted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(stories) { story ->
                    RescueStoryCard(story)
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
                model = report.rescuedPhotoUrl ?: report.photoUrl,
                contentDescription = "Rescued Cat",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = "Rescued! 🎉",
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
