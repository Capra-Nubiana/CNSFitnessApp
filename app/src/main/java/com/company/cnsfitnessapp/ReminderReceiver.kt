package com.company.cnsfitnessapp

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
//noinspection SuspiciousImport
import android.R
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission

/**
 * A BroadcastReceiver that is triggered by the AlarmManager.
 * It's responsible for displaying a notification to the user.
 */
class ReminderReceiver : BroadcastReceiver() {

    // Unique ID for the notification
    private val NOTIFICATION_ID = 101
    private val TAG = "ReminderReceiver"

    // This channel ID must be the same as the one defined in ReminderManager
    private val CHANNEL_ID = "fitness_reminder_channel"

    override fun onReceive(context: Context?, intent: Intent?) {
        // Ensure context is not null before proceeding
        context?.let {
            // Check for POST_NOTIFICATIONS permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showNotification(it)
                } else {
                    // Log an error if permission is not granted
                    Log.e(TAG, "❌ Cannot post notification: POST_NOTIFICATIONS permission not granted.")
                }
            } else {
                // For older versions, permission is granted at install time, so we can proceed
                showNotification(it)
            }
        }
    }

    /**
     * Builds and displays the notification.
     * This function is only called if permissions are already granted.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(context: Context) {
        // Create an intent to launch your app's main activity when the user taps the notification
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("Time to Move!")
            .setContentText("A gentle reminder to get up and stretch or hydrate.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
