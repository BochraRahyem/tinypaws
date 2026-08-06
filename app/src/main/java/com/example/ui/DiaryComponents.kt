package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DiaryEntry
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableDiaryEntry(
    log: DiaryEntry,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.6f)
                else -> Color.Transparent
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.padding(end = 24.dp),
                    tint = Color.White
                )
            }
        }
    ) {
        DiaryEntryCard(log = log)
    }
}

@Composable
fun DiaryEntryCard(log: DiaryEntry) {
    val dateStr = remember(log.date) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.date))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .glassyCard(shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = BlushPink.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val moodEmoji = when(log.mood.lowercase()) {
                        "happy" -> "😊"
                        "tired" -> "😴"
                        "unwell" -> "🤒"
                        "playful" -> "🧶"
                        "feeding" -> "🍲"
                        else -> "🐱"
                    }
                    Text(moodEmoji, fontSize = 28.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepBurgundy,
                            fontFamily = FrauncesFontFamily
                        )
                    )
                    if (log.diaryEntryType != "general") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Mauve.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = log.diaryEntryType.replace("_", " ").uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBurgundy,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Ink.copy(alpha = 0.75f),
                        fontFamily = QuicksandFontFamily
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                log.weight?.let { w ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DeepBurgundy.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "⚖️ $w kg",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepBurgundy
                            )
                        )
                    }
                }
            }
        }
    }
}
