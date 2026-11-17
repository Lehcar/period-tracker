package com.example.periodtracker.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import java.time.LocalDate

class PeriodTrackerDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "period_tracker.db"
        const val DATABASE_VERSION = 1

        const val TABLE_DAILY_LOGS = "daily_logs"
        const val COLUMN_DATE = "date"
        const val COLUMN_FLOW = "flow_intensity"
        const val COLUMN_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_DAILY_LOGS (
                $COLUMN_DATE TEXT PRIMARY KEY,
                $COLUMN_FLOW TEXT NOT NULL,
                $COLUMN_NOTES TEXT
            )
        """.trimIndent()
        db.execSQL(createTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAILY_LOGS")
        onCreate(db)
    }

    fun upsertDailyLog(log: DailyLog) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DATE, log.date.toString())
            put(COLUMN_FLOW, log.flowIntensity.name)
            put(COLUMN_NOTES, log.notes)
        }
        db.insertWithOnConflict(
            TABLE_DAILY_LOGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getDailyLog(date: LocalDate): DailyLog? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            arrayOf(COLUMN_DATE, COLUMN_FLOW, COLUMN_NOTES),
            "$COLUMN_DATE = ?",
            arrayOf(date.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val flowStr = it.getString(it.getColumnIndexOrThrow(COLUMN_FLOW))
                val notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))
                val flow = try {
                    FlowIntensity.valueOf(flowStr)
                } catch (e: Exception) {
                    FlowIntensity.NONE
                }
                return DailyLog(date = date, flowIntensity = flow, notes = notes)
            }
        }
        return null
    }

    fun getAllDailyLogs(): List<DailyLog> {
        val db = readableDatabase
        val list = mutableListOf<DailyLog>()
        val cursor = db.query(
            TABLE_DAILY_LOGS,
            arrayOf(COLUMN_DATE, COLUMN_FLOW, COLUMN_NOTES),
            null,
            null,
            null,
            null,
            "$COLUMN_DATE ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val dateStr = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE))
                val flowStr = it.getString(it.getColumnIndexOrThrow(COLUMN_FLOW))
                val notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES))
                val date = LocalDate.parse(dateStr)
                val flow = try {
                    FlowIntensity.valueOf(flowStr)
                } catch (e: Exception) {
                    FlowIntensity.NONE
                }
                list.add(DailyLog(date = date, flowIntensity = flow, notes = notes))
            }
        }
        return list
    }

    fun deleteDailyLog(date: LocalDate): Int {
        val db = writableDatabase
        return db.delete(TABLE_DAILY_LOGS, "$COLUMN_DATE = ?", arrayOf(date.toString()))
    }

    fun deleteAllData(): Int {
        val db = writableDatabase
        return db.delete(TABLE_DAILY_LOGS, null, null)
    }
}
