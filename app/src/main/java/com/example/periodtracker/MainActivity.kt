package com.example.periodtracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.periodtracker.data.db.PeriodTrackerDbHelper
import com.example.periodtracker.data.preferences.PreferencesManager
import com.example.periodtracker.data.repository.PeriodRepositoryImpl
import com.example.periodtracker.ui.PeriodTrackerScreen
import com.example.periodtracker.ui.PeriodViewModel
import com.example.periodtracker.ui.PeriodViewModelFactory
import com.example.periodtracker.ui.theme.PeriodTrackerTheme

class MainActivity : ComponentActivity() {

    private val dbHelper by lazy { PeriodTrackerDbHelper(applicationContext) }
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }
    private val repository by lazy { PeriodRepositoryImpl(dbHelper) }
    private val viewModel: PeriodViewModel by viewModels {
        PeriodViewModelFactory(repository, preferencesManager)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (preferencesManager.notificationPreferences.value.enabled &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            PeriodTrackerTheme {
                PeriodTrackerScreen(viewModel = viewModel)
            }
        }
    }
}
