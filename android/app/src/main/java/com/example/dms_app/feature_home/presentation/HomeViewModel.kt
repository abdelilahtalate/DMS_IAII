package com.example.dms_app.feature_home.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_home.data.repository.HomeRepositoryImpl
import com.example.dms_app.feature_home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: HomeRepository = HomeRepositoryImpl(ApiClient.apiService)
    private val tokenManager = TokenManager(app)

    private val _driverName = MutableStateFlow("Driver")
    val driverName: StateFlow<String> = _driverName.asStateFlow()

    private val _greeting = MutableStateFlow(getGreetingMessage())
    val greeting: StateFlow<String> = _greeting.asStateFlow()

    private val _focusScore = MutableStateFlow(0)
    val focusScore: StateFlow<Int> = _focusScore.asStateFlow()

    private val _todayStats = MutableStateFlow(
        TodayStats(0, "0 km", "0m", "+0")
    )
    val todayStats: StateFlow<TodayStats> = _todayStats.asStateFlow()

    private val _recentTrips = MutableStateFlow<List<TripSummary>>(emptyList())
    val recentTrips: StateFlow<List<TripSummary>> = _recentTrips.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch the actual userId from TokenManager
                val userId = tokenManager.userIdFlow.first()
                println("DEBUG: HomeViewModel loading data for userId: $userId")
                if (userId == -1) {
                    println("DEBUG: userId is -1, aborting load")
                    return@launch
                }

                val profile = repository.getProfile(userId)
                _driverName.value = profile.name
                
                val insights = repository.getInsights(userId)
                _focusScore.value = insights.stats.averageScore
                _todayStats.value = TodayStats(
                    trips = insights.stats.totalTrips,
                    distance = "${insights.stats.totalDistance} km",
                    duration = formatDuration(insights.stats.totalDuration),
                    scoreChange = if (insights.stats.scoreChange >= 0) "+${insights.stats.scoreChange}" else "${insights.stats.scoreChange}"
                )

                val sessions = repository.getRecentTrips(userId)
                _recentTrips.value = sessions.sortedByDescending { it.start_time }.take(5).map { session ->
                    val date = try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        outputFormat.format(inputFormat.parse(session.start_time)!!)
                    } catch (e: Exception) { "Recent Trip" }

                    TripSummary(
                        id = session.id.toString(),
                        name = "$date Trip",
                        timeRange = formatTimeRange(session.start_time, session.end_time),
                        scoreChange = if (session.alert_count == 0) "+${session.average_focus / 10}" else "-${session.alert_count * 5}",
                        isPositive = session.alert_count == 0
                    )
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatTimeRange(start: String, end: String?): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = inputFormat.parse(start)
            val startTime = outputFormat.format(startDate!!)
            val endTime = if (end != null) {
                val endDate = inputFormat.parse(end)
                outputFormat.format(endDate!!)
            } else {
                "Ongoing"
            }
            "$startTime - $endTime"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    private fun formatDuration(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
