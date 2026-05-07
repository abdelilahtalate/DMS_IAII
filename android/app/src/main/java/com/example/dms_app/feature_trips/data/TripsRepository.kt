package com.example.dms_app.feature_trips.data

import com.example.dms_app.core.network.ApiService
import com.example.dms_app.core.network.SessionDetailResponse
import com.example.dms_app.core.network.SessionSummaryResponse

interface TripsRepository {
    suspend fun listSessions(userId: Int?): List<SessionSummaryResponse>
    suspend fun getSessionDetail(sessionId: Int): SessionDetailResponse
}

class TripsRepositoryImpl(private val apiService: ApiService) : TripsRepository {
    override suspend fun listSessions(userId: Int?): List<SessionSummaryResponse> {
        return apiService.listSessions(userId)
    }

    override suspend fun getSessionDetail(sessionId: Int): SessionDetailResponse {
        return apiService.getSessionDetail(sessionId)
    }
}
