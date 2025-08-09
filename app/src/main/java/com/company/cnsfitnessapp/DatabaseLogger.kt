// File: app/src/main/java/com/company/cnsfitnessapp/DatabaseLogger.kt
package com.company.cnsfitnessapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.util.Log

// Import the newly defined LogEntry data class

/**
 * Handles database operations for logging fitness data to an SQLite database
 * using Android's SQLiteOpenHelper.
 *
 * This class must be initialized with an application context via init() before use.
 */
object DatabaseLogger {

    private const val DATABASE_NAME = "fitness.db"
    private const val DATABASE_VERSION = 1
    private const val TABLE_LOGS = "logs"

    // Column names
    private const val COL_USER = "user"
    private const val COL_LABEL = "label"
    private const val COL_VALUE = "value"
    private const val COL_TIMESTAMP = "timestamp"

    private lateinit var dbHelper: DatabaseHelper
    private const val TAG = "DatabaseLogger"

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            val createLogsTable = """
                CREATE TABLE $TABLE_LOGS (
                    $COL_USER TEXT NOT NULL,
                    $COL_LABEL TEXT NOT NULL,
                    $COL_VALUE INTEGER NOT NULL,
                    $COL_TIMESTAMP TEXT DEFAULT CURRENT_TIMESTAMP
                );
            """
            db.execSQL(createLogsTable)
            Log.d(TAG, "✅ Logs table created.")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
            onCreate(db)
            Log.d(TAG, "⬆️ Database upgraded (table dropped and recreated).")
        }
    }

    fun init(context: Context) {
        if (!this::dbHelper.isInitialized) {
            dbHelper = DatabaseHelper(context.applicationContext)
            Log.d(TAG, "✅ DatabaseHelper initialized.")
        } else {
            Log.w(TAG, "⚠️ DatabaseHelper already initialized.")
        }
    }

    fun log(user: String, label: String, value: Int) {
        if (!this::dbHelper.isInitialized) {
            Log.e(TAG, "❌ DatabaseHelper not initialized. Call init() first.")
            return
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COL_USER, user)
            put(COL_LABEL, label)
            put(COL_VALUE, value)
        }

        try {
            val newRowId = db.insert(TABLE_LOGS, null, values)
            if (newRowId != -1L) {
                Log.d(TAG, "✅ Log saved (row ID: $newRowId): $user - $label: $value")
            } else {
                Log.e(TAG, "❌ Failed to insert row: $user - $label: $value")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to log data: ${e.message}", e)
        } finally {
            db.close()
        }
    }

}