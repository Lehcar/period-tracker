package com.example.periodtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.periodtracker.data.db.PeriodTrackerDbHelper
import com.example.periodtracker.data.repository.PeriodRepositoryImpl
import com.example.periodtracker.ui.PeriodTrackerScreen
import com.example.periodtracker.ui.PeriodViewModel
import com.example.periodtracker.ui.PeriodViewModelFactory
import com.example.periodtracker.ui.theme.PeriodTrackerTheme

class MainActivity : ComponentActivity() {

    private val dbHelper by lazy { PeriodTrackerDbHelper(applicationContext) }
    private val repository by lazy { PeriodRepositoryImpl(dbHelper) }
    private val viewModel: PeriodViewModel by viewModels {
        PeriodViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeriodTrackerTheme {
                PeriodTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
