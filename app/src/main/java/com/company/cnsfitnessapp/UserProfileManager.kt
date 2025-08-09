// File: app/src/main/java/com/company/cnsfitnessapp/UserProfileManager.kt
package com.company.cnsfitnessapp // Corrected to all lowercase 'cnsfitnessapp'

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.company.cnsfitnessapp.RemoteSyncManager // Import RemoteSyncManager

object UserProfileManager {
    private const val TAG = "UserProfileManager" // Added TAG, made const as it's in an object
    private var currentUser: String = "guest"
    private val userSteps: MutableMap<String, Int> = mutableMapOf() // Note: This is in-memory, will reset on app close

    fun login(user: String) {
        currentUser = user
        userSteps.putIfAbsent(user, 0)
        Log.d(TAG, "User logged in: $currentUser")
    }

    fun getCurrentUser(): String = currentUser

    fun getSteps(): Int = userSteps[currentUser] ?: 0

    fun addStep() {
        val newStepCount = getSteps() + 1
        userSteps[currentUser] = newStepCount
        Log.d(TAG, "$currentUser steps: $newStepCount")
    }

    fun getCalories(): Int = getSteps() / 20 // Assuming a simple conversion for demonstration

    // @Suppress("unused") // Uncomment if this function is truly not used elsewhere
    fun syncProfileToCloud() {
        // Get Firestore instance from RemoteSyncManager for consistency
        val db = RemoteSyncManager.getFirestore()
        if (db == null) {
            Log.e(TAG, "❌ Firestore not initialized. Skipping profile sync.") // Used TAG
            return
        }

        val data = hashMapOf(
            "steps" to getSteps(),
            "calories" to getCalories(),
            "lastSynced" to System.currentTimeMillis()
        )

        db.collection("users").document(currentUser)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Synced user profile successfully for $currentUser.") // Used TAG
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to sync profile for $currentUser: ${e.message}", e) // Used TAG
            }
    }
}