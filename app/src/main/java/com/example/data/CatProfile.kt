package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_profile")
data class CatProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val ageYears: Int,
    val ageMonths: Int,
    val coatColor: String
)
