package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int = 1,
    val catIds: String = "", // Comma-separated cat IDs if shared reminder
    val title: String,
    val timeMillis: Long,
    val type: String = "general", // "vet_visit", "vaccination", "medication", "meal", "general"
    val isRecurring: Boolean = false,
    val isEnabled: Boolean = true
)
