package com.example.dms_app.feature_settings.domain

import com.example.dms_app.core.network.UserSettingsUpdate
import com.example.dms_app.feature_settings.data.AlertSettingsRepository

class UpdateAlertSettingsUseCase(
    private val repository: AlertSettingsRepository
) {
    suspend operator fun invoke(settings: AlertSettings): Result<Unit> {
        return try {
            repository.updateSettings(settings.toUpdate())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun AlertSettings.toUpdate(): UserSettingsUpdate {
    return UserSettingsUpdate(
        fatigue_sensitivity = fatigueSensitivity,
        phone_sensitivity = phoneSensitivity,
        distraction_sensitivity = distractionSensitivity,
        voice_alerts = voiceAlerts,
        haptic_feedback = hapticFeedback,
        critical_only = criticalOnly,
        preferred_voice = preferredVoice
    )
}
