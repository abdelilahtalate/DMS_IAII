package com.example.dms_app.feature_settings.domain

import com.example.dms_app.core.network.UserSettingsResponse
import com.example.dms_app.feature_settings.data.AlertSettingsRepository

class GetAlertSettingsUseCase(
    private val repository: AlertSettingsRepository
) {
    suspend operator fun invoke(): Result<AlertSettings?> {
        return try {
            Result.success(repository.getSettings()?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun UserSettingsResponse.toDomain(): AlertSettings {
    return AlertSettings(
        fatigueSensitivity = fatigue_sensitivity,
        phoneSensitivity = phone_sensitivity,
        distractionSensitivity = distraction_sensitivity,
        voiceAlerts = voice_alerts,
        hapticFeedback = haptic_feedback,
        criticalOnly = critical_only,
        preferredVoice = preferred_voice
    )
}
