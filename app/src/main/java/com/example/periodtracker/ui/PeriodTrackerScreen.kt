package com.example.periodtracker.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.periodtracker.data.model.FlowIntensity
import com.example.periodtracker.ui.components.DailyFlowDialog
import com.example.periodtracker.ui.components.MonthCalendarView
import com.example.periodtracker.ui.components.PeriodEstimateCard
import com.example.periodtracker.ui.components.StatisticsScreenContent
import com.example.periodtracker.ui.components.WipeDataConfirmDialog
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(
    viewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(viewModel.getExportCsvData().toByteArray())
                }
                Toast.makeText(context, "Exported period data to CSV successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().use { it.readText() }
                }
                if (csvContent != null) {
                    viewModel.importCsvContent(csvContent) { count ->
                        Toast.makeText(context, "Successfully imported $count entries from CSV!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to import CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Period Tracker",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color(0xFFE91E63),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )

                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    label = { Text("Calendar", fontWeight = FontWeight.Bold) },
                    selected = (uiState.currentTab == TrackerTab.CALENDAR),
                    onClick = {
                        viewModel.selectTab(TrackerTab.CALENDAR)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFFCE4EC),
                        selectedIconColor = Color(0xFFE91E63),
                        selectedTextColor = Color(0xFFE91E63)
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Statistics", fontWeight = FontWeight.Bold) },
                    selected = (uiState.currentTab == TrackerTab.STATISTICS),
                    onClick = {
                        viewModel.selectTab(TrackerTab.STATISTICS)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFFCE4EC),
                        selectedIconColor = Color(0xFFE91E63),
                        selectedTextColor = Color(0xFFE91E63)
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Export to CSV", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        exportLauncher.launch("period_tracker_logs.csv")
                    },
                    icon = { Icon(imageVector = Icons.Default.Share, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Import from CSV", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        importLauncher.launch("*/*")
                    },
                    icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (uiState.currentTab == TrackerTab.CALENDAR) "Calendar" else "Statistics",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C1D21)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = Color(0xFF2C1D21)
                            )
                        }
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

                when (uiState.currentTab) {
                    TrackerTab.CALENDAR -> {
                        // View 1: Calendar View (Period Estimate + Calendar + Selected Day Logging)
                        PeriodEstimateCard(stats = uiState.stats)

                        MonthCalendarView(
                            displayedMonth = uiState.displayedMonth,
                            selectedDate = uiState.selectedDate,
                            logs = uiState.dailyLogs,
                            onDateSelected = { date -> viewModel.openLogForDate(date) },
                            onMonthChanged = { delta -> viewModel.changeMonth(delta) },
                            onGoToToday = { viewModel.goToToday() }
                        )

                        SelectedDayCard(
                            uiState = uiState,
                            onLogClicked = { viewModel.openLogForDate(uiState.selectedDate) }
                        )
                    }

                    TrackerTab.STATISTICS -> {
                        // View 2: Separate Statistics View
                        StatisticsScreenContent(
                            stats = uiState.stats,
                            dailyLogs = uiState.dailyLogs
                        )
                    }
                }

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
