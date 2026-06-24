package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettingsDao
import com.example.data.local.DailyRecordDao
import com.example.data.model.AppSettings
import com.example.data.model.DailyRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HealthRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val recordDao: DailyRecordDao = db.dailyRecordDao()
    private val settingsDao: AppSettingsDao = db.appSettingsDao()

    // Retrieve global settings. Flow returns default if none exist in the database.
    val settingsFlow: Flow<AppSettings?> = settingsDao.getSettingsFlow()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun getAppSettings(): AppSettings = withContext(Dispatchers.IO) {
        settingsDao.getSettingsSync() ?: AppSettings().also {
            settingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdate(settings)
        // Also update today's goals if today's record exists
        val today = getTodayDateString()
        val existingRecord = recordDao.getRecordForDateSync(today)
        if (existingRecord != null) {
            val updatedRecord = existingRecord.copy(
                stepsGoal = settings.stepsGoal,
                waterGoal = settings.waterGoal,
                sleepGoalMinutes = settings.sleepGoalMinutes
            )
            recordDao.insertOrUpdate(updatedRecord)
        }
    }

    suspend fun getOrCreateTodayRecord(): DailyRecord = withContext(Dispatchers.IO) {
        val today = getTodayDateString()
        val existing = recordDao.getRecordForDateSync(today)
        if (existing != null) {
            existing
        } else {
            val settings = getAppSettings()
            val newRecord = DailyRecord(
                date = today,
                steps = 0,
                stepsGoal = settings.stepsGoal,
                water = 0,
                waterGoal = settings.waterGoal,
                sleepMinutes = 0,
                sleepGoalMinutes = settings.sleepGoalMinutes
            )
            recordDao.insertOrUpdate(newRecord)
            newRecord
        }
    }

    fun getTodayRecordFlow(): Flow<DailyRecord?> {
        val today = getTodayDateString()
        return recordDao.getRecordForDate(today).flowOn(Dispatchers.IO)
    }

    fun getRecentRecordsFlow(limit: Int = 7): Flow<List<DailyRecord>> {
        return recordDao.getRecentRecords(limit).flowOn(Dispatchers.IO)
    }

    suspend fun updateTodaySteps(steps: Int) = withContext(Dispatchers.IO) {
        val todayRecord = getOrCreateTodayRecord()
        recordDao.updateSteps(todayRecord.date, steps)
    }

    suspend fun incrementTodaySteps(increment: Int) = withContext(Dispatchers.IO) {
        val todayRecord = getOrCreateTodayRecord()
        val newSteps = todayRecord.steps + increment
        recordDao.updateSteps(todayRecord.date, newSteps)
    }

    suspend fun addWater(amountMl: Int) = withContext(Dispatchers.IO) {
        val todayRecord = getOrCreateTodayRecord()
        val newWater = todayRecord.water + amountMl
        recordDao.updateWater(todayRecord.date, newWater)
    }

    suspend fun resetWater() = withContext(Dispatchers.IO) {
        val todayRecord = getOrCreateTodayRecord()
        recordDao.updateWater(todayRecord.date, 0)
    }

    suspend fun logSleep(bedTime: String, wakeTime: String) = withContext(Dispatchers.IO) {
        val todayRecord = getOrCreateTodayRecord()
        val minutes = calculateSleepMinutes(bedTime, wakeTime)
        recordDao.updateSleep(todayRecord.date, minutes, bedTime, wakeTime)
    }

    suspend fun deleteTodaySleep() = withContext(Dispatchers.IO) {
        val todayRecord = getOrCreateTodayRecord()
        recordDao.updateSleep(todayRecord.date, 0, null, null)
    }

    private fun calculateSleepMinutes(bedTime: String, wakeTime: String): Int {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val bedDate = sdf.parse(bedTime) ?: return 0
            val wakeDate = sdf.parse(wakeTime) ?: return 0

            var diffMillis = wakeDate.time - bedDate.time
            if (diffMillis <= 0) {
                // Slept across midnight (e.g., bedtime 22:00, wake 07:00)
                diffMillis += 24 * 60 * 60 * 1000 // Add one day
            }
            return (diffMillis / (1000 * 60)).toInt()
        } catch (e: Exception) {
            return 0
        }
    }

    // Seed historical dummy data to make the charts look useful and attractive immediately.
    suspend fun seedHistoryIfEmpty() = withContext(Dispatchers.IO) {
        val count = recordDao.getRecentRecords(10).firstOrNull()?.size ?: 0
        if (count < 2) {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val settings = getAppSettings()

            // Seed 7 days of history prior to today
            for (i in 1..7) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = sdf.format(calendar.time)

                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                // Introduce natural-looking variation in steps, water, sleep
                val stepsFactor = when (dayOfWeek) {
                    Calendar.SATURDAY, Calendar.SUNDAY -> 1.2
                    Calendar.WEDNESDAY -> 0.6
                    else -> 0.95
                }
                val waterFactor = when (dayOfWeek) {
                    Calendar.TUESDAY, Calendar.THURSDAY -> 0.8
                    else -> 1.1
                }
                val sleepFactor = when (dayOfWeek) {
                    Calendar.FRIDAY, Calendar.SATURDAY -> 1.15
                    Calendar.MONDAY -> 0.8
                    else -> 1.0
                }

                val steps = (settings.stepsGoal * stepsFactor).toInt() + (-400..600).random()
                val water = (settings.waterGoal * waterFactor).toInt() + (-300..400).random()
                val sleep = (settings.sleepGoalMinutes * sleepFactor).toInt() + (-60..90).random()

                val record = DailyRecord(
                    date = dateStr,
                    steps = steps.coerceAtLeast(0),
                    stepsGoal = settings.stepsGoal,
                    water = water.coerceAtLeast(0),
                    waterGoal = settings.waterGoal,
                    sleepMinutes = sleep.coerceAtLeast(0),
                    sleepGoalMinutes = settings.sleepGoalMinutes,
                    bedTime = "22:45",
                    wakeTime = "06:30"
                )
                recordDao.insertOrUpdate(record)
            }
        }
    }

    // Calculate goals met streaks
    suspend fun calculateStreaks(): StreakResult = withContext(Dispatchers.IO) {
        val allRecords = recordDao.getAllRecords().firstOrNull() ?: emptyList()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sortedRecords = allRecords.sortedByDescending { it.date }

        var stepsStreak = 0
        var waterStreak = 0
        var sleepStreak = 0

        // Calculate steps streak (current consecutive active streak starting from today or yesterday)
        val todayStr = getTodayDateString()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)

        var foundTodayOrYesterdayForSteps = false
        var currentStepsStreak = 0
        for (rec in sortedRecords) {
            if (rec.date == todayStr || rec.date == yesterdayStr) {
                foundTodayOrYesterdayForSteps = true
            }
            if (rec.isStepsGoalMet) {
                currentStepsStreak++
            } else {
                if (rec.date != todayStr) { // If today is not met yet, don't break the streak immediately if yesterday and before are met
                    break
                }
            }
        }
        stepsStreak = if (foundTodayOrYesterdayForSteps) currentStepsStreak else 0

        var currentWaterStreak = 0
        for (rec in sortedRecords) {
            if (rec.isWaterGoalMet) {
                currentWaterStreak++
            } else {
                if (rec.date != todayStr) break
            }
        }
        waterStreak = currentWaterStreak

        var currentSleepStreak = 0
        for (rec in sortedRecords) {
            if (rec.isSleepGoalMet) {
                currentSleepStreak++
            } else {
                if (rec.date != todayStr) break
            }
        }
        sleepStreak = currentSleepStreak

        StreakResult(stepsStreak, waterStreak, sleepStreak)
    }

    data class StreakResult(val stepsStreak: Int, val waterStreak: Int, val sleepStreak: Int)
}
