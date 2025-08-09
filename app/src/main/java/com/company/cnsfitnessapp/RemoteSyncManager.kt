package com.company.cnsfitnessapp // VERIFY: This must match your actual folder structure (com/company/cnsfitnessapp)

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object RemoteSyncManager {

    private var db: FirebaseFirestore? = null
    private const val TAG = "RemoteSyncManager"

    /**
     * Initializes Firebase Firestore with a provided instance.
     * Should be called once, ideally from the Application class or MainActivity after FirebaseApp is initialized.
     */
    fun initializeFirebase(firestoreInstance: FirebaseFirestore) {
        if (db != null) {
            Log.w(TAG, "⚠️ Firestore client already initialized.")
            return
        }
        db = firestoreInstance
        Log.d(TAG, "✅ Firestore client initialized successfully.")
    }

    @Suppress("unused") // Suppress warning if this function is not used internally in this file
    fun getFirestore(): FirebaseFirestore? = db

    fun isInitialized(): Boolean = db != null

    /**
     * Log a named metric (like steps, calories) to Firestore.
     * Using .document(label).set(data) will OVERWRITE the document with 'label' as its ID.
     * If you intend to add new, unique logs for a metric, consider using .add() instead.
     */
    fun logToFirebase(label: String, value: Int) {
        val currentDb = db
        if (currentDb == null) {
            Log.e(TAG, "❌ Firestore not initialized. Skipping remote log.")
            return
        }

        val data = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "value" to value
        )

        currentDb.collection("metrics").document(label).set(data)
            .addOnSuccessListener {
                Log.d(TAG, "📡 Logged to Firebase: $label = $value")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to log to Firebase: ${e.message}", e)
            }
    }

    /**
     * Sync step count for a user. This method adds a new document.
     */
    @Suppress("unused") // Suppress warning if this function is not used internally in this file
    fun syncStep(userId: String, steps: Int) {
        val currentDb = db
        if (currentDb == null) {
            Log.e(TAG, "❌ Firestore not initialized. Skipping step sync.")
            return
        }

        val data = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "steps" to steps
        )

        currentDb.collection("users").document(userId).collection("stepLogs")
            .add(data)
            .addOnSuccessListener {
                Log.d(TAG, "👣 Synced steps to Firebase for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Step sync failed: ${e.message}", e)
            }
    }
}