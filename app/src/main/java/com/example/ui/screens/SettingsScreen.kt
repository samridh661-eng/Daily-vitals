package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleepIndigo
import com.example.ui.theme.StepCountGreen
import com.example.ui.theme.WaterBlue
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun SettingsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val notificationsAllowed by viewModel.notificationsAllowed.collectAsState()

    var stepsGoalInput by remember(settings.stepsGoal) { mutableStateOf(settings.stepsGoal.toString()) }
    var waterGoalInput by remember(settings.waterGoal) { mutableStateOf(settings.waterGoal.toString()) }
    var sleepGoalHoursInput by remember(settings.sleepGoalMinutes) {
        mutableStateOf(String.format("%.1f", settings.sleepGoalMinutes / 60f))
    }

    var sleepStartHour by remember(settings.sleepStartHour) { mutableStateOf(settings.sleepStartHour.toString()) }
    var sleepStartMin by remember(settings.sleepStartMinute) { mutableStateOf(settings.sleepStartMinute.toString()) }
    var sleepEndHour by remember(settings.sleepEndHour) { mutableStateOf(settings.sleepEndHour.toString()) }
    var sleepEndMin by remember(settings.sleepEndMinute) { mutableStateOf(settings.sleepEndMinute.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("dashboard") },
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Daily Goals Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Daily Target Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Steps Target
                OutlinedTextField(
                    value = stepsGoalInput,
                    onValueChange = { stepsGoalInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Steps Target") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = StepCountGreen
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("steps_goal_input"),
                    singleLine = true
                )

                // Water Target
                OutlinedTextField(
                    value = waterGoalInput,
                    onValueChange = { waterGoalInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Water Target (ml)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = WaterBlue
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("water_goal_input"),
                    singleLine = true
                )

                // Sleep Target
                OutlinedTextField(
                    value = sleepGoalHoursInput,
                    onValueChange = { sleepGoalHoursInput = it },
                    label = { Text("Target Sleep Hours") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = SleepIndigo
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("sleep_goal_input"),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val steps = stepsGoalInput.toIntOrNull() ?: 8000
                        val water = waterGoalInput.toIntOrNull() ?: 2000
                        val sleepHours = sleepGoalHoursInput.toFloatOrNull() ?: 7.5f
                        val sleepMins = (sleepHours * 60).toInt()

                        viewModel.updateStepsGoal(steps)
                        viewModel.updateWaterGoal(water)
                        viewModel.updateSleepGoal(sleepMins)

                        Toast.makeText(context, "Goals updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_goals_btn")
                ) {
                    Text("Apply Goals")
                }
            }
        }

        // Quiet Hours & Sleep Schedule Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Scheduled Sleep Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleepIndigo
                )

                Text(
                    text = "DailyVitals will not nudge or play notifications during these hours to ensure undisturbed rest.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sleepStartHour,
                        onValueChange = { sleepStartHour = it.filter { char -> char.isDigit() } },
                        label = { Text("Bed Hour (0-23)") },
                        modifier = Modifier.weight(1f).testTag("sleep_start_hour_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = sleepStartMin,
                        onValueChange = { sleepStartMin = it.filter { char -> char.isDigit() } },
                        label = { Text("Bed Minute (0-59)") },
                        modifier = Modifier.weight(1f).testTag("sleep_start_min_input"),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sleepEndHour,
                        onValueChange = { sleepEndHour = it.filter { char -> char.isDigit() } },
                        label = { Text("Wake Hour (0-23)") },
                        modifier = Modifier.weight(1f).testTag("sleep_end_hour_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = sleepEndMin,
                        onValueChange = { sleepEndMin = it.filter { char -> char.isDigit() } },
                        label = { Text("Wake Minute (0-59)") },
                        modifier = Modifier.weight(1f).testTag("sleep_end_min_input"),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        val sH = sleepStartHour.toIntOrNull()?.coerceIn(0, 23) ?: 22
                        val sM = sleepStartMin.toIntOrNull()?.coerceIn(0, 59) ?: 30
                        val eH = sleepEndHour.toIntOrNull()?.coerceIn(0, 23) ?: 7
                        val eM = sleepEndMin.toIntOrNull()?.coerceIn(0, 59) ?: 0

                        viewModel.updateSleepSchedule(sH, sM, eH, eM)
                        Toast.makeText(context, "Quiet Sleep hours updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleepIndigo),
                    modifier = Modifier.fillMaxWidth().testTag("save_schedule_btn")
                ) {
                    Text("Save Sleep Schedule")
                }
            }
        }

        // Reminders & Opt-In Smart Rules Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reminders & Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Opt-in notifications toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Water & Step Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Smart reminders to hydrate and nudge walking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = notificationsAllowed,
                        onCheckedChange = { allowed ->
                            if (allowed) {
                                // Navigate to permission opt-in explanation screen
                                viewModel.navigateTo("permissions")
                            } else {
                                viewModel.setNotificationsAllowed(false)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("notification_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                // Accelerometer Sleep Estimation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Accelerometer Sleep Estimation",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Log sleep implicitly by analyzing device orientation and immobility.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = settings.accelerometerSleepEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.toggleAccelerometerSleep(enabled)
                            Toast.makeText(context, "Accelerometer sleep mode: " + if (enabled) "ON" else "OFF", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SleepIndigo),
                        modifier = Modifier.testTag("sleep_estimation_switch")
                    )
                }
            }
        }

        // About the Creator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DailyVitals Creator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "samridh singhal",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "A wellness application designed with peaceful aesthetics. Focuses on gentle progress and clear daily insights without guilt patterns or intrusive alerts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
