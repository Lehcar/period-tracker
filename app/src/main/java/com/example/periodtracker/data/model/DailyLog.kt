package com.example.periodtracker.data.model

import java.time.LocalDate

enum class FlowIntensity {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN,
    NONE
}

data class DailyLog(
    val date: LocalDate,
    val flowIntensity: FlowIntensity = FlowIntensity.NONE,
    val notes: String? = null
) {
    val isPeriodDay: Boolean
        get() = flowIntensity != FlowIntensity.NONE
}

data class PeriodCycle(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    val durationDays: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
}

data class PeriodStats(
    val medianCycleLengthDays: Double?,
    val averagePeriodLengthDays: Double?,
    val nextEstimatedStartDate: LocalDate?,
    val daysUntilNextPeriod: Long?,
    val totalCyclesCount: Int,
    val totalRecordedDaysCount: Int
)
