package com.example.periodtracker.domain

import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PeriodAnalyticsTest {

    @Test
    fun `extractPeriodCycles combines consecutive days into cycles`() {
        val logs = listOf(
            DailyLog(LocalDate.of(2026, 1, 1), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 1, 2), FlowIntensity.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 3), FlowIntensity.LIGHT),
            // Next cycle 28 days later
            DailyLog(LocalDate.of(2026, 1, 29), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 1, 30), FlowIntensity.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 31), FlowIntensity.LIGHT),
            DailyLog(LocalDate.of(2026, 2, 1), FlowIntensity.LIGHT)
        )

        val cycles = PeriodAnalytics.extractPeriodCycles(logs)
        assertEquals(2, cycles.size)

        assertEquals(LocalDate.of(2026, 1, 1), cycles[0].startDate)
        assertEquals(LocalDate.of(2026, 1, 3), cycles[0].endDate)
        assertEquals(3L, cycles[0].durationDays)

        assertEquals(LocalDate.of(2026, 1, 29), cycles[1].startDate)
        assertEquals(LocalDate.of(2026, 2, 1), cycles[1].endDate)
        assertEquals(4L, cycles[1].durationDays)
    }

    @Test
    fun `calculateMedianCycleLength returns median for odd and even cycle counts`() {
        val logs = listOf(
            // Cycle 1: Jan 1 - Jan 5
            DailyLog(LocalDate.of(2026, 1, 1), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 1, 5), FlowIntensity.LIGHT),
            // Cycle 2: Jan 29 - Feb 2 (28 days gap from Jan 1)
            DailyLog(LocalDate.of(2026, 1, 29), FlowIntensity.HEAVY),
            // Cycle 3: Feb 28 - Mar 4 (30 days gap from Jan 29)
            DailyLog(LocalDate.of(2026, 2, 28), FlowIntensity.HEAVY),
            // Cycle 4: Mar 26 - Mar 30 (26 days gap from Feb 28)
            DailyLog(LocalDate.of(2026, 3, 26), FlowIntensity.HEAVY)
        )

        val cycles = PeriodAnalytics.extractPeriodCycles(logs)
        // Cycle intervals: 28, 30, 26 -> Sorted: 26, 28, 30 -> Median is 28.0
        val median = PeriodAnalytics.calculateMedianCycleLength(cycles)
        assertEquals(28.0, median!!, 0.001)
    }

    @Test
    fun `calculateAveragePeriodLength calculates correct average duration`() {
        val logs = listOf(
            // Cycle 1: 4 days (Jan 1, 2, 3, 4)
            DailyLog(LocalDate.of(2026, 1, 1), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 1, 2), FlowIntensity.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 3), FlowIntensity.MEDIUM),
            DailyLog(LocalDate.of(2026, 1, 4), FlowIntensity.LIGHT),
            // Cycle 2: 6 days (Feb 1 .. Feb 6)
            DailyLog(LocalDate.of(2026, 2, 1), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 2, 6), FlowIntensity.LIGHT)
        )

        val cycles = PeriodAnalytics.extractPeriodCycles(logs)
        val avgDuration = PeriodAnalytics.calculateAveragePeriodLength(cycles)
        // (4 + 6) / 2 = 5.0
        assertEquals(5.0, avgDuration!!, 0.001)
    }

    @Test
    fun `calculateNextEstimatedStartDate predicts future date based on median`() {
        val logs = listOf(
            DailyLog(LocalDate.of(2026, 1, 1), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 1, 29), FlowIntensity.HEAVY)
        )
        val cycles = PeriodAnalytics.extractPeriodCycles(logs)
        val nextEstimated = PeriodAnalytics.calculateNextEstimatedStartDate(cycles)
        // Jan 29 + 28 days = Feb 26
        assertEquals(LocalDate.of(2026, 2, 26), nextEstimated)
    }

    @Test
    fun `extractPeriodCycles includes unknown flow intensity as period days`() {
        val logs = listOf(
            DailyLog(LocalDate.of(2026, 1, 1), FlowIntensity.UNKNOWN),
            DailyLog(LocalDate.of(2026, 1, 2), FlowIntensity.UNKNOWN),
            DailyLog(LocalDate.of(2026, 1, 3), FlowIntensity.LIGHT)
        )
        val cycles = PeriodAnalytics.extractPeriodCycles(logs)
        assertEquals(1, cycles.size)
        assertEquals(3L, cycles[0].durationDays)
    }

    @Test
    fun `computeStats handles empty logs safely`() {
        val stats = PeriodAnalytics.computeStats(emptyList(), LocalDate.of(2026, 3, 1))
        assertNull(stats.medianCycleLengthDays)
        assertNull(stats.averagePeriodLengthDays)
        assertNull(stats.nextEstimatedStartDate)
        assertNull(stats.daysUntilNextPeriod)
        assertEquals(0, stats.totalCyclesCount)
        assertEquals(0, stats.totalRecordedDaysCount)
    }
}
