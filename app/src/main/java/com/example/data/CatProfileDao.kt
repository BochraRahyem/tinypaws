package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatProfileDao {
    @Query("SELECT * FROM cat_profile WHERE id = 1 LIMIT 1")
    fun getCatProfile(): Flow<CatProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CatProfile)
}
