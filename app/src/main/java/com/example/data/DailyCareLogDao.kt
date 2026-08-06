package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCareLogDao {
    @Query("SELECT * FROM daily_care_logs WHERE dateString = :dateStr LIMIT 1")
    fun getCareLogForDate(dateStr: String): Flow<DailyCareLog?>

    @Query("SELECT * FROM daily_care_logs ORDER BY dateString DESC")
    fun getAllCareLogs(): Flow<List<DailyCareLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCareLog(careLog: DailyCareLog)
}
