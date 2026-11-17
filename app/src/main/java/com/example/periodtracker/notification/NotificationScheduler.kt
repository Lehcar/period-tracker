package com.example.periodtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZoneId
import java.time.ZonedDateTime

/** Schedules a daily inexact alarm that re-checks whether a period reminder should fire. */
object NotificationScheduler {

    private const val REQUEST_CODE = 2001
    private const val DAILY_CHECK_HOUR = 9

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PeriodReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleDailyCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var next = now.toLocalDate().atTime(DAILY_CHECK_HOUR, 0).atZone(ZoneId.systemDefault())
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next.toInstant().toEpochMilli(),
            pendingIntent(context)
        )
    }

    fun cancelDailyCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }
}
