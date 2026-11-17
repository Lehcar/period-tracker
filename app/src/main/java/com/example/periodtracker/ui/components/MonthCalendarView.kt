package com.example.periodtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodtracker.data.model.DailyLog
import com.example.periodtracker.data.model.FlowIntensity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthCalendarView(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    logs: List<DailyLog>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (Long) -> Unit,
    onGoToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logsMap = logs.associateBy { it.date }
    val today = LocalDate.now()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calendar Header: Month/Year + Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF2C1D21)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onGoToToday) {
                        Text("Today", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { onMonthChanged(-1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month"
                        )
                    }
                    IconButton(onClick = { onMonthChanged(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of Week Header Row (Sun to Sat)
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                for (dayName in daysOfWeek) {
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month Grid Calculations
            val firstDayOfMonth = displayedMonth.atDay(1)
            // Sunday = 0, Monday = 1, ... Saturday = 6
            val firstDayOfWeekIndex = firstDayOfMonth.dayOfWeek.value % 7
            val totalDaysInMonth = displayedMonth.lengthOfMonth()

            var dayCounter = 1
            var currentWeek = 0

            while (dayCounter <= totalDaysInMonth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..6) {
                        if (currentWeek == 0 && col < firstDayOfWeekIndex) {
                            // Blank cell before month start
                            Spacer(modifier = Modifier.weight(1f))
                        } else if (dayCounter <= totalDaysInMonth) {
                            val date = displayedMonth.atDay(dayCounter)
                            val isSelected = (date == selectedDate)
                            val isToday = (date == today)
                            val log = logsMap[date]

                            CalendarDayCell(
                                date = date,
                                log = log,
                                isSelected = isSelected,
                                isToday = isToday,
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                            dayCounter++
                        } else {
                            // Blank cell after month end
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                currentWeek++
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Flow Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFFD81B60), label = "Heavy")
                LegendItem(color = Color(0xFFEC407A), label = "Medium")
                LegendItem(color = Color(0xFFF48FB1), label = "Light")
                LegendItem(color = Color(0xFFBA68C8), label = "Unknown")
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    log: DailyLog?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flowColor = when (log?.flowIntensity) {
        FlowIntensity.HEAVY -> Color(0xFFD81B60)
        FlowIntensity.MEDIUM -> Color(0xFFEC407A)
        FlowIntensity.LIGHT -> Color(0xFFF48FB1)
        FlowIntensity.UNKNOWN -> Color(0xFFBA68C8)
        FlowIntensity.NONE, null -> Color.Transparent
    }

    val isPeriodDay = log?.isPeriodDay == true
    val textColor = when {
        isPeriodDay -> Color.White
        isToday -> Color(0xFFE91E63)
        else -> Color(0xFF212121)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(flowColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color(0xFF8E24AA), CircleShape)
                } else if (isToday) {
                    Modifier.border(1.5.dp, Color(0xFFE91E63), CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected || isToday || isPeriodDay) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            ),
            color = textColor
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
