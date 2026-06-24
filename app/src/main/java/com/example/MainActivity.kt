package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.NotificationHelper
import com.example.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: HealthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Setup Oreo+ Notification channels on startup
    NotificationHelper.createNotificationChannels(this)

    setContent {
      MyApplicationTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          AppNavigationRouter(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.registerStepSensor()
    viewModel.refreshStreaks()
  }

  override fun onPause() {
    super.onPause()
    viewModel.unregisterStepSensor()
  }
}

@Composable
fun AppNavigationRouter(
  viewModel: HealthViewModel,
  modifier: Modifier = Modifier
) {
  val currentScreen by viewModel.currentScreen.collectAsState()

  when (currentScreen) {
    "dashboard" -> DashboardScreen(
      viewModel = viewModel,
      modifier = modifier
    )
    "settings" -> SettingsScreen(
      viewModel = viewModel,
      modifier = modifier
    )
    "permissions" -> PermissionsScreen(
      viewModel = viewModel,
      modifier = modifier
    )
    else -> DashboardScreen(
      viewModel = viewModel,
      modifier = modifier
    )
  }
}
