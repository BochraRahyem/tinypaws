package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatHistoryEntryDao {
    @Query("SELECT * FROM cat_history_entries WHERE catId = :catId ORDER BY date DESC")
    fun getHistoryEntriesForCat(catId: Int): Flow<List<CatHistoryEntry>>

    @Query("SELECT * FROM cat_history_entries ORDER BY date DESC")
    fun getAllHistoryEntries(): Flow<List<CatHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntry(entry: CatHistoryEntry)

    @Query("DELETE FROM cat_history_entries WHERE id = :id")
    suspend fun deleteHistoryEntryById(id: Int)

    @Query("DELETE FROM cat_history_entries WHERE catId = :catId")
    suspend fun deleteEntriesForCat(catId: Int)
}
