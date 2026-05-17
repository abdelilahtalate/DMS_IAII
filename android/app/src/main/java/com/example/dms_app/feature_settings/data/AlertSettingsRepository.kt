package com.example.dms_app.feature_settings.data

import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.ApiService
import com.example.dms_app.core.network.UserSettingsResponse
import com.example.dms_app.core.network.UserSettingsUpdate
import kotlinx.coroutines.flow.first

class AlertSettingsRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getSettings(): UserSettingsResponse? {
        val userId = tokenManager.userIdFlow.first()
        if (userId == -1) return null
        return apiService.getSettings(userId)
    }

    suspend fun updateSettings(settings: UserSettingsUpdate): UserSettingsResponse? {
        val userId = tokenManager.userIdFlow.first()
        if (userId == -1) return null
        return apiService.updateSettings(userId, settings)
    }
}
