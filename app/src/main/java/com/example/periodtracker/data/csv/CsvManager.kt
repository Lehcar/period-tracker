package com.example.periodtracker.data.csv

import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import java.time.LocalDate

object CsvManager {

    private const val HEADER = "Date,FlowIntensity,Notes"

    private fun csvField(value: String): String {
        return if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    fun generateCsv(logs: List<DailyLog>): String {
        val builder = StringBuilder()
        builder.append(HEADER).append("\n")
        logs.sortedBy { it.date }.forEach { log ->
            val notesField = csvField(log.notes ?: "")
            builder.append("${log.date},${log.flowIntensity.name},$notesField\n")
        }
        return builder.toString()
    }

    /** Parses full CSV content into records (lists of fields), correctly preserving multiline quoted fields. */
    private fun parseCsvRecords(csvContent: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        val currentRecord = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < csvContent.length) {
            val c = csvContent[i]
            when {
                inQuotes && c == '"' && i + 1 < csvContent.length && csvContent[i + 1] == '"' -> {
                    currentField.append('"')
                    i++
                }
                c == '"' -> {
                    inQuotes = !inQuotes
                }
                c == ',' && !inQuotes -> {
                    currentRecord.add(currentField.toString())
                    currentField.clear()
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < csvContent.length && csvContent[i + 1] == '\n') {
                        i++
                    }
                    currentRecord.add(currentField.toString())
                    currentField.clear()
                    if (currentRecord.any { it.isNotBlank() }) {
                        records.add(ArrayList(currentRecord))
                    }
                    currentRecord.clear()
                }
                else -> {
                    currentField.append(c)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
            currentRecord.add(currentField.toString())
            if (currentRecord.any { it.isNotBlank() }) {
                records.add(currentRecord)
            }
        }

        return records
    }

    fun parseCsv(csvContent: String): List<DailyLog> {
        val records = parseCsvRecords(csvContent)
        val parsedLogs = mutableListOf<DailyLog>()

        for (record in records) {
            if (record.isEmpty()) continue
            val dateStr = record[0].trim()
            if (dateStr.equals("Date", ignoreCase = true) || dateStr.isEmpty()) {
                continue
            }
            if (record.size >= 2) {
                try {
                    val flowStr = record[1].trim()
                    val notesStr = if (record.size >= 3) record[2].ifEmpty { null } else null

                    val date = LocalDate.parse(dateStr)
                    val flow = try {
                        FlowIntensity.valueOf(flowStr.uppercase())
                    } catch (e: Exception) {
                        FlowIntensity.NONE
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
