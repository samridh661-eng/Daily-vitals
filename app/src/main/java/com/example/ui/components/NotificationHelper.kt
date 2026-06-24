package com.example.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.model.AppSettings
import com.example.data.model.DailyRecord
import java.util.Calendar

object NotificationHelper {
    private const val CHANNEL_WATER_ID = "daily_vitals_water"
    private const val CHANNEL_STEPS_ID = "daily_vitals_steps"
    private const val WATER_NOTIFICATION_ID = 1001
    private const val STEPS_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameWater = "Water Reminders"
            val descWater = "Reminders to log and meet your water intake goal"
            val importanceWater = NotificationManager.IMPORTANCE_DEFAULT
            val channelWater = NotificationChannel(CHANNEL_WATER_ID, nameWater, importanceWater).apply {
                description = descWater
            }

            val nameSteps = "Step Reminders"
            val descSteps = "Nudges to reach your daily walking goals"
            val importanceSteps = NotificationManager.IMPORTANCE_DEFAULT
            val channelSteps = NotificationChannel(CHANNEL_STEPS_ID, nameSteps, importanceSteps).apply {
                description = descSteps
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelWater)
            notificationManager.createNotificationChannel(channelSteps)
        }
    }

    // Smart evaluation check for reminders
    fun shouldTriggerWaterReminder(record: DailyRecord, settings: AppSettings): ReminderResult {
        if (!settings.waterRemindersEnabled) {
            return ReminderResult(false, "Reminders are disabled in settings")
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Rule 1: Do not trigger during sleep hours
        val isSleepTime = isTimeInSleepSchedule(
            currentHour, currentMinute,
            settings.sleepStartHour, settings.sleepStartMinute,
            settings.sleepEndHour, settings.sleepEndMinute
        )
        if (isSleepTime) {
            return ReminderResult(false, "Currently within scheduled sleep hours")
        }

        // Rule 2: Smart nudge: < 50% of water intake by 3 PM
        if (currentHour >= 15 && record.water < (record.waterGoal * 0.5f)) {
            return ReminderResult(true, "It's past 3 PM and you are under 50% of your water goal. Let's drink some water!")
        }

        // Rule 3: Smart nudge: No log in 2 hours during waking hours (simulated as active hourly nudge)
        // Since we don't store individual timestamps for logs in the MVP entity,
        // we nudge if water is below goal and it's active waking hours.
        if (record.water < record.waterGoal) {
            return ReminderResult(true, "Stay refreshed! Time to hydrate and log another cup of water.")
        }

        return ReminderResult(false, "Water intake goals are already met for today!")
    }

    private fun isTimeInSleepSchedule(
        h: Int, m: Int,
        startH: Int, startM: Int,
        endH: Int, endM: Int
    ): Boolean {
        val currentMins = h * 60 + m
        val startMins = startH * 60 + startM
        val endMins = endH * 60 + endM

        return if (startMins < endMins) {
            // Sleep window is within same calendar day (e.g., 1 PM to 3 PM)
            currentMins in startMins..endMins
        } else {
            // Sleep window crosses midnight (e.g., 10 PM to 7 AM)
            currentMins >= startMins || currentMins <= endMins
        }
    }

    fun triggerWaterNotification(context: Context, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            android.app.PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_WATER_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Fallback drawable
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(WATER_NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Missing notification permission on API 33+
        }
    }

    fun triggerStepsNotification(context: Context, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            android.app.PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, flags)

        val builder = NotificationCompat.Builder(context, CHANNEL_STEPS_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(STEPS_NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Missing notification permission on API 33+
        }
    }

    data class ReminderResult(val shouldRemind: Boolean, val message: String)
}
