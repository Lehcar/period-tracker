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

import com.example.periodtracker.data.model.NotificationPreferences
import com.example.periodtracker.data.preferences.PreferencesManager

enum class TrackerTab {
    CALENDAR,
    STATISTICS,
    NOTIFICATIONS
}

data class PeriodUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.now(),
    val dailyLogs: List<DailyLog> = emptyList(),
    val stats: PeriodStats = PeriodStats(null, null, null, null, 0, 0),
    val showWipeConfirmDialog: Boolean = false,
    val isLogDialogOpen: Boolean = false,
    val currentTab: TrackerTab = TrackerTab.CALENDAR,
    val notificationPreferences: NotificationPreferences = NotificationPreferences()
) {
    val selectedLog: DailyLog?
        get() = dailyLogs.find { it.date == selectedDate }
}

class PeriodViewModel(
    private val repository: PeriodRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _showWipeConfirmDialog = MutableStateFlow(false)
    private val _isLogDialogOpen = MutableStateFlow(false)
    private val _currentTab = MutableStateFlow(TrackerTab.CALENDAR)
    private val _defaultNotifPrefs = MutableStateFlow(NotificationPreferences())

    private val notifPrefsFlow: StateFlow<NotificationPreferences> =
        preferencesManager?.notificationPreferences ?: _defaultNotifPrefs

    val uiState: StateFlow<PeriodUiState> = combine(
        _selectedDate,
        _displayedMonth,
        repository.logsFlow,
        _showWipeConfirmDialog,
        _isLogDialogOpen,
        _currentTab,
        notifPrefsFlow
    ) { flows: Array<Any?> ->
        val date = flows[0] as LocalDate
        val month = flows[1] as YearMonth
        @Suppress("UNCHECKED_CAST")
        val logs = flows[2] as List<DailyLog>
        val showWipe = flows[3] as Boolean
        val showLog = flows[4] as Boolean
        val tab = flows[5] as TrackerTab
        val notifPrefs = flows[6] as NotificationPreferences

        val stats = PeriodAnalytics.computeStats(logs, LocalDate.now())
        PeriodUiState(
            selectedDate = date,
            displayedMonth = month,
            dailyLogs = logs,
            stats = stats,
            showWipeConfirmDialog = showWipe,
            isLogDialogOpen = showLog,
            currentTab = tab,
            notificationPreferences = notifPrefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PeriodUiState()
    )

    fun updateNotificationPreferences(enabled: Boolean, daysBefore: Int) {
        if (preferencesManager != null) {
            preferencesManager.saveNotificationPreferences(enabled, daysBefore)
        } else {
            _defaultNotifPrefs.value = NotificationPreferences(enabled = enabled, daysBefore = daysBefore)
        }
    }

    fun selectTab(tab: TrackerTab) {
        _currentTab.value = tab
    }

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

    fun getExportCsvData(): String {
        return com.example.periodtracker.data.csv.CsvManager.generateCsv(uiState.value.dailyLogs)
    }

    fun importCsvContent(csvContent: String, onResult: (Int) -> Unit) {
        val parsed = com.example.periodtracker.data.csv.CsvManager.parseCsv(csvContent)
        viewModelScope.launch {
            val count = repository.importDailyLogs(parsed)
            onResult(count)
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
            preferencesManager?.clearAllPreferences()
            _showWipeConfirmDialog.value = false
        }
    }
}

class PeriodViewModelFactory(
    private val repository: PeriodRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeriodViewModel(repository, preferencesManager) as T
    }
}
