package com.example.periodtracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.periodtracker.data.model.FlowIntensity
import com.example.periodtracker.ui.components.DailyFlowDialog
import com.example.periodtracker.ui.components.MonthCalendarView
import com.example.periodtracker.ui.components.PeriodStatsHeader
import com.example.periodtracker.ui.components.WipeDataConfirmDialog
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(
    viewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Period Tracker",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1D21)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.requestWipeData() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete All Data",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF0F5)
                )
            )
        },
        containerColor = Color(0xFFFAF7F8)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Analytics / Stats Header Card
            PeriodStatsHeader(stats = uiState.stats)

            // Month Calendar View
            MonthCalendarView(
                displayedMonth = uiState.displayedMonth,
                selectedDate = uiState.selectedDate,
                logs = uiState.dailyLogs,
                onDateSelected = { date -> viewModel.openLogForDate(date) },
                onMonthChanged = { delta -> viewModel.changeMonth(delta) },
                onGoToToday = { viewModel.goToToday() }
            )

            // Selected Day Details & Quick Action Card
            SelectedDayCard(
                uiState = uiState,
                onLogClicked = { viewModel.openLogForDate(uiState.selectedDate) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dialog for Logging Daily Flow Intensity
        if (uiState.isLogDialogOpen) {
            DailyFlowDialog(
                selectedDate = uiState.selectedDate,
                currentLog = uiState.selectedLog,
                onSaveFlow = { flow -> viewModel.saveFlowForSelectedDate(flow) },
                onDeleteLog = { viewModel.deleteSelectedDateLog() },
                onDismiss = { viewModel.closeLogDialog() }
            )
        }

        // Confirmation Dialog for Wiping Data
        if (uiState.showWipeConfirmDialog) {
            WipeDataConfirmDialog(
                onConfirm = { viewModel.confirmWipeData() },
                onDismiss = { viewModel.dismissWipeDialog() }
            )
        }
    }
}

@Composable
private fun SelectedDayCard(
    uiState: PeriodUiState,
    onLogClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
    val log = uiState.selectedLog
    val flow = log?.flowIntensity ?: FlowIntensity.NONE

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C1D21)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val statusText = when (flow) {
                    FlowIntensity.HEAVY -> "🩸 Heavy flow recorded"
                    FlowIntensity.MEDIUM -> "🌺 Medium flow recorded"
                    FlowIntensity.LIGHT -> "🌸 Light flow recorded"
                    FlowIntensity.UNKNOWN -> "❓ Period flow recorded (intensity unknown)"
                    FlowIntensity.NONE -> "No period flow recorded for this day"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (flow != FlowIntensity.NONE) Color(0xFFE91E63) else Color.Gray
                )
            }

            Button(
                onClick = onLogClicked,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE4EC), contentColor = Color(0xFFE91E63))
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(if (flow != FlowIntensity.NONE) "Edit" else "Log Flow", fontWeight = FontWeight.Bold)
            }
        }
    }
}
