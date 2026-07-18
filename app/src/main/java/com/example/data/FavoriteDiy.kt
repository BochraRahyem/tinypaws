package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_diy")
data class FavoriteDiy(
    @PrimaryKey val projectId: String
)
