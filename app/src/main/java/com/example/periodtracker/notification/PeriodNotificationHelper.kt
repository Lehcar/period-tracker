package com.example.periodtracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.periodtracker.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PeriodNotificationHelper {

    private const val CHANNEL_ID = "period_reminder_channel"
    private const val CHANNEL_NAME = "Period Reminders"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming estimated period start dates"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showReminderNotification(context: Context, estimatedDate: LocalDate, daysBefore: Int): Boolean {
        createNotificationChannel(context)

        val dateStr = estimatedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        val contentText = "Your next period is estimated to start in $daysBefore ${if (daysBefore == 1) "day" else "days"} ($dateStr)."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_red_dot)
            .setColor(0xFFE91E63.toInt())
            .setContentTitle("Upcoming Period Reminder")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return try {
            notificationManager.notify(NOTIFICATION_ID, notification)
            true
        } catch (e: Exception) {
            false
        }
    }
}
