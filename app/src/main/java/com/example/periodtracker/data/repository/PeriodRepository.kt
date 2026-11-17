package com.example.periodtracker.data.repository

import com.example.periodtracker.data.db.PeriodTrackerDbHelper
import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate

interface PeriodRepository {
    val logsFlow: StateFlow<List<DailyLog>>
    suspend fun saveDailyLog(date: LocalDate, flowIntensity: FlowIntensity)
    suspend fun deleteDailyLog(date: LocalDate)
    suspend fun deleteAllData()
    suspend fun refreshLogs()
    suspend fun importDailyLogs(logs: List<DailyLog>): Int
}

class PeriodRepositoryImpl(
    private val dbHelper: PeriodTrackerDbHelper
) : PeriodRepository {

    private val _logsFlow = MutableStateFlow<List<DailyLog>>(emptyList())
    override val logsFlow: StateFlow<List<DailyLog>> = _logsFlow.asStateFlow()

    suspend fun init() {
        refreshLogs()
    }

    override suspend fun refreshLogs() {
        withContext(Dispatchers.IO) {
            val logs = dbHelper.getAllDailyLogs()
            _logsFlow.value = logs
        }
    }

    override suspend fun saveDailyLog(date: LocalDate, flowIntensity: FlowIntensity) {
        withContext(Dispatchers.IO) {
            if (flowIntensity == FlowIntensity.NONE) {
                dbHelper.deleteDailyLog(date)
            } else {
                dbHelper.upsertDailyLog(DailyLog(date = date, flowIntensity = flowIntensity))
            }
            val logs = dbHelper.getAllDailyLogs()
            _logsFlow.value = logs
        }
    }

    override suspend fun importDailyLogs(logs: List<DailyLog>): Int {
        return withContext(Dispatchers.IO) {
            val count = dbHelper.upsertDailyLogs(logs)
            _logsFlow.value = dbHelper.getAllDailyLogs()
            count
        }
    }

    override suspend fun deleteDailyLog(date: LocalDate) {
        withContext(Dispatchers.IO) {
            dbHelper.deleteDailyLog(date)
            val logs = dbHelper.getAllDailyLogs()
            _logsFlow.value = logs
        }
    }

    override suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            dbHelper.deleteAllData()
            _logsFlow.value = emptyList()
        }
    }
}
