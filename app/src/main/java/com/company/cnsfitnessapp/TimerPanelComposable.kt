package com.company.cnsfitnessapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier // <--- IMPORTANT: Ensure this import is here
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * A Composable function that displays a workout timer with start/pause and reset functionality.
 * This is an Android Jetpack Compose equivalent of the Java Swing TimerPanel.
 */
@Composable
fun TimerPanelComposable(modifier: Modifier = Modifier) { // <--- ADDED 'modifier: Modifier = Modifier'
    // State variables for the timer
    var seconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // LaunchedEffect to manage the timer coroutine
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(1000L) // Wait for 1 second
                seconds++
            }
        }
    }

    // UI Layout
    Surface(
        modifier = modifier // <--- APPLY THE PASSED MODIFIER HERE
            .padding(16.dp), // You can keep internal padding
        shape = MaterialTheme.shapes.medium, // Rounded corners
        color = MaterialTheme.colorScheme.surfaceVariant, // Slightly different background
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline) // Simulate titled border
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title (simulating TitledBorder text)
            Text(
                text = "⏱ Workout Timer",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Timer display label
            Text(
                text = "Workout Time: ${seconds}s",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { isRunning = !isRunning }, // Toggle running state
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isRunning) "Pause" else if (seconds > 0) "Resume" else "Start")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        isRunning = false // Stop the timer if running
                        seconds = 0      // Reset seconds
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}