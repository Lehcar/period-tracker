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
    suspend fun saveDailyLog(date: LocalDate, flowIntensity: FlowIntensity, notes: String? = null)
    suspend fun deleteDailyLog(date: LocalDate)
    suspend fun deleteAllData()
    suspend fun refreshLogs()
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

    override suspend fun saveDailyLog(date: LocalDate, flowIntensity: FlowIntensity, notes: String?) {
        withContext(Dispatchers.IO) {
            if (flowIntensity == FlowIntensity.NONE && notes.isNull_or_blank()) {
                dbHelper.deleteDailyLog(date)
            } else {
                dbHelper.upsertDailyLog(DailyLog(date = date, flowIntensity = flowIntensity, notes = notes))
            }
            val logs = dbHelper.getAllDailyLogs()
            _logsFlow.value = logs
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

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
