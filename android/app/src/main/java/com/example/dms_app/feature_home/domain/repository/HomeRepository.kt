package com.example.dms_app.feature_home.domain.repository

import com.example.dms_app.core.network.InsightOverview
import com.example.dms_app.core.network.UserProfileResponse
import com.example.dms_app.core.network.SessionSummaryResponse

interface HomeRepository {
    suspend fun getProfile(userId: Int): UserProfileResponse
    suspend fun getInsights(userId: Int): InsightOverview
    suspend fun getRecentTrips(userId: Int): List<SessionSummaryResponse>
}
