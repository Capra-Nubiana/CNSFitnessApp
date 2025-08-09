package com.company.cnsfitnessapp // VERIFY: This must match your actual folder structure (com/company/cnsfitnessapp)

import android.content.Context
import android.util.Log
import java.time.LocalDate // This will work after minSdk 26+
import java.time.format.DateTimeFormatter // This will work after minSdk 26+
import java.time.DayOfWeek // This will work after minSdk 26+
// Add these imports at the top of the file
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.company.cnsfitnessapp.UserProfileManager // Correct import for UserProfileManager

// Assuming you have a class/interface that provides database query methods for the chart:
// For example, if using Room, this would be a DAO (Data Access Object) interface.
// If using SQLiteOpenHelper, this would be methods in your helper class.
interface FitnessChartDao {
    fun getDailySteps(user: String, limit: Int): List<Pair<String, Int>> // List of (dateString, steps)
    fun getWeeklySteps(user: String, limit: Int): List<Pair<String, Int>> // List of (weekString, steps)
    fun getMonthlySteps(user: String, limit: Int): List<Pair<String, Int>> // List of (monthString, steps)
}

// Dummy/Placeholder implementation for the DAO.
// You would replace this with your actual Room DAO or SQLiteOpenHelper query methods.
class DummyFitnessChartDao(
    @Suppress("unused") private val context: Context // Suppress if context is truly not used within this class
) : FitnessChartDao {
    // This is a placeholder for actual database access
    // In a real Android app, you'd use SQLiteOpenHelper or Room here, interacting with DatabaseLogger.

    override fun getDailySteps(user: String, limit: Int): List<Pair<String, Int>> {
        Log.d("DummyFitnessChartDao", "Fetching daily steps for $user (dummy data)")
        // In a real app, you'd query your DatabaseLogger or Room DAO here.
        // Example: return DatabaseLogger.getAggregatedStepsByDay(user, limit)

        // For demonstration, return some dummy data
        val dummyData = mutableListOf<Pair<String, Int>>()
        val today = LocalDate.now() // Requires API 26+
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd

        for (i in 0 until limit) {
            val date = today.minusDays(i.toLong())
            val steps = (1000..8000).random() // Random steps for dummy data
            dummyData.add(Pair(date.format(formatter), steps))
        }
        return dummyData.sortedBy { it.first } // Ensure data is sorted by date
    }

    override fun getWeeklySteps(user: String, limit: Int): List<Pair<String, Int>> {
        Log.d("DummyFitnessChartDao", "Fetching weekly steps for $user (dummy data)")
        // Example: return DatabaseLogger.getAggregatedStepsByWeek(user, limit)
        val dummyData = mutableListOf<Pair<String, Int>>()
        val today = LocalDate.now() // Requires API 26+
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Format for week start/end

        for (i in 0 until limit) {
            // This is simplified; a real weekly aggregation needs careful calculation
            val weekStart = today.minusWeeks(i.toLong()).with(DayOfWeek.MONDAY) // Requires API 26+
            val weekEnd = weekStart.plusDays(6) // Requires API 26+
            val label = "${weekStart.format(formatter)} to ${weekEnd.format(formatter)}"
            val steps = (5000..50000).random()
            dummyData.add(Pair(label, steps))
        }
        return dummyData.sortedBy { it.first }
    }

    override fun getMonthlySteps(user: String, limit: Int): List<Pair<String, Int>> {
        Log.d("DummyFitnessChartDao", "Fetching monthly steps for $user (dummy data)")
        // Example: return DatabaseLogger.getAggregatedStepsByMonth(user, limit)
        val dummyData = mutableListOf<Pair<String, Int>>()
        val today = LocalDate.now() // Requires API 26+
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM") // Format for month

        for (i in 0 until limit) {
            val month = today.minusMonths(i.toLong()) // Requires API 26+
            val label = month.format(formatter)
            val steps = (20000..200000).random()
            dummyData.add(Pair(label, steps))
        }
        return dummyData.sortedBy { it.first }
    }
}


class ChartPanelWrapper(
    @Suppress("unused") private val context: Context, // Suppress if context is truly not used directly by ChartPanelWrapper
    private val fitnessChartDao: FitnessChartDao
) {

    // --- Chart Data (Android equivalent of DefaultCategoryDataset) ---
    // In Android Compose, you'd typically use MutableState to hold the data
    // and trigger UI recomposition when it changes.
    // Making this a mutable state that can be observed by a Composable.
    private var _chartData = mutableStateOf<List<Pair<String, Int>>>(emptyList())
    @Suppress("unused") // Suppress if only _chartData.value is accessed by Composables
    val chartData: State<List<Pair<String, Int>>> get() = _chartData // Expose as immutable State

    private val tag = "ChartPanelWrapper" // CHANGED: removed 'const', 'TAG' to 'tag' for Kotlin convention

    init {
        // Initial data load when the wrapper is created
        loadDailyData()
    }

    // Data loading methods
    fun loadDailyData() { // Made public so your UI can call it
        val user = UserProfileManager.getCurrentUser()
        val data = fitnessChartDao.getDailySteps(user, 7) // Get last 7 days for example
        val today = LocalDate.now().toString() // Requires API 26+

        // Ensure today's data is included, even if 0 steps
        val dataMap = data.associate { it.first to it.second }.toMutableMap() // Convert to mutable map
        if (!dataMap.containsKey(today)) {
            dataMap[today] = 0 // Add today with 0 steps if not present
        }

        // Sort by date for proper chart display
        val finalData = dataMap.entries.map { it.key to it.value }.sortedBy { it.first }
        _chartData.value = finalData // Update the mutable state
        Log.d(tag, "Loaded daily chart data: ${finalData.size} entries")
        // Update chart title if applicable for your Android chart library
        // chart.setTitle("Steps Over Time - Daily")
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun loadWeeklyData() { // Made public
        val user = UserProfileManager.getCurrentUser()
        val data = fitnessChartDao.getWeeklySteps(user, 4) // Get last 4 weeks for example
        _chartData.value = data.sortedBy { it.first } // Update the mutable state
        Log.d(tag, "Loaded weekly chart data: ${_chartData.value.size} entries")
        // chart.setTitle("Steps Over Time - Weekly")
    }

    @Suppress("unused") // Suppress warning if this function is not used elsewhere
    fun loadMonthlyData() { // Made public
        val user = UserProfileManager.getCurrentUser()
        val data = fitnessChartDao.getMonthlySteps(user, 6) // Get last 6 months for example
        _chartData.value = data.sortedBy { it.first } // Update the mutable state
        Log.d(tag, "Loaded monthly chart data: ${_chartData.value.size} entries")
        // chart.setTitle("Steps Over Time - Monthly")
    }

    /**
     * Increment today's step count and update the chart.
     * This method directly updates the `chartData` to reflect the new total.
     */
    fun incrementTodayStepCount(stepsToAdd: Int) {
        val today = LocalDate.now().toString() // Requires API 26+
        val currentSteps = _chartData.value.firstOrNull { it.first == today }?.second ?: 0
        val newTotal = currentSteps + stepsToAdd

        // Create a new list with updated today's data or add if not present
        val updatedList = _chartData.value.toMutableList()
        val index = updatedList.indexOfFirst { it.first == today }

        if (index != -1) {
            updatedList[index] = Pair(today, newTotal)
        } else {
            updatedList.add(Pair(today, newTotal))
        }

        // Re-sort to maintain order if new item added or order is critical
        _chartData.value = updatedList.sortedBy { it.first }

        Log.d(tag, "Incremented steps for $today by $stepsToAdd to $newTotal. Chart data: ${_chartData.value}")
        // UI will automatically recompose if observing _chartData
    }

    // --- Animation (Swing-specific, commented out) ---
    // Android animation is handled differently (e.g., ValueAnimator, Compose animation APIs).
    // You would implement this logic using Android's animation framework.
    /*
    public fun updateChartAnimated(targetSteps: Int) {
        val today = LocalDate.now().toString()
        val currentVal = chartData.firstOrNull { it.first == today }?.second ?: 0
        val current = currentVal

        // This Timer is Swing-specific and cannot be directly used in Android.
        // new Timer(10, object : ActionListener { ... }).start()
        Log.w(tag, "updateChartAnimated is not directly translatable to Android Swing Timer. Use Android animation APIs.")
    }
    */
}