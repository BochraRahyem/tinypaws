package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
@Entity(tableName = "cat_history_entries", indices = [Index("catId")])
data class CatHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int = 1,
    val title: String,
    val date: Long, // timestamp
    val category: String, // "Vet", "Vaccination", "Medication", "Milestone", "Other"
    val notes: String = ""
)
