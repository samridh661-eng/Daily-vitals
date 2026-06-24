package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.DailyRecord
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val repository = HealthRepository(application)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepSensor: Sensor? = null

    // For step counting offset calculations
    private var firstSensorValue: Float = -1f
    private var initialStepsInRecord: Int = 0

    // Streak counters state
    private val _stepsStreak = MutableStateFlow(0)
    val stepsStreak = _stepsStreak.asStateFlow()

    private val _waterStreak = MutableStateFlow(0)
    val waterStreak = _waterStreak.asStateFlow()

    private val _sleepStreak = MutableStateFlow(0)
    val sleepStreak = _sleepStreak.asStateFlow()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen = _currentScreen.asStateFlow()

    // Notification opt-in triggers (since MVP is opt-in)
    private val _notificationsAllowed = MutableStateFlow(false)
    val notificationsAllowed = _notificationsAllowed.asStateFlow()

    // Consolidate database flows
    val todayRecord: StateFlow<DailyRecord?> = repository.getTodayRecordFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .combine(MutableStateFlow(AppSettings())) { dbSettings, default ->
            dbSettings ?: default
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val recentRecords: StateFlow<List<DailyRecord>> = repository.getRecentRecordsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Seed sample history if database is empty so graphs look stunning on first launch
            repository.seedHistoryIfEmpty()
            // Make sure today's record is initialized
            repository.getOrCreateTodayRecord()
            // Sync streaks
            refreshStreaks()
        }

        // Initialize Step Counter Sensor
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            Log.d("DailyVitals", "Step counter sensor found and configured.")
        } else {
            Log.w("DailyVitals", "Step counter sensor not available on this device.")
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setNotificationsAllowed(allowed: Boolean) {
        _notificationsAllowed.value = allowed
        viewModelScope.launch {
            val s = settings.value.copy(
                waterRemindersEnabled = allowed,
                stepRemindersEnabled = allowed
            )
            repository.updateAppSettings(s)
        }
    }

    fun refreshStreaks() {
        viewModelScope.launch {
            val streaks = repository.calculateStreaks()
            _stepsStreak.value = streaks.stepsStreak
            _waterStreak.value = streaks.waterStreak
            _sleepStreak.value = streaks.sleepStreak
        }
    }

    // Step Operations
    fun registerStepSensor() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterStepSensor() {
        sensorManager.unregisterListener(this)
        firstSensorValue = -1f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceReboot = event.values[0]
            if (firstSensorValue < 0) {
                firstSensorValue = totalStepsSinceReboot
                viewModelScope.launch {
                    val today = repository.getOrCreateTodayRecord()
                    initialStepsInRecord = today.steps
                }
            } else {
                val delta = (totalStepsSinceReboot - firstSensorValue).toInt()
                if (delta > 0) {
                    viewModelScope.launch {
                        repository.updateTodaySteps(initialStepsInRecord + delta)
                        refreshStreaks()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    fun addSimulatedSteps(amount: Int) {
        viewModelScope.launch {
            repository.incrementTodaySteps(amount)
            refreshStreaks()
        }
    }

    fun updateStepsGoal(goal: Int) {
        viewModelScope.launch {
            val s = settings.value.copy(stepsGoal = goal)
            repository.updateAppSettings(s)
            refreshStreaks()
        }
    }

    // Water Operations
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(amountMl)
            refreshStreaks()
        }
    }

    fun resetTodayWater() {
        viewModelScope.launch {
            repository.resetWater()
            refreshStreaks()
        }
    }

    fun updateWaterGoal(goalMl: Int) {
        viewModelScope.launch {
            val s = settings.value.copy(waterGoal = goalMl)
            repository.updateAppSettings(s)
            refreshStreaks()
        }
    }

    // Sleep Operations
    fun logSleep(bedTime: String, wakeTime: String) {
        viewModelScope.launch {
            repository.logSleep(bedTime, wakeTime)
            refreshStreaks()
        }
    }

    fun clearTodaySleep() {
        viewModelScope.launch {
            repository.deleteTodaySleep()
            refreshStreaks()
        }
    }

    fun updateSleepGoal(goalMinutes: Int) {
        viewModelScope.launch {
            val s = settings.value.copy(sleepGoalMinutes = goalMinutes)
            repository.updateAppSettings(s)
            refreshStreaks()
        }
    }

    fun updateSleepSchedule(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            val s = settings.value.copy(
                sleepStartHour = startHour,
                sleepStartMinute = startMinute,
                sleepEndHour = endHour,
                sleepEndMinute = endMinute
            )
            repository.updateAppSettings(s)
        }
    }

    fun toggleAccelerometerSleep(enabled: Boolean) {
        viewModelScope.launch {
            val s = settings.value.copy(accelerometerSleepEnabled = enabled)
            repository.updateAppSettings(s)
        }
    }
}
