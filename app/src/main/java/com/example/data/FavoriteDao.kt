package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_diy")
    fun getAllFavorites(): Flow<List<FavoriteDiy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteDiy)

    @Query("DELETE FROM favorite_diy WHERE projectId = :projectId")
    suspend fun deleteFavorite(projectId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_diy WHERE projectId = :projectId)")
    fun isFavorite(projectId: String): Flow<Boolean>
}
