package com.example.data

import kotlinx.coroutines.flow.Flow

class LogRepository(
    private val logDao: LogDao,
    private val favoriteDao: FavoriteDao
) {
    val allLogs: Flow<List<LogEntry>> = logDao.getAllLogs()
    val allFavorites: Flow<List<FavoriteDiy>> = favoriteDao.getAllFavorites()

    suspend fun insert(log: LogEntry) {
        logDao.insertLog(log)
    }

    suspend fun deleteById(id: Int) {
        logDao.deleteLogById(id)
    }

    suspend fun clearAll() {
        logDao.clearAllLogs()
    }

    suspend fun toggleFavorite(projectId: String, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteDao.insertFavorite(FavoriteDiy(projectId))
        } else {
            favoriteDao.deleteFavorite(projectId)
        }
    }

    fun isFavorite(projectId: String): Flow<Boolean> = favoriteDao.isFavorite(projectId)
}
