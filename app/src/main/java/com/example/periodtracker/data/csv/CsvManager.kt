package com.example.periodtracker.data.csv

import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import java.time.LocalDate

object CsvManager {

    private const val HEADER = "Date,FlowIntensity,Notes"

    fun generateCsv(logs: List<DailyLog>): String {
        val builder = StringBuilder()
        builder.append(HEADER).append("\n")
        logs.sortedBy { it.date }.forEach { log ->
            val notesSanitized = log.notes?.replace("\n", " ")?.replace(",", " ") ?: ""
            builder.append("${log.date},${log.flowIntensity.name},$notesSanitized\n")
        }
        return builder.toString()
    }

    fun parseCsv(csvContent: String): List<DailyLog> {
        val lines = csvContent.lines()
        val parsedLogs = mutableListOf<DailyLog>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            // Skip header line
            if (trimmed.startsWith("Date", ignoreCase = true) && trimmed.contains("FlowIntensity", ignoreCase = true)) {
                continue
            }

            val parts = trimmed.split(",")
            if (parts.size >= 2) {
                try {
                    val dateStr = parts[0].trim()
                    val flowStr = parts[1].trim()
                    val notesStr = if (parts.size >= 3) parts[2].trim().ifEmpty { null } else null

                    val date = LocalDate.parse(dateStr)
                    val flow = try {
                        FlowIntensity.valueOf(flowStr.uppercase())
                    } catch (e: Exception) {
                        FlowIntensity.UNKNOWN
                    }

                    parsedLogs.add(DailyLog(date = date, flowIntensity = flow, notes = notesStr))
                } catch (e: Exception) {
                    // Skip malformed row
                }
            }
        }
        return parsedLogs
    }
}
