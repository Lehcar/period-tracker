package com.example.periodtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodtracker.data.model.PeriodStats
import java.time.format.DateTimeFormatter

@Composable
fun PeriodStatsHeader(
    stats: PeriodStats,
    modifier: Modifier = Modifier
) {
    val rosePrimary = Color(0xFFE91E63)
    val roseContainer = Color(0xFFFCE4EC)
    val cardBg = Color(0xFFFFF0F5)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Next Period Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = roseContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NEXT PERIOD ESTIMATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = rosePrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val estimateText = stats.nextEstimatedStartDate?.format(
                    DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
                ) ?: "Add logs to calculate"

                Text(
                    text = estimateText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2C1D21)
                )

                stats.daysUntilNextPeriod?.let { days ->
                    Spacer(modifier = Modifier.height(8.dp))
                    val statusText = when {
                        days > 1 -> "In $days days"
                        days == 1L -> "Tomorrow"
                        days == 0L -> "Expected Today"
                        else -> "Overdue by ${-days} days"
                    }
                    Text(
                        text = statusText,
                        modifier = Modifier
                            .background(rosePrimary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Secondary Stats Row (Median Cycle & Average Period Duration)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Median Cycle Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Median Cycle",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val medianStr = stats.medianCycleLengthDays?.let {
                        "${String.format("%.1f", it)} days"
                    } ?: "No data"
                    Text(
                        text = medianStr,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                    )
                }
            }

            // Average Period Duration Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Avg Period Length",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val avgStr = stats.averagePeriodLengthDays?.let {
                        "${String.format("%.1f", it)} days"
                    } ?: "No data"
                    Text(
                        text = avgStr,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAD1457)
                        )
                    )
                }
            }
        }
    }
}
