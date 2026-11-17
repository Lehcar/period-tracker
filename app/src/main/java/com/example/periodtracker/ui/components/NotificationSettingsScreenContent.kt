package com.example.periodtracker.ui.components

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodtracker.data.model.NotificationPreferences
import com.example.periodtracker.data.model.PeriodStats
import com.example.periodtracker.notification.PeriodNotificationHelper
import java.time.format.DateTimeFormatter

@Composable
fun NotificationSettingsScreenContent(
    preferences: NotificationPreferences,
    stats: PeriodStats,
    onUpdatePreferences: (enabled: Boolean, daysBefore: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Request notification permission on Android 13+ (API 33+) if needed
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Notification Settings",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color(0xFF2C1D21)
        )

        // Card 1: Main Notification Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Period Start Reminder",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C1D21)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Get notified on your device before your estimated next period starts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Switch(
                    checked = preferences.enabled,
                    onCheckedChange = { isEnabled ->
                        if (isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onUpdatePreferences(isEnabled, preferences.daysBefore)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFE91E63)
                    )
                )
            }
        }

        // Card 2: Days Before Selector (only visible if enabled)
        if (preferences.enabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "How many days before?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C1D21)
                    )
                    Text(
                        text = "Select when you want to receive the reminder notification:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dayOptions = listOf(1, 2, 3, 5, 7)
                        for (days in dayOptions) {
                            val isSelected = (preferences.daysBefore == days)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onUpdatePreferences(true, days) },
                                label = {
                                    Text(
                                        text = "$days ${if (days == 1) "day" else "days"}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFCE4EC),
                                    selectedLabelColor = Color(0xFFE91E63)
                                )
                            )
                        }
                    }
                }
            }

            // Card 3: Status & Test Notification Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Notification Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2C1D21)
                        )
                    }

                    val nextEst = stats.nextEstimatedStartDate
                    val daysBefore = preferences.daysBefore
                    val statusMessage = if (nextEst != null) {
                        val notifDate = nextEst.minusDays(daysBefore.toLong())
                        val notifDateStr = notifDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
                        val estDateStr = nextEst.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        "Your next reminder will be delivered on $notifDateStr ($daysBefore ${if (daysBefore == 1) "day" else "days"} before your estimated start date of $estDateStr)."
                    } else {
                        "Notifications are active ($daysBefore ${if (daysBefore == 1) "day" else "days"} before). Log your period dates to enable calculated date alerts."
                    }

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242)
                    )

                    Button(
                        onClick = {
                            if (!PeriodNotificationHelper.hasNotificationPermission(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val targetDate = stats.nextEstimatedStartDate ?: java.time.LocalDate.now().plusDays(daysBefore.toLong())
                                val success = PeriodNotificationHelper.showReminderNotification(context, targetDate, daysBefore)
                                if (success) {
                                    Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Notifications disabled in system settings.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("Send Test Notification", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
