package com.example.dms_app.feature_settings.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.core.utils.SoundManager
import com.example.dms_app.feature_settings.data.AlertSettingsRepository
import com.example.dms_app.feature_settings.domain.AlertSettings
import com.example.dms_app.feature_settings.domain.GetAlertSettingsUseCase
import com.example.dms_app.feature_settings.domain.UpdateAlertSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SensitivityLevel { LOW, MEDIUM, HIGH }

class AlertSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AlertSettingsRepository(
        apiService = ApiClient.apiService,
        tokenManager = TokenManager(app)
    )
    private val getAlertSettingsUseCase = GetAlertSettingsUseCase(repository)
    private val updateAlertSettingsUseCase = UpdateAlertSettingsUseCase(repository)
    private val soundManager = SoundManager(app)

    private val _state = MutableStateFlow(AlertSettingsUiState())
    val state: StateFlow<AlertSettingsUiState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getAlertSettingsUseCase()
                .onSuccess { settings ->
                    _state.update {
                        settings?.toUiState()?.copy(isLoading = false, error = null)
                            ?: it.copy(isLoading = false, error = "User session not found")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.localizedMessage ?: "Failed to load settings"
                        )
                    }
                }
        }
    }

    fun setFatigue(level: SensitivityLevel) {
        updateAndSave { it.copy(fatigueLevel = level) }
    }

    fun setPhone(level: SensitivityLevel) {
        updateAndSave { it.copy(phoneLevel = level) }
    }

    fun setDistraction(level: SensitivityLevel) {
        updateAndSave { it.copy(distractionLevel = level) }
    }

    fun toggleVoice() {
        updateAndSave { it.copy(voiceAlertsOn = !it.voiceAlertsOn) }
    }

    fun toggleHaptic() {
        updateAndSave { it.copy(hapticOn = !it.hapticOn) }
    }

    fun toggleCritical() {
        updateAndSave { it.copy(criticalOnlyOn = !it.criticalOnlyOn) }
    }

    fun setVoice(voice: String) {
        updateAndSave { it.copy(preferredVoice = voice) }
    }

    fun previewWarning() {
        soundManager.playWarning()
    }

    fun previewCritical() {
        soundManager.playCritical()
    }

    private fun updateAndSave(transform: (AlertSettingsUiState) -> AlertSettingsUiState) {
        val updated = transform(_state.value).copy(error = null)
        _state.value = updated
        saveSettings(updated)
    }

    private fun saveSettings(settings: AlertSettingsUiState) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            updateAlertSettingsUseCase(settings.toDomain())
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = error.localizedMessage ?: "Failed to save settings"
                        )
                    }
                }
        }
    }
}

private fun AlertSettings.toUiState(): AlertSettingsUiState {
    return AlertSettingsUiState(
        fatigueLevel = fatigueSensitivity.toSensitivityLevel(SensitivityLevel.HIGH),
        phoneLevel = phoneSensitivity.toSensitivityLevel(SensitivityLevel.MEDIUM),
        distractionLevel = distractionSensitivity.toSensitivityLevel(SensitivityLevel.HIGH),
        voiceAlertsOn = voiceAlerts,
        hapticOn = hapticFeedback,
        criticalOnlyOn = criticalOnly,
        preferredVoice = preferredVoice
    )
}

private fun AlertSettingsUiState.toDomain(): AlertSettings {
    return AlertSettings(
        fatigueSensitivity = fatigueLevel.name,
        phoneSensitivity = phoneLevel.name,
        distractionSensitivity = distractionLevel.name,
        voiceAlerts = voiceAlertsOn,
        hapticFeedback = hapticOn,
        criticalOnly = criticalOnlyOn,
        preferredVoice = preferredVoice
    )
}

private fun String.toSensitivityLevel(fallback: SensitivityLevel): SensitivityLevel {
    return runCatching { SensitivityLevel.valueOf(this) }.getOrDefault(fallback)
}
