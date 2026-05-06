package com.example.dms_app.feature_insights.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.feature_insights.domain.DayScore
import com.example.dms_app.feature_insights.domain.InsightStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsightsViewModel : ViewModel() {

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Initial data as requested
            val weeklyData = listOf(
                DayScore("Mon", 78), DayScore("Tue", 82), DayScore("Wed", 75),
                DayScore("Thu", 88), DayScore("Fri", 92), DayScore("Sat", 85), DayScore("Sun", 79)
            )
            
            val heatmap = listOf(
                listOf(0, 2, 3, 1, 0, 0, 0), listOf(1, 3, 4, 2, 1, 0, 0),
                listOf(2, 4, 5, 3, 2, 1, 0), listOf(1, 3, 4, 2, 1, 0, 0),
                listOf(0, 2, 3, 1, 0, 0, 0), listOf(0, 1, 2, 1, 0, 0, 0)
            )

            val stats = InsightStats(
                averageScore = 83,
                scoreChange = 5,
                bestDay = "Fri",
                bestScore = 92,
                totalTrips = 23,
                eventCount = 12
            )

            _state.update { 
                it.copy(
                    weeklyScores = weeklyData,
                    heatmapData = heatmap,
                    stats = stats,
                    isLoading = false
                )
            }
        }
    }
}
