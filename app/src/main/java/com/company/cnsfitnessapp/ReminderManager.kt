package com.company.cnsfitnessapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Manages scheduling and canceling fitness reminders using Android's AlarmManager.
 * This is a singleton object.
 */
object ReminderManager {

    private const val REQUEST_CODE = 100
    private const val CHANNEL_ID = "fitness_reminder_channel"
    private const val CHANNEL_NAME = "Fitness Reminders"
    private const val CHANNEL_DESCRIPTION = "Reminders to move or hydrate"
    private const val TAG = "REMINDER_MANAGER"

    private lateinit var appContext: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var reminderPendingIntent: PendingIntent

    /**
     * Initializes the ReminderManager with the application context.
     * This must be called once, typically in your Application class or MainActivity.
     *
     * @param context The application context.
     */
    fun init(context: Context) {
        if (this::appContext.isInitialized && appContext == context.applicationContext) {
            Log.w(TAG, "⚠️ ReminderManager already initialized with the same context.")
            return
        }
        appContext = context.applicationContext
        alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel (required for Android 8.0+)
        createNotificationChannel()

        val intent = Intent(appContext, ReminderReceiver::class.java)
        reminderPendingIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(TAG, "✅ ReminderManager initialized.")
    }

    /**
     * Creates a notification channel for fitness reminders.
     * This is required for Android 8.0 (Oreo) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created.")
        }
    }

    /**
     * Starts fitness reminders.
     * Reads reminder time from SettingsManager and schedules a repeating alarm.
     *
     * @param context The context from which to show toasts.
     */
    fun startReminders(context: Context) {
        if (!this::appContext.isInitialized) {
            Log.e(TAG, "❌ ReminderManager not initialized. Call init() first.")
            Toast.makeText(context, "Error: Reminder manager not initialized.", Toast.LENGTH_SHORT).show()
            return
        }

        stopReminders(context)

        SettingsManager.getReminderTime() // e.g., "18:00"

        val testFirstDelay = 10_000L
        val intervalMillis = AlarmManager.INTERVAL_HOUR * 2

        val finalTrigger = System.currentTimeMillis() + testFirstDelay

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            finalTrigger,
            intervalMillis,
            reminderPendingIntent
        )

        val scheduledDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(finalTrigger), ZoneId.systemDefault())
        } else {
            // Provide a less specific log for older devices
            "Time: $finalTrigger"
        }

        Log.d(TAG, "🔔 Reminders scheduled: first at $scheduledDateTime, then every ${intervalMillis / (1000 * 60 * 60)} hours.")
        Toast.makeText(context, "Fitness reminders started!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Stops any currently scheduled fitness reminders.
     *
     * @param context The context from which to show toasts.
     */
    fun stopReminders(context: Context) {
        if (!this::alarmManager.isInitialized) {
            Log.e(TAG, "❌ ReminderManager not initialized. Cannot stop reminders.")
            Toast.makeText(context, "Error: Reminder manager not initialized.", Toast.LENGTH_SHORT).show()
            return
        }
        alarmManager.cancel(reminderPendingIntent)
        Log.d(TAG, "🔕 Reminders stopped.")
        Toast.makeText(context, "Fitness reminders stopped.", Toast.LENGTH_SHORT).show()
    }

    fun isInitialized(): Boolean = this::appContext.isInitialized
}
