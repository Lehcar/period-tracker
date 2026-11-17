package com.example.periodtracker.ui

import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import com.example.periodtracker.data.repository.PeriodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FakePeriodRepository : PeriodRepository {
    private val _logs = MutableStateFlow<List<DailyLog>>(emptyList())
    override val logsFlow: StateFlow<List<DailyLog>> = _logs.asStateFlow()

    override suspend fun saveDailyLog(date: LocalDate, flowIntensity: FlowIntensity, notes: String?) {
        val current = _logs.value.toMutableList()
        current.removeAll { it.date == date }
        if (flowIntensity != FlowIntensity.NONE) {
            current.add(DailyLog(date, flowIntensity, notes))
        }
        _logs.value = current
    }

    override suspend fun deleteDailyLog(date: LocalDate) {
        val current = _logs.value.toMutableList()
        current.removeAll { it.date == date }
        _logs.value = current
    }

    override suspend fun deleteAllData() {
        _logs.value = emptyList()
    }

    override suspend fun importDailyLogs(logs: List<DailyLog>): Int {
        val current = _logs.value.toMutableList()
        for (log in logs) {
            current.removeAll { it.date == log.date }
            current.add(log)
        }
        _logs.value = current
        return logs.size
    }

    override suspend fun refreshLogs() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class PeriodViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePeriodRepository
    private lateinit var viewModel: PeriodViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePeriodRepository()
        viewModel = PeriodViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveFlowForSelectedDate updates UI state and logs`() = runTest {
        val targetDate = LocalDate.of(2026, 4, 15)
        viewModel.selectDate(targetDate)
        viewModel.saveFlowForSelectedDate(FlowIntensity.HEAVY)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(targetDate, state.selectedDate)
        assertEquals(1, state.dailyLogs.size)
        assertEquals(FlowIntensity.HEAVY, state.selectedLog?.flowIntensity)
    }

    @Test
    fun `selectTab updates currentTab in uiState`() = runTest {
        assertEquals(TrackerTab.CALENDAR, viewModel.uiState.value.currentTab)
        viewModel.selectTab(TrackerTab.STATISTICS)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TrackerTab.STATISTICS, viewModel.uiState.value.currentTab)
    }

    @Test
    fun `confirmWipeData removes all logs`() = runTest {
        viewModel.selectDate(LocalDate.of(2026, 4, 15))
        viewModel.saveFlowForSelectedDate(FlowIntensity.MEDIUM)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.dailyLogs.size)

        viewModel.requestWipeData()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showWipeConfirmDialog)

        viewModel.confirmWipeData()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.dailyLogs.size)
        assertFalse(viewModel.uiState.value.showWipeConfirmDialog)
    }
}
