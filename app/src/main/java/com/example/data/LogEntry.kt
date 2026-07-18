package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityType: String, // e.g., "feed_cat", "build_shelter", "donate_food", "rescue_cat", "vet_visit"
    val activityName: String, // e.g., "Fed a stray cat", "Built a cozy shelter", etc.
    val points: Int,          // e.g., 10, 50, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""    // Optional user notes
)
