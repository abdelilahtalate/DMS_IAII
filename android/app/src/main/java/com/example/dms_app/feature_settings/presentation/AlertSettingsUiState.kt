package com.example.dms_app.feature_settings.presentation

data class AlertSettingsUiState(
    val fatigueLevel: SensitivityLevel = SensitivityLevel.HIGH,
    val phoneLevel: SensitivityLevel = SensitivityLevel.MEDIUM,
    val distractionLevel: SensitivityLevel = SensitivityLevel.HIGH,
    val voiceAlertsOn: Boolean = true,
    val hapticOn: Boolean = true,
    val criticalOnlyOn: Boolean = false,
    val preferredVoice: String = "DEFAULT",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
