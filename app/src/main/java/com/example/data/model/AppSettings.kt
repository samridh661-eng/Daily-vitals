package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val stepsGoal: Int = 8000,
    val waterGoal: Int = 2000,
    val sleepGoalMinutes: Int = 450, // 7.5 hours
    val sleepStartHour: Int = 22, // Bedtime hour for sleep schedule
    val sleepStartMinute: Int = 30,
    val sleepEndHour: Int = 7, // Wake hour for sleep schedule
    val sleepEndMinute: Int = 0,
    val waterRemindersEnabled: Boolean = false,
    val stepRemindersEnabled: Boolean = false,
    val accelerometerSleepEnabled: Boolean = false
)
