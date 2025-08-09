package com.company.cnsfitnessapp // CORRECTED: Package name is all lowercase 'cnsfitnessapp'

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.FirebaseFirestore

// Project-specific imports (all corrected to lowercase package)


object DataLogger {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private const val LOG_FILE_NAME = "fitness_log.txt"
    private const val TAG = "DataLogger"

    // WARNING: Storing ChartPanelWrapper as a static field can lead to memory leaks
    // if ChartPanelWrapper itself holds an Activity context. Consider:
    // 1. Passing the wrapper as a parameter to methods that need it.
    // 2. Using a WeakReference if it MUST be stored statically.
    // 3. Refactoring DataLogger to be a class whose instances are lifecycle-aware.
    private var chartPanelWrapper: ChartPanelWrapper? = null
    private var appContext: Context? = null // Storing application context is generally safer for singletons

    /**
     * Initializes DataLogger with application context and the Firestore instance for RemoteSyncManager.
     * This must be called once at application startup.
     *
     * @param context The application context.
     * @param firestoreInstance The FirebaseFirestore instance to use for cloud synchronization.
     */
    fun init(context: Context, firestoreInstance: FirebaseFirestore) {
        // Only initialize if not already initialized
        if (appContext == null) {
            appContext = context.applicationContext
            // Initialize RemoteSyncManager here as well, as DataLogger depends on it
            RemoteSyncManager.initializeFirebase(firestoreInstance)
            Log.d(TAG, "DataLogger initialized.")
        } else {
            Log.w(TAG, "DataLogger already initialized.")
        }
    }

    /**
     * Sets the ChartPanelWrapper for real-time chart updates.
     * WARNING: Be mindful of potential memory leaks if ChartPanelWrapper holds an Activity context.
     *
     * @param wrapper The ChartPanelWrapper instance.
     */
    fun setChartPanelWrapper(wrapper: ChartPanelWrapper) {
        chartPanelWrapper = wrapper
        Log.d(TAG, "ChartPanelWrapper set.")
    }

    /**
     * Logs a metric for the current user.
     * Automatically gets the current user from UserProfileManager.
     *
     * @param label The label for the metric (e.g., "step", "calories").
     * @param value The integer value of the metric.
     */
    fun log(label: String, value: Int) {
        val user = UserProfileManager.getCurrentUser()
        log(user, label, value) // Delegate to the overloaded function
    }

    /**
     * Logs a metric for a specific user.
     * Handles logging to SQLite, Firebase (if enabled), local file, AI engine, and chart updates.
     *
     * @param user The username for whom the metric is being logged.
     * @param label The label for the metric.
     * @param value The integer value of the metric.
     */
    fun log(user: String, label: String, value: Int) {
        val timestamp = formatter.format(LocalDateTime.now())
        val logEntry = "[$user] $timestamp - ${label.uppercase()}: $value"
        Log.d(TAG, "Processing log: $logEntry")

        // 1. Log to SQLite
        try {
            DatabaseLogger.log(user, label, value) // DatabaseLogger.log is expected to take Int for value
            Log.d(TAG, "DB log successful for $label: $value")
        } catch (e: Exception) {
            Log.e(TAG, "❌ DB log failed: ${e.message}", e)
        }

        // 2. Firebase sync (if enabled) - **DELEGATE TO REMOTESYNCMANAGER**
        if (SettingsManager.isCloudFirstEnabled()) {
            try {
                if (RemoteSyncManager.isInitialized()) {
                    // RemoteSyncManager.logToFirebase is assumed to handle the 'user' if needed
                    RemoteSyncManager.logToFirebase(label, value) // RemoteSyncManager.logToFirebase is expected to take Int for value
                    Log.d(TAG, "Firebase sync initiated for $label: $value")
                } else {
                    Log.w(TAG, "⚠️ RemoteSyncManager not initialized. Cannot sync to Firebase.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Firebase sync failed via RemoteSyncManager: ${e.message}", e)
            }
        }

        // 3. Local file log
        try {
            appContext?.let { context ->
                val file = File(context.filesDir, LOG_FILE_NAME)
                FileWriter(file, true).use { writer -> // 'true' for append mode
                    writer.appendLine(logEntry)
                }
                Log.d(TAG, "Local file log successful to ${file.absolutePath}")
            } ?: Log.e(TAG, "App context is null, cannot log to file.")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Local file log failed: ${e.message}", e)
        }

        // 4. Forward to AI engine
        try {
            AISuggestionEngine.process(label, value.toDouble())
            Log.d(TAG, "AI engine processed $label: $value")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ AI engine error: ${e.message}", e)
        }

        // 5. Realtime chart update
        if (label.equals("step", ignoreCase = true)) {
            // CORRECTED: Pass value as Int, as ChartPanelWrapper.incrementTodayStepCount MUST be expecting an Int now
            chartPanelWrapper?.incrementTodayStepCount(value) // This is line 129
            Log.d(TAG, "ChartPanelWrapper step incremented by $value (as Int).")
        }

        // Optional UI toast
        appContext?.let {
            Toast.makeText(it, "Logged $label: $value", Toast.LENGTH_SHORT).show()
        }
    }

    // Function "logStep" is never used - placeholder for direct step logging
    fun logStep() {
        val currentUser = UserProfileManager.getCurrentUser()
        try {
            DatabaseLogger.log(currentUser, "step", 1) // Log single step to DB
            // CORRECTED: Pass 1 as Int, as ChartPanelWrapper.incrementTodayStepCount MUST be expecting an Int now
            chartPanelWrapper?.incrementTodayStepCount(1) // This is line 154 (or close to it depending on formatting)
            appContext?.let {
                Toast.makeText(it, "Logged 1 step for $currentUser.", Toast.LENGTH_SHORT).show()
            }
            Log.d(TAG, "$currentUser logged 1 step.")
        } catch (e: Exception) {
            Log.e(TAG, "Step log error: ${e.message}", e)
        }
    }

    // Function "logStepData" is never used - placeholder for logging bulk data
    fun logStepData(date: LocalDate, steps: Int, calories: Int) {
        val user = UserProfileManager.getCurrentUser()
        Log.d(TAG, "Logging bulk step data for $user on $date: steps=$steps, calories=$calories")

        log(user, "step", steps) // This is line 138 (or close to it)
        log(user, "calories", calories)
    }
}