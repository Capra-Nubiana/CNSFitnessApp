package com.company.cnsfitnessapp // VERIFY: This must match your actual folder structure (com/company/cnsfitnessapp)

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

/**
 * Manages application settings, storing them in a properties file.
 *
 * NOTE: For Android applications, it is generally recommended to use
 * SharedPreferences or Jetpack DataStore for managing user preferences
 * and application settings, as they provide better lifecycle management,
 * thread safety, and integration with the Android framework.
 * This implementation directly translates the file-based approach from Java.
 *
 * To use this effectively in Android, you'll need to initialize it with an
 * application context to get the correct file path for internal storage.
 * e.g., SettingsManager.init(applicationContext)
 */
object SettingsManager {
    private const val CONFIG_FILE_NAME = "fitness_settings.properties"
    private val props = Properties()
    // Changed to lateinit var, requires init() to be called before access.
    private lateinit var appContext: Context
    private const val TAG = "SettingsManager" // ADDED: 'const' as suggested, good practice for TAGs

    /**
     * Initializes the SettingsManager with an application context.
     * This method MUST be called once, typically in your Application class or MainActivity,
     * before any other SettingsManager methods are used.
     *
     * @param context The application context.
     */
    fun init(context: Context) {
        // Check if already initialized with the same context to prevent unnecessary re-initialization
        if (this::appContext.isInitialized && appContext == context.applicationContext) {
            Log.w(TAG, "⚠️ SettingsManager already initialized with the same context.")
            return
        }
        appContext = context.applicationContext
        loadSettings()
    }

    /**
     * Helper to get the config file.
     * Throws IllegalStateException if not initialized to enforce init() call.
     */
    private fun getConfigFile(): File {
        // Enforce initialization
        if (!this::appContext.isInitialized) {
            val errorMessage = "❌ SettingsManager not initialized. Call init() first."
            Log.e(TAG, errorMessage)
            throw IllegalStateException(errorMessage)
        }
        return File(appContext.filesDir, CONFIG_FILE_NAME) // Use internal storage
    }

    // Load properties from file
    private fun loadSettings() {
        try {
            val configFile = getConfigFile() // This will now throw if not initialized
            if (configFile.exists()) {
                FileInputStream(configFile).use { input ->
                    props.load(input)
                    Log.d(TAG, "✅ Settings loaded successfully from ${configFile.absolutePath}.")
                }
            } else {
                Log.i(TAG, "ℹ No settings file found at ${configFile.absolutePath}, using defaults.")
            }
        } catch (e: IllegalStateException) {
            // Logged by getConfigFile(), rethrow to propagate the initialization error
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "❌ Failed to load settings from ${getConfigFile().absolutePath}: ${e.message}", e)
            // If loading fails, it's good practice to clear properties
            // to ensure subsequent gets use defaults rather than potentially corrupted old values.
            props.clear()
            Log.w(TAG, "Properties cleared due to load failure. Using default settings.")
        }
    }

    // Save properties to file
    private fun saveSettings() {
        try {
            val configFile = getConfigFile() // This will now throw if not initialized
            FileOutputStream(configFile).use { output ->
                props.store(output, "Fitness App Settings")
                Log.d(TAG, "💾 Settings saved successfully to ${configFile.absolutePath}.")
            }
        } catch (e: IllegalStateException) {
            // Logged by getConfigFile(), rethrow to propagate the initialization error
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "❌ Failed to save settings to ${getConfigFile().absolutePath}: ${e.message}", e)
        }
    }

    /**
     * Checks if the SettingsManager has been initialized.
     * @return true if initialized, false otherwise.
     */
    fun isInitialized(): Boolean = this::appContext.isInitialized

    // ===== Cloud Sync Toggle =====
    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun isCloudFirstEnabled(): Boolean {
        // Safe access to properties with default value
        return props.getProperty("cloudFirst", "false").toBoolean()
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setCloudFirst(enabled: Boolean) {
        props.setProperty("cloudFirst", enabled.toString())
        Log.d(TAG, "☁ Cloud-first mode: ${if (enabled) "ON" else "OFF"}")
        saveSettings()
    }

    // ===== Dark Mode Toggle =====
    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun isDarkModeEnabled(): Boolean {
        return props.getProperty("darkMode", "false").toBoolean()
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setDarkModeEnabled(enabled: Boolean) {
        props.setProperty("darkMode", enabled.toString())
        Log.d(TAG, "🌙 Dark Mode: ${if (enabled) "ENABLED" else "DISABLED"}")
        saveSettings()
    }

    // ===== Step Goal =====
    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun getDailyStepGoal(): Int {
        // Use getOrDefault to avoid NumberFormatException if property is missing/invalid
        return props.getProperty("dailyStepGoal", "8000").toIntOrNull() ?: 8000
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setDailyStepGoal(goal: Int) {
        props.setProperty("dailyStepGoal", goal.toString())
        Log.d(TAG, "🎯 Daily Step Goal set to: $goal")
        saveSettings()
    }

    // ===== Reminder Time =====
    fun getReminderTime(): String {
        return props.getProperty("reminderTime", "18:00") // Default to 6PM
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setReminderTime(time: String) {
        props.setProperty("reminderTime", time)
        Log.d(TAG, "⏰ Reminder Time set to: $time")
        saveSettings()
    }

    // ===== Units Preference (Metric/Imperial) =====
    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun getUnitsPreference(): String {
        return props.getProperty("units", "metric")
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setUnitsPreference(units: String) {
        props.setProperty("units", units)
        Log.d(TAG, "📏 Units preference set to: $units")
        saveSettings()
    }

    // ===== Language (for localization support) =====
    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun getLanguage(): String {
        return props.getProperty("language", "en")
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setLanguage(lang: String) {
        props.setProperty("language", lang)
        saveSettings()
    }

    // ===== Generic Get/Set for future settings =====
    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun getSetting(key: String, defaultValue: String): String {
        return props.getProperty(key, defaultValue)
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun setSetting(key: String, value: String) {
        props.setProperty(key, value)
        saveSettings()
    }
}