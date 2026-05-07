package com.example.dms_app.feature_insights.data

import com.example.dms_app.core.network.ApiService
import com.example.dms_app.core.network.InsightOverview

class InsightsRepository(private val apiService: ApiService) {
    suspend fun getInsights(userId: Int): InsightOverview {
        return apiService.getInsightOverview(userId)
    }
}
