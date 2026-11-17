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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.PeriodStats
import com.example.periodtracker.domain.PeriodAnalytics
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsScreenContent(
    stats: PeriodStats,
    dailyLogs: List<DailyLog>,
    modifier: Modifier = Modifier
) {
    val cardBg = Color(0xFFFFF0F5)
    val cyclesHistory = PeriodAnalytics.extractPeriodCycles(dailyLogs)
        .sortedByDescending { it.startDate }
        .take(5)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title
        Text(
            text = "Cycle Insights & Statistics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color(0xFF2C1D21)
        )

        // Estimated Start Date Hero Card
        PeriodEstimateCard(stats = stats)

        // Primary Stats Cards Row (Median Cycle & Avg Duration)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Median Cycle",
                value = stats.medianCycleLengthDays?.let { "${String.format("%.1f", it)} days" } ?: "No data",
                valueColor = Color(0xFF4A148C),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Avg Period Length",
                value = stats.averagePeriodLengthDays?.let { "${String.format("%.1f", it)} days" } ?: "No data",
                valueColor = Color(0xFFAD1457),
                modifier = Modifier.weight(1f)
            )
        }

        // Secondary Stats Row (Total Cycles & Total Logged Days)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Cycles Logged",
                value = "${stats.totalCyclesCount}",
                valueColor = Color(0xFF00695C),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Total Period Days",
                value = "${stats.totalRecordedDaysCount}",
                valueColor = Color(0xFFD81B60),
                modifier = Modifier.weight(1f)
            )
        }

        // Period Cycle History Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Past 5 Cycles History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C1D21)
                )

                if (cyclesHistory.isEmpty()) {
                    Text(
                        text = "No period cycles recorded yet. Log dates in the calendar to see your history.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    cyclesHistory.forEachIndexed { index, cycle ->
                        if (index > 0) {
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val startStr = cycle.startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                val endStr = cycle.endDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                val rangeStr = if (cycle.startDate == cycle.endDate) startStr else "$startStr – $endStr"

                                Text(
                                    text = rangeStr,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2C1D21)
                                )
                            }

                            Text(
                                text = "${cycle.durationDays} ${if (cycle.durationDays == 1L) "day" else "days"}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE91E63),
                                modifier = Modifier
                                    .background(Color(0xFFFCE4EC), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            )
        }
    }
}
