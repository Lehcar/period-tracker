package com.example.periodtracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.periodtracker.data.preferences.PreferencesManager

/** Re-arms the daily reminder check after a reboot, since AlarmManager alarms don't survive one. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = PreferencesManager(context).notificationPreferences.value
        if (prefs.enabled) {
            NotificationScheduler.scheduleDailyCheck(context)
        }
    }
}
