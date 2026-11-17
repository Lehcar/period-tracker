package com.example.periodtracker.data.model

data class NotificationPreferences(
    val enabled: Boolean = false,
    val daysBefore: Int = 2
)
