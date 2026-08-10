package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatWeightLogDao {
    @Query("SELECT * FROM cat_weight_logs WHERE catId = :catId ORDER BY date ASC")
    fun getWeightLogsForCat(catId: Int): Flow<List<CatWeightLog>>

    @Query("SELECT * FROM cat_weight_logs ORDER BY date ASC")
    fun getAllWeightLogs(): Flow<List<CatWeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: CatWeightLog)

    @Query("DELETE FROM cat_weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Int)

    @Query("DELETE FROM cat_weight_logs WHERE catId = :catId")
    suspend fun deleteLogsForCat(catId: Int)
}
