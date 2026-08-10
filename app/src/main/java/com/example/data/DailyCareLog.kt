package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_care_logs", primaryKeys = ["catId", "dateString"])
data class DailyCareLog(
    val catId: Int = 1,
    val dateString: String, // Format: "YYYY-MM-DD"
    val fed: Boolean = false,
    val watered: Boolean = false,
    val played: Boolean = false,
    val litterCleaned: Boolean = false,
    val groomed: Boolean = false,
    val medicationGiven: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
