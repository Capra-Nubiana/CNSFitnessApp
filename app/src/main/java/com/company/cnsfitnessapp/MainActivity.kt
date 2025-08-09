// File: app/src/main/java/com/company/cnsfitnessapp/MainActivity.kt
// This file has been updated to use the new files created below.
package com.company.cnsfitnessapp

// Core Compose Material3 Icon import (used for Icons.Default.*)

// THEME IMPORT: Corrected capitalization assumption
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.company.cnsfitnessapp.ui.theme.CNSFitnessAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Note: I am assuming the following files exist in your project
// TimerPanelComposable.kt, ChartPanelWrapper.kt, ReminderManager.kt, UserProfileManager.kt
// SettingsManager.kt, DummyFitnessChartDao.kt, LogSearchDialog.kt

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (ReminderManager.isInitialized()) {
                    ReminderManager.startReminders(this)
                } else {
                    Toast.makeText(this, "Reminder manager not initialized. Try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders will not show.", Toast.LENGTH_LONG).show()
            }
        }

    private val requestWriteStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission granted. You can now export CSV.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Write permission denied. Cannot export CSV.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- Initialize Managers (CRITICAL) ---
        SettingsManager.init(applicationContext)
        DatabaseLogger.init(applicationContext)
        FirebaseApp.initializeApp(this)
        val firestore = FirebaseFirestore.getInstance()
        RemoteSyncManager.initializeFirebase(firestore)
        UserProfileManager.login("guest")
        val fitnessChartDao = DummyFitnessChartDao(applicationContext)
        val chartPanelWrapper = ChartPanelWrapper(applicationContext, fitnessChartDao)
        DataLogger.init(this, firestore)
        DataLogger.setChartPanelWrapper(chartPanelWrapper)
        AISuggestionEngine.reset()
        ReminderManager.init(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            CNSFitnessAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSearchDialog by remember { mutableStateOf(false) }
                    var showAISuggestionDialog by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Welcome, ${UserProfileManager.getCurrentUser()}",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))

                        // This needs to be a @Composable function in TimerPanelComposable.kt
                        // TODO: Implement TimerPanelComposable.kt
                        // TimerPanelComposable(modifier = Modifier.fillMaxWidth(0.8f))

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { showSearchDialog = true },
                                text = { Text("Search Logs") },
                                icon = { Icon(Icons.Default.Search, contentDescription = "Search Logs") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )

                            ExtendedFloatingActionButton(
                                onClick = { showAISuggestionDialog = true },
                                text = { Text("AI Suggestion") },
                                icon = { Icon(Icons.Default.Lightbulb, contentDescription = "AI Suggestion") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    // Check for permission before attempting to export
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        requestWriteStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    } else {
                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                CSVExporter.exportUserLogs(context, UserProfileManager.getCurrentUser(), "fitness_logs_${UserProfileManager.getCurrentUser()}.csv")
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Logs exported to CSV!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                text = { Text("Export CSV") },
                                icon = { Icon(Icons.Default.Download, contentDescription = "Export CSV") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )

                            var remindersOn by remember { mutableStateOf(false) }
                            ExtendedFloatingActionButton(
                                onClick = {
                                    if (remindersOn) {
                                        ReminderManager.stopReminders(context)
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            if (ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                ) == PackageManager.PERMISSION_GRANTED
                                            ) {
                                                ReminderManager.startReminders(context)
                                            } else {
                                                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        } else {
                                            ReminderManager.startReminders(context)
                                        }
                                    }
                                    remindersOn = !remindersOn
                                },
                                text = { Text(if (remindersOn) "Stop Reminders" else "Start Reminders") },
                                icon = { Icon(if (remindersOn) Icons.Default.NotificationsOff else Icons.Default.Notifications, contentDescription = "Toggle Reminders") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // TODO: Implement FitnessChartScreen
                        // FitnessChartScreen(chartPanelWrapper = chartPanelWrapper)

                        if (showSearchDialog) {
                            // TODO: Implement LogSearchDialog.LogSearchDialogComposable
                            // LogSearchDialog.LogSearchDialogComposable(
                            //     onDismiss = { showSearchDialog = false }
                            // )
                        }

                        if (showAISuggestionDialog) {
                            AISuggestionEngine.AISuggestionDialogComposable(
                                context = context,
                                onDismiss = { showAISuggestionDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
