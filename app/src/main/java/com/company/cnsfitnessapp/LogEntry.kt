package com.company.cnsfitnessapp

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

/**
 * A data class representing a single fitness log entry.
 * Firestore will automatically convert this to and from a document.
 */
data class LogEntry @RequiresApi(Build.VERSION_CODES.O) constructor(
    val date: String? = LocalDate.now(),
    val steps: String? = 0,
    val water: String? = 0,
    val calsBurned: Int = 0,
    val userId: String = ""
)
