package com.example.periodtracker.data.csv

import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CsvManagerTest {

    @Test
    fun `generateCsv creates valid CSV string with header`() {
        val logs = listOf(
            DailyLog(LocalDate.of(2026, 4, 1), FlowIntensity.HEAVY),
            DailyLog(LocalDate.of(2026, 4, 2), FlowIntensity.MEDIUM)
        )
        val csv = CsvManager.generateCsv(logs)
        val lines = csv.trim().lines()

        assertEquals("Date,FlowIntensity,Notes", lines[0])
        assertEquals("2026-04-01,HEAVY,", lines[1])
        assertEquals("2026-04-02,MEDIUM,", lines[2])
    }

    @Test
    fun `parseCsv correctly parses CSV content into DailyLogs`() {
        val csvContent = """
            Date,FlowIntensity,Notes
            2026-04-01,HEAVY,heavy day
            2026-04-02,LIGHT,
            2026-04-03,UNKNOWN,
        """.trimIndent()

        val logs = CsvManager.parseCsv(csvContent)
        assertEquals(3, logs.size)

        assertEquals(LocalDate.of(2026, 4, 1), logs[0].date)
        assertEquals(FlowIntensity.HEAVY, logs[0].flowIntensity)
        assertEquals("heavy day", logs[0].notes)

        assertEquals(LocalDate.of(2026, 4, 2), logs[1].date)
        assertEquals(FlowIntensity.LIGHT, logs[1].flowIntensity)

        assertEquals(LocalDate.of(2026, 4, 3), logs[2].date)
        assertEquals(FlowIntensity.UNKNOWN, logs[2].flowIntensity)
    }

    @Test
    fun `parseCsv handles multiline notes and escaped quotes in CSV content`() {
        val multilineNotes = "Line 1\nLine 2 with \"quotes\""
        val originalLog = DailyLog(LocalDate.of(2026, 5, 1), FlowIntensity.HEAVY, notes = multilineNotes)
        val csv = CsvManager.generateCsv(listOf(originalLog))

        val parsed = CsvManager.parseCsv(csv)
        assertEquals(1, parsed.size)
        assertEquals(LocalDate.of(2026, 5, 1), parsed[0].date)
        assertEquals(FlowIntensity.HEAVY, parsed[0].flowIntensity)
        assertEquals(multilineNotes, parsed[0].notes)
    }
}
