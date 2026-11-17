package com.example.periodtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.periodtracker.data.db.PeriodTrackerDbHelper
import com.example.periodtracker.data.preferences.PreferencesManager
import com.example.periodtracker.domain.PeriodAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fired daily by [NotificationScheduler] to check whether a period reminder is due. */
class PeriodReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferencesManager = PreferencesManager(context)
                val prefs = preferencesManager.notificationPreferences.value
                if (prefs.enabled) {
                    val logs = PeriodTrackerDbHelper(context).getAllDailyLogs()
                    val stats = PeriodAnalytics.computeStats(logs)
                    val nextStart = stats.nextEstimatedStartDate
                    val daysUntil = stats.daysUntilNextPeriod

                    if (nextStart != null && daysUntil != null &&
                        daysUntil in 0..prefs.daysBefore.toLong() &&
                        preferencesManager.getLastNotifiedDate() != nextStart &&
                        PeriodNotificationHelper.hasNotificationPermission(context)
                    ) {
                        PeriodNotificationHelper.showReminderNotification(context, nextStart, daysUntil.toInt())
                        preferencesManager.setLastNotifiedDate(nextStart)
                    }

                    NotificationScheduler.scheduleDailyCheck(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
