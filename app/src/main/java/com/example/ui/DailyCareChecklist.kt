package com.example.ui

import com.example.R

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyCareLog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyCareChecklistSection(
    viewModel: TinyPawsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateStr = remember(selectedDateMillis) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis))
    }
    val displayDateStr = remember(selectedDateMillis) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dateFormatted = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(selectedDateMillis))
        if (dateStr == todayStr) context.getString(R.string.checklist_today, dateFormatted)
        else SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    val careLogFlow = remember(dateStr) { viewModel.getCareLogForDate(dateStr) }
    val careLogState by careLogFlow.collectAsStateWithLifecycle()

    val currentLog = careLogState ?: DailyCareLog(dateString = dateStr)

    val tasks = listOf(
        TaskItem("fed", stringResource(R.string.checklist_fed), "🍲", currentLog.fed),
        TaskItem("watered", stringResource(R.string.checklist_watered), "💧", currentLog.watered),
        TaskItem("played", stringResource(R.string.checklist_played), "🧶", currentLog.played),
        TaskItem("litterCleaned", stringResource(R.string.checklist_litter), "🧹", currentLog.litterCleaned),
        TaskItem("groomed", stringResource(R.string.checklist_groomed), "🪮", currentLog.groomed),
        TaskItem("medicationGiven", stringResource(R.string.checklist_meds), "💊", currentLog.medicationGiven)
    )

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val percentInt = (progressFraction * 100).toInt()

    PixelCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        emblemType = "paw"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.checklist_title),
                        fontFamily = FrauncesFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DeepBurgundy
                    )
                    Text(
                        text = displayDateStr,
                        fontFamily = QuicksandFontFamily,
                        fontSize = 13.sp,
                        color = Wine.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = com.example.ui.theme.rememberHapticOnClick {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = selectedDateMillis
                        android.app.DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val c = Calendar.getInstance()
                                c.set(y, m, d)
                                selectedDateMillis = c.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.testTag("checklist_date_picker_btn")
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.checklist_change_date), tint = DeepBurgundy)
                }
            }

            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.checklist_progress, completedCount, totalCount),
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DeepBurgundy
                    )
                    Text(
                        text = "$percentInt%",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Wine
                    )
                }
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = DeepBurgundy,
                    trackColor = Mauve.copy(alpha = 0.3f)
                )
            }

            if (percentInt == 100) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Text(
                            text = stringResource(R.string.checklist_all_done),
                            fontFamily = QuicksandFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            // Task List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tasks.forEach { task ->
                    TaskRowItem(
                        task = task,
                        onToggle = {
                            val updated = when (task.id) {
                                "fed" -> currentLog.copy(fed = !currentLog.fed)
                                "watered" -> currentLog.copy(watered = !currentLog.watered)
                                "played" -> currentLog.copy(played = !currentLog.played)
                                "litterCleaned" -> currentLog.copy(litterCleaned = !currentLog.litterCleaned)
                                "groomed" -> currentLog.copy(groomed = !currentLog.groomed)
                                "medicationGiven" -> currentLog.copy(medicationGiven = !currentLog.medicationGiven)
                                else -> currentLog
                            }
                            viewModel.saveCareLog(updated)
                        }
                    )
                }
            }
        }
    }
}

private data class TaskItem(
    val id: String,
    val title: String,
    val emoji: String,
    val isCompleted: Boolean
)

@Composable
private fun TaskRowItem(
    task: TaskItem,
    onToggle: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val view = androidx.compose.ui.platform.LocalView.current

    val performHapticToggle = {
        try {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (e: Exception) {
            // fallback
        }
        onToggle()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = com.example.ui.theme.rememberHapticOnClick { performHapticToggle() })
            .testTag("care_task_${task.id}"),
        color = if (task.isCompleted) DeepBurgundy.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) DeepBurgundy.copy(alpha = 0.4f) else Mauve.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = task.emoji, fontSize = 20.sp)
                Text(
                    text = task.title,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = if (task.isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (task.isCompleted) DeepBurgundy else Ink
                )
            }

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { performHapticToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = DeepBurgundy,
                    uncheckedColor = Mauve,
                    checkmarkColor = Cream
                )
            )
        }
    }
}
