package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_check_in_logs")
data class CatCheckInLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // timestamp
    val mood: String, // happy, tired, unwell
    val healthStatus: String // general health status
)
