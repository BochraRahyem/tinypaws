package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun FeedingMoodDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit // type (feeding/mood), notes
) {
    var selectedType by remember { mutableStateOf("Feeding") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Quick Log 🐾",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FrauncesFontFamily,
                    color = DeepBurgundy
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Feeding", "Mood").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontFamily = QuicksandFontFamily) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepBurgundy,
                                selectedLabelColor = Cream
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes", fontFamily = QuicksandFontFamily) },
                    placeholder = { Text(if (selectedType == "Feeding") "What did they eat?" else "How is their mood?", fontFamily = QuicksandFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepBurgundy,
                        unfocusedBorderColor = Mauve
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedType, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy)
            ) {
                Text(stringResource(R.string.save_reminder), color = Cream, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_btn), color = DeepBurgundy)
            }
        },
        containerColor = Cream,
        shape = RoundedCornerShape(28.dp)
    )
}
