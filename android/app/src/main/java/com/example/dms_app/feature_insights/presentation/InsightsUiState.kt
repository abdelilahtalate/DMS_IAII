package com.example.dms_app.feature_insights.presentation

import com.example.dms_app.feature_insights.domain.DayScore
import com.example.dms_app.feature_insights.domain.InsightStats

data class InsightsUiState(
    val weeklyScores: List<DayScore> = emptyList(),
    val heatmapData: List<List<Int>> = emptyList(),
    val stats: InsightStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
