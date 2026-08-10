package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatCheckInLogDao {
    @Query("SELECT * FROM cat_check_in_logs WHERE catId = :catId ORDER BY date DESC")
    fun getCheckInLogsForCat(catId: Int): Flow<List<CatCheckInLog>>

    @Query("SELECT * FROM cat_check_in_logs ORDER BY date DESC")
    fun getAllCheckInLogs(): Flow<List<CatCheckInLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckInLog(log: CatCheckInLog)

    @Query("DELETE FROM cat_check_in_logs WHERE id = :id")
    suspend fun deleteCheckInLogById(id: Int)

    @Query("DELETE FROM cat_check_in_logs WHERE catId = :catId")
    suspend fun deleteLogsForCat(catId: Int)
}
