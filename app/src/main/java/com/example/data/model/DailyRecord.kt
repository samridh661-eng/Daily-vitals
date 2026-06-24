package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey
    val date: String, // format: "yyyy-MM-dd"
    val steps: Int = 0,
    val stepsGoal: Int = 8000,
    val water: Int = 0, // in ml
    val waterGoal: Int = 2000, // in ml
    val sleepMinutes: Int = 0,
    val sleepGoalMinutes: Int = 450, // 7.5 hours
    val bedTime: String? = null, // "HH:mm"
    val wakeTime: String? = null // "HH:mm"
) {
    val isStepsGoalMet: Boolean get() = steps >= stepsGoal
    val isWaterGoalMet: Boolean get() = water >= waterGoal
    val isSleepGoalMet: Boolean get() = sleepMinutes >= sleepGoalMinutes
}
