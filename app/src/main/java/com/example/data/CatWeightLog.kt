package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_weight_logs")
data class CatWeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int = 1,
    val date: Long, // timestamp
    val weight: Float // weight in kg
)
