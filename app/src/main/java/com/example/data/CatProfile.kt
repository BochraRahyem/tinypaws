package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_profile")
data class CatProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val ageYears: Int = 0,
    val ageMonths: Int = 0,
    val coatColor: String = "orange",
    val photoUrl: String? = null,
    val personality: String = "",
    val adoptionPhotoUrl: String? = null,
    val foodPreferences: String = "",
    val chronicConditions: String? = null
)

