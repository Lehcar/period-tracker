package com.example.periodtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyFlowDialog(
    selectedDate: LocalDate,
    currentLog: DailyLog?,
    onSaveFlow: (FlowIntensity) -> Unit,
    onDeleteLog: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
    val currentFlow = currentLog?.flowIntensity ?: FlowIntensity.NONE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Daily Log",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C1D21)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Flow Intensity:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                FlowOptionCard(
                    title = "Heavy Flow",
                    subtitle = "Heavy bleeding day",
                    color = Color(0xFFD81B60),
                    isSelected = (currentFlow == FlowIntensity.HEAVY),
                    onClick = { onSaveFlow(FlowIntensity.HEAVY) }
                )

                FlowOptionCard(
                    title = "Medium Flow",
                    subtitle = "Moderate bleeding day",
                    color = Color(0xFFEC407A),
                    isSelected = (currentFlow == FlowIntensity.MEDIUM),
                    onClick = { onSaveFlow(FlowIntensity.MEDIUM) }
                )

                FlowOptionCard(
                    title = "Light Flow",
                    subtitle = "Spotting or light flow",
                    color = Color(0xFFF48FB1),
                    isSelected = (currentFlow == FlowIntensity.LIGHT),
                    onClick = { onSaveFlow(FlowIntensity.LIGHT) }
                )

                FlowOptionCard(
                    title = "Unknown Flow",
                    subtitle = "Flow level unrecorded or uncertain",
                    color = Color(0xFFBA68C8),
                    isSelected = (currentFlow == FlowIntensity.UNKNOWN),
                    onClick = { onSaveFlow(FlowIntensity.UNKNOWN) }
                )

                FlowOptionCard(
                    title = "None / Clear Entry",
                    subtitle = "No flow recorded",
                    color = Color.Gray,
                    isSelected = (currentFlow == FlowIntensity.NONE),
                    onClick = { onSaveFlow(FlowIntensity.NONE) }
                )
            }
        },
        confirmButton = {
            if (currentLog != null && currentLog.isPeriodDay) {
                TextButton(
                    onClick = onDeleteLog,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Delete Entry")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun FlowOptionCard(
    title: String,
    subtitle: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) color else Color(0xFFE0E0E0)
    val containerColor = if (isSelected) color.copy(alpha = 0.12f) else Color(0xFFF9F9F9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C1D21)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun WipeDataConfirmDialog(
    onConfirm: () -> Unit,
    onExportFirst: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Delete All Information?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2C1D21)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Are you sure you want to erase all period tracking data from this device? This will permanently delete your logged dates, flow entries, and statistics.\n\nThis action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242)
                )
                Text(
                    text = "💡 Tip: We recommend exporting your data to CSV first so you don't lose your history.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFFC2185B)
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportFirst,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("Export to CSV First", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete Everything", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF424242))
                }
            }
        },
        dismissButton = null
    )
}
