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
     * Logs up to 10 days apart are treated as the same period (tolerating sparse
     * logging, e.g. only the first/last day of a period being recorded); periods
     * are normally weeks apart, so a larger gap starts a new cycle.
     * Logs dated after [today] are excluded so a mistaken future-dated entry
     * can't anchor or corrupt a cycle.
     */
    fun extractPeriodCycles(logs: List<DailyLog>, today: LocalDate = LocalDate.now()): List<PeriodCycle> {
        val periodLogs = logs.filter { it.isPeriodDay && !it.date.isAfter(today) }.sortedBy { it.date }
        if (periodLogs.isEmpty()) return emptyList()

        val cycles = mutableListOf<PeriodCycle>()
        var cycleStart = periodLogs.first().date
        var cycleEnd = periodLogs.first().date

        for (i in 1 until periodLogs.size) {
            val logDate = periodLogs[i].date
            val gapDays = ChronoUnit.DAYS.between(cycleEnd, logDate)

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

    const val MAX_SINGLE_CYCLE_DAYS = 50.0
    const val MIN_SINGLE_CYCLE_DAYS = 15.0
    const val DEFAULT_CYCLE_LENGTH_DAYS = 28.0

    /**
     * Calculates median time between period start dates in days.
     * Filters out outlier gaps (> 50 days) that indicate skipped or unlogged months
     * so that missed months do not distort cycle predictions.
     * Requires at least 2 period cycles.
     */
    fun calculateMedianCycleLength(cycles: List<PeriodCycle>): Double? {
        if (cycles.size < 2) return null

        val sortedCycles = cycles.sortedBy { it.startDate }
        val validCycleLengths = mutableListOf<Double>()
        val normalizedCycleLengths = mutableListOf<Double>()

        for (i in 0 until sortedCycles.size - 1) {
            val daysBetween = ChronoUnit.DAYS.between(sortedCycles[i].startDate, sortedCycles[i + 1].startDate).toDouble()
            if (daysBetween in MIN_SINGLE_CYCLE_DAYS..MAX_SINGLE_CYCLE_DAYS) {
                validCycleLengths.add(daysBetween)
            } else if (daysBetween > MAX_SINGLE_CYCLE_DAYS) {
                val estimatedCycles = (daysBetween / DEFAULT_CYCLE_LENGTH_DAYS).roundToLong().coerceAtLeast(1)
                normalizedCycleLengths.add(daysBetween / estimatedCycles)
            }
        }

        val targetLengths = if (validCycleLengths.isNotEmpty()) validCycleLengths else normalizedCycleLengths
        if (targetLengths.isEmpty()) return null

        val sortedLengths = targetLengths.sorted()
        val count = sortedLengths.size
        return if (count % 2 == 1) {
            sortedLengths[count / 2]
        } else {
            (sortedLengths[count / 2 - 1] + sortedLengths[count / 2]) / 2.0
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
        val cycles = extractPeriodCycles(logs, today)
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
            totalRecordedDaysCount = logs.count { it.isPeriodDay && !it.date.isAfter(today) }
        )
    }
}
