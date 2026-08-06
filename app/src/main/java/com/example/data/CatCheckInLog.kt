package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_check_in_logs")
data class CatCheckInLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // timestamp
    val mood: String, // happy, tired, unwell, playful
    val healthStatus: String, // general health status or detailed notes
    val weight: Float? = null,
    val photos: String = "", // comma-separated photo URLs / resource names
    val category: String = "general", // "general", "vet_visit", "vaccination", "medication", "grooming"
    val reminderTimeMillis: Long? = null
)
