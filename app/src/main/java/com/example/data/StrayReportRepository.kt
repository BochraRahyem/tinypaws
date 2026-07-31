package com.example.data

import kotlinx.coroutines.flow.Flow

class StrayReportRepository(private val strayReportDao: StrayReportDao) {
    val allReports: Flow<List<StrayReport>> = strayReportDao.getAllReports()

    suspend fun insert(report: StrayReport) {
        strayReportDao.insertReport(report)
    }

    suspend fun clear() {
        strayReportDao.clearAll()
    }
}
