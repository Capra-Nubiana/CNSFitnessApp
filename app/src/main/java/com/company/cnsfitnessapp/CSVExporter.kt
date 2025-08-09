// File: app/src/main/java/com/company/cnsfitnessapp/CSVExporter.kt
package com.company.cnsfitnessapp

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * An object to handle exporting user fitness logs to a CSV file.
 */
object CSVExporter {
    private const val TAG = "CSVExporter"

    /**
     * Exports all logs for a given user to a CSV file in the Downloads directory.
     *
     * @param context   The application context.
     * @param user      The user whose logs to export.
     * @param fileName  The name of the CSV file to create (e.g., "fitness_logs.csv").
     */
    fun exportUserLogs(context: Context, user: String, fileName: String) {
        // First, check if DatabaseLogger is initialized
        if (!DatabaseLogger.isInitialized) {
            Log.e(TAG, "DatabaseLogger is not initialized. Cannot export logs.")
            return
        }

        // Get all logs for the user (we are searching for a general keyword like "%" to get all)
        val logs = DatabaseLogger.searchLogs(user, "")

        // If no logs, do not create a file
        if (logs.isEmpty()) {
            Log.w(TAG, "No logs found for user '$user'. CSV file will not be created.")
            return
        }

        // Use the standard Downloads directory for the CSV file
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs() // Create the directory if it doesn't exist
        }

        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    // Write the CSV header
                    writer.write("timestamp,user,label,value\n")
                    // Write each log entry
                    logs.forEach { logEntry ->
                        writer.write("${logEntry.timestamp},${logEntry.user},${logEntry.label},${logEntry.value}\n")
                    }
                    writer.flush()
                }
            }
            Log.d(TAG, "Successfully exported logs to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export logs to CSV: ${e.message}", e)
        }
    }
}