package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: TinyPawsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders_title), fontFamily = FrauncesFontFamily) },
                navigationIcon = {
                    IconButton(onClick = com.example.ui.theme.rememberHapticOnClick { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = com.example.ui.theme.rememberHapticOnClick {  showAddDialog = true },
                containerColor = DeepBurgundy
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_reminder), tint = Cream)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reminders) { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlushPink.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = reminder.title, fontFamily = QuicksandFontFamily, fontWeight = FontWeight.Bold)
                            Text(
                                text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(reminder.timeMillis)),
                                fontFamily = QuicksandFontFamily,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = com.example.ui.theme.rememberHapticOnClick {  viewModel.deleteReminder(reminder.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_desc))
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddReminderDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, time ->
                    viewModel.saveReminder(title, time)
                    notificationHelper.scheduleNotification(Random().nextInt(1000), title, time)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var timeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_reminder)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title_label)) })
                Button(onClick = com.example.ui.theme.rememberHapticOnClick { 
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(context, { _, y, m, d ->
                        calendar.set(y, m, d)
                        TimePickerDialog(context, { _, h, min ->
                            calendar.set(Calendar.HOUR_OF_DAY, h)
                            calendar.set(Calendar.MINUTE, min)
                            timeMillis = calendar.timeInMillis
                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Text(stringResource(R.string.select_date_time))
                }
            }
        },
        confirmButton = {
            Button(onClick = com.example.ui.theme.rememberHapticOnClick {  onAdd(title, timeMillis) }) { Text(stringResource(R.string.save_reminder)) }
        }
    )
}
