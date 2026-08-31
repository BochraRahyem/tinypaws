package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
@Entity(tableName = "cat_weight_logs", indices = [Index("catId")])
data class CatWeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int = 1,
    val date: Long, // timestamp
    val weight: Float // weight in kg
)
