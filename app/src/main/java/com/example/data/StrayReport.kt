package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "stray_reports")
data class StrayReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val needs: String, // e.g. "Food & Water", "Medical Attention", "Shelter", "Warm Blanket"
    val latitude: Double,
    val longitude: Double,
    val photoUri: String, // Can be local content URI, drawable resource name, or empty
    val timestamp: Long = System.currentTimeMillis(),
    val reporterName: String = "Anonymous"
)

@Dao
interface StrayReportDao {
    @Query("SELECT * FROM stray_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<StrayReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: StrayReport)

    @Query("DELETE FROM stray_reports")
    suspend fun clearAll()
}
