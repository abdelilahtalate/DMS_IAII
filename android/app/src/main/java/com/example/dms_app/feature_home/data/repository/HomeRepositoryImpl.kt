package com.example.dms_app.feature_home.data.repository

import com.example.dms_app.core.network.ApiService
import com.example.dms_app.core.network.InsightOverview
import com.example.dms_app.core.network.SessionSummaryResponse
import com.example.dms_app.core.network.UserProfileResponse
import com.example.dms_app.feature_home.domain.repository.HomeRepository

class HomeRepositoryImpl(private val apiService: ApiService) : HomeRepository {
    override suspend fun getProfile(userId: Int): UserProfileResponse {
        return apiService.getProfile(userId)
    }

    override suspend fun getInsights(userId: Int): InsightOverview {
        return apiService.getInsightOverview(userId)
    }

    override suspend fun getRecentTrips(userId: Int): List<SessionSummaryResponse> {
        return apiService.listSessions(userId)
    }
}
