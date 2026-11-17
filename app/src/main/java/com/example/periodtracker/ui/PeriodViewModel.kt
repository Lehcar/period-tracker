package com.example.periodtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import com.example.periodtracker.data.model.PeriodStats
import com.example.periodtracker.data.repository.PeriodRepository
import com.example.periodtracker.domain.PeriodAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class PeriodUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.now(),
    val dailyLogs: List<DailyLog> = emptyList(),
    val stats: PeriodStats = PeriodStats(null, null, null, null, 0, 0),
    val showWipeConfirmDialog: Boolean = false,
    val isLogDialogOpen: Boolean = false
) {
    val selectedLog: DailyLog?
        get() = dailyLogs.find { it.date == selectedDate }
}

class PeriodViewModel(
    private val repository: PeriodRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _showWipeConfirmDialog = MutableStateFlow(false)
    private val _isLogDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<PeriodUiState> = combine(
        _selectedDate,
        _displayedMonth,
        repository.logsFlow,
        _showWipeConfirmDialog,
        _isLogDialogOpen
    ) { date, month, logs, showWipe, showLog ->
        val stats = PeriodAnalytics.computeStats(logs, LocalDate.now())
        PeriodUiState(
            selectedDate = date,
            displayedMonth = month,
            dailyLogs = logs,
            stats = stats,
            showWipeConfirmDialog = showWipe,
            isLogDialogOpen = showLog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PeriodUiState()
    )

    init {
        viewModelScope.launch {
            repository.refreshLogs()
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun openLogForDate(date: LocalDate) {
        _selectedDate.value = date
        _isLogDialogOpen.value = true
    }

    fun closeLogDialog() {
        _isLogDialogOpen.value = false
    }

    fun changeMonth(deltaMonths: Long) {
        _displayedMonth.value = _displayedMonth.value.plusMonths(deltaMonths)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _displayedMonth.value = YearMonth.now()
    }

    fun saveFlowForSelectedDate(flowIntensity: FlowIntensity) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.saveDailyLog(date, flowIntensity)
            _isLogDialogOpen.value = false
        }
    }

    fun deleteSelectedDateLog() {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.deleteDailyLog(date)
            _isLogDialogOpen.value = false
        }
    }

    fun requestWipeData() {
        _showWipeConfirmDialog.value = true
    }

    fun dismissWipeDialog() {
        _showWipeConfirmDialog.value = false
    }

    fun confirmWipeData() {
        viewModelScope.launch {
            repository.deleteAllData()
            _showWipeConfirmDialog.value = false
        }
    }
}

class PeriodViewModelFactory(
    private val repository: PeriodRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeriodViewModel(repository) as T
    }
}
