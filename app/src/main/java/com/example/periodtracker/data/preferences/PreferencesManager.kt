package com.example.periodtracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.periodtracker.data.model.NotificationPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _notificationPreferences = MutableStateFlow(loadNotificationPreferences())
    val notificationPreferences: StateFlow<NotificationPreferences> = _notificationPreferences.asStateFlow()

    companion object {
        private const val PREFS_NAME = "period_tracker_prefs"
        private const val KEY_NOTIF_ENABLED = "key_notif_enabled"
        private const val KEY_NOTIF_DAYS_BEFORE = "key_notif_days_before"
    }

    private fun loadNotificationPreferences(): NotificationPreferences {
        val enabled = prefs.getBoolean(KEY_NOTIF_ENABLED, false)
        val daysBefore = prefs.getInt(KEY_NOTIF_DAYS_BEFORE, 2)
        return NotificationPreferences(enabled = enabled, daysBefore = daysBefore)
    }

    fun saveNotificationPreferences(enabled: Boolean, daysBefore: Int) {
        prefs.edit()
            .putBoolean(KEY_NOTIF_ENABLED, enabled)
            .putInt(KEY_NOTIF_DAYS_BEFORE, daysBefore)
            .apply()
        _notificationPreferences.value = NotificationPreferences(enabled = enabled, daysBefore = daysBefore)
    }

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
        _notificationPreferences.value = NotificationPreferences()
    }
}
