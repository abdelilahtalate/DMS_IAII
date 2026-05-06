package com.example.dms_app.feature_home.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class TodayStats(
    val trips: Int,
    val distance: String,
    val duration: String,
    val scoreChange: String
)

data class TripSummary(
    val id: String,
    val name: String,
    val timeRange: String,
    val scoreChange: String,
    val isPositive: Boolean
)

class HomeViewModel : ViewModel() {

    private val _driverName = MutableStateFlow("Alex Chen")
    val driverName: StateFlow<String> = _driverName.asStateFlow()

    private val _greeting = MutableStateFlow(getGreetingMessage())
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    private val _focusScore = MutableStateFlow(80)
    val focusScore: StateFlow<Int> = _focusScore.asStateFlow()

    private val _todayStats = MutableStateFlow(
        TodayStats(
            trips = 3,
            distance = "48 km",
            duration = "1h 24m",
            scoreChange = "+12"
        )
    )
    val todayStats: StateFlow<TodayStats> = _todayStats.asStateFlow()

    private val _recentTrips = MutableStateFlow(
        listOf(
            TripSummary(
                id = "1",
                name = "Morning Commute",
                timeRange = "08:15 - 08:42",
                scoreChange = "+15",
                isPositive = true
            ),
            TripSummary(
                id = "2",
                name = "Grocery Store",
                timeRange = "14:20 - 14:35",
                scoreChange = "-2",
                isPositive = false
            )
        )
    )
    val recentTrips: StateFlow<List<TripSummary>> = _recentTrips.asStateFlow()

    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
