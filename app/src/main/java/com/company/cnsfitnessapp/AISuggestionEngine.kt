// File: app/src/main/java/com/company/cnsfitnessapp/AISuggestionEngine.kt
package com.company.cnsfitnessapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CancellationException

// Added imports for DatabaseLogger and UserProfileManager
import com.company.cnsfitnessapp.DatabaseLogger
import com.company.cnsfitnessapp.UserProfileManager

/**
 * An object to handle AI suggestions and display them in a Composable dialog.
 * It uses the Gemini API for text generation.
 */
object AISuggestionEngine {
    private const val TAG = "AISuggestionEngine"
    // IMPORTANT: Replace with your actual Gemini API key.
    private const val API_KEY = ""
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key=$API_KEY"

    fun reset() {
        Log.d(TAG, "AI Suggestion Engine reset.")
    }

    /**
     * A Composable dialog to show AI-generated fitness suggestions.
     *
     * @param context   The context to use for API calls and toasts.
     * @param onDismiss The action to take when the dialog is dismissed.
     */
    @Composable
    fun AISuggestionDialogComposable(
        context: Context,
        onDismiss: () -> Unit
    ) {
        var suggestionText by remember { mutableStateOf("Tap 'Get Suggestion' to get a fitness tip.") }
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("AI Fitness Suggestion") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            Text(suggestionText)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Launch a coroutine to handle the network operation
                        coroutineScope.launch {
                            isLoading = true
                            val userLogs = DatabaseLogger.searchLogs(UserProfileManager.getCurrentUser(), "")
                            val prompt = "Provide a short, positive, and encouraging fitness suggestion for a user with the following recent activity logs: ${userLogs.takeLast(5)}. Focus on practical advice, a single workout idea, or a motivational quote. If no logs are available, provide a general fitness tip."
                            val newSuggestion = getAISuggestion(prompt)

                            withContext(Dispatchers.Main) {
                                suggestionText = newSuggestion
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text("Get Suggestion")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        )
    }

    /**
     * Makes an API call to the Gemini API to get a fitness suggestion.
     *
     * @param prompt The prompt to send to the AI.
     * @return The AI-generated text, or an error message.
     */
    private suspend fun getAISuggestion(prompt: String): String {
        return withContext(Dispatchers.IO) {
            if (API_KEY.isEmpty()) {
                return@withContext "API key not set. Please add your Gemini API key in AISuggestionEngine.kt."
            }

            try {
                val url = URL(API_URL)
                val httpConnection = url.openConnection() as HttpURLConnection
                httpConnection.requestMethod = "POST"
                httpConnection.setRequestProperty("Content-Type", "application/json")
                httpConnection.doOutput = true

                val payload = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(JSONObject().apply {
                        put("parts", org.json.JSONArray().put(JSONObject().apply {
                            put("text", prompt)
                        }))
                    }))
                }.toString()

                OutputStreamWriter(httpConnection.outputStream).use { writer ->
                    writer.write(payload)
                    writer.flush()
                }

                val responseCode = httpConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = httpConnection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val generatedText = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    return@withContext generatedText
                } else {
                    val errorResponse = httpConnection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    Log.e(TAG, "API call failed with code $responseCode: $errorResponse")
                    return@withContext "Error getting suggestion. Please try again. ($responseCode)"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call: ${e.message}", e)
                return@withContext "An error occurred: ${e.message}"
            }
        }
    }
}
