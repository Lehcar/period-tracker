package com.example.periodtracker.domain

import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.PeriodCycle
import com.example.periodtracker.data.model.PeriodStats
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

object PeriodAnalytics {

    /**
     * Groups daily logs marked as period days into discrete PeriodCycle objects.
     * Consecutive days (or days with up to 1 gap day) belong to the same cycle.
     */
    fun extractPeriodCycles(logs: List<DailyLog>): List<PeriodCycle> {
        val periodLogs = logs.filter { it.isPeriodDay }.sortedBy { it.date }
        if (periodLogs.isEmpty()) return emptyList()

        val cycles = mutableListOf<PeriodCycle>()
        var cycleStart = periodLogs.first().date
        var cycleEnd = periodLogs.first().date

        for (i in 1 until periodLogs.size) {
            val logDate = periodLogs[i].date
            val gapDays = ChronoUnit.DAYS.between(cycleEnd, logDate)
            
            // Allow up to 10-day gap between logs belonging to the same monthly period cycle
            if (gapDays <= 10) {
                cycleEnd = logDate
            } else {
                cycles.add(PeriodCycle(startDate = cycleStart, endDate = cycleEnd))
                cycleStart = logDate
                cycleEnd = logDate
            }
        }
        cycles.add(PeriodCycle(startDate = cycleStart, endDate = cycleEnd))
        return cycles
    }

    /**
     * Calculates median time between period start dates in days.
     * Requires at least 2 period cycles.
     */
    fun calculateMedianCycleLength(cycles: List<PeriodCycle>): Double? {
        if (cycles.size < 2) return null

        val cycleLengths = mutableListOf<Double>()
        for (i in 0 until cycles.size - 1) {
            val daysBetween = ChronoUnit.DAYS.between(cycles[i].startDate, cycles[i + 1].startDate).toDouble()
            cycleLengths.add(daysBetween)
        }

        cycleLengths.sort()
        val count = cycleLengths.size
        return if (count % 2 == 1) {
            cycleLengths[count / 2]
        } else {
            (cycleLengths[count / 2 - 1] + cycleLengths[count / 2]) / 2.0
        }
    }

    /**
     * Calculates the average period length in days across all recorded cycles.
     */
    fun calculateAveragePeriodLength(cycles: List<PeriodCycle>): Double? {
        if (cycles.isEmpty()) return null
        val totalDays = cycles.sumOf { it.durationDays }
        return totalDays.toDouble() / cycles.size
    }

    /**
     * Predicts the next period start date based on the latest cycle start date and median cycle length (or default 28 days).
     */
    fun calculateNextEstimatedStartDate(cycles: List<PeriodCycle>): LocalDate? {
        if (cycles.isEmpty()) return null
        val lastCycle = cycles.maxByOrNull { it.startDate } ?: return null
        val medianCycle = calculateMedianCycleLength(cycles) ?: 28.0
        return lastCycle.startDate.plusDays(medianCycle.roundToLong())
    }

    /**
     * Computes all stats for the given daily logs relative to a target date (default today).
     */
    fun computeStats(logs: List<DailyLog>, today: LocalDate = LocalDate.now()): PeriodStats {
        val cycles = extractPeriodCycles(logs)
        val medianCycle = calculateMedianCycleLength(cycles)
        val avgDuration = calculateAveragePeriodLength(cycles)
        val nextStart = calculateNextEstimatedStartDate(cycles)
        val daysUntil = nextStart?.let { ChronoUnit.DAYS.between(today, it) }

        return PeriodStats(
            medianCycleLengthDays = medianCycle,
            averagePeriodLengthDays = avgDuration,
            nextEstimatedStartDate = nextStart,
            daysUntilNextPeriod = daysUntil,
            totalCyclesCount = cycles.size,
            totalRecordedDaysCount = logs.count { it.isPeriodDay }
        )
    }
}
