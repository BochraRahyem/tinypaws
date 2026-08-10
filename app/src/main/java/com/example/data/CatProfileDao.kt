package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatProfileDao {
    @Query("SELECT * FROM cat_profile ORDER BY id ASC")
    fun getAllCatProfiles(): Flow<List<CatProfile>>

    @Query("SELECT * FROM cat_profile WHERE id = :id LIMIT 1")
    fun getCatProfileById(id: Int): Flow<CatProfile?>

    @Query("SELECT * FROM cat_profile WHERE id = :id LIMIT 1")
    suspend fun getCatProfileByIdSync(id: Int): CatProfile?

    @Query("SELECT * FROM cat_profile WHERE id = 1 LIMIT 1")
    fun getCatProfile(): Flow<CatProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: CatProfile): Long

    @Query("DELETE FROM cat_profile WHERE id = :id")
    suspend fun deleteCatProfileById(id: Int)

    @Query("SELECT COUNT(*) FROM cat_profile")
    suspend fun getCatCount(): Int
}
