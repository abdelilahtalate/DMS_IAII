package com.example.dms_app.feature_profile.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.core.network.UserSettingsUpdate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SensitivityLevel { LOW, MEDIUM, HIGH }

class AlertSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val api = ApiClient.apiService
    private val tokenManager = TokenManager(app)
    private val soundManager = com.example.dms_app.core.utils.SoundManager(app)

    private val _fatigueLevel = MutableStateFlow(SensitivityLevel.HIGH)
    val fatigueLevel: StateFlow<SensitivityLevel> = _fatigueLevel.asStateFlow()

    private val _phoneLevel = MutableStateFlow(SensitivityLevel.MEDIUM)
    val phoneLevel: StateFlow<SensitivityLevel> = _phoneLevel.asStateFlow()

    private val _distractionLevel = MutableStateFlow(SensitivityLevel.HIGH)
    val distractionLevel: StateFlow<SensitivityLevel> = _distractionLevel.asStateFlow()

    private val _voiceAlertsOn = MutableStateFlow(true)
    val voiceAlertsOn: StateFlow<Boolean> = _voiceAlertsOn.asStateFlow()

    private val _hapticOn = MutableStateFlow(true)
    val hapticOn: StateFlow<Boolean> = _hapticOn.asStateFlow()

    private val _criticalOnlyOn = MutableStateFlow(false)
    val criticalOnlyOn: StateFlow<Boolean> = _criticalOnlyOn.asStateFlow()

    private val _preferredVoice = MutableStateFlow("DEFAULT")
    val preferredVoice: StateFlow<String> = _preferredVoice.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = tokenManager.userIdFlow.first()
                if (userId != -1) {
                    val settings = api.getSettings(userId)
                    _fatigueLevel.value = SensitivityLevel.valueOf(settings.fatigue_sensitivity)
                    _phoneLevel.value = SensitivityLevel.valueOf(settings.phone_sensitivity)
                    _distractionLevel.value = SensitivityLevel.valueOf(settings.distraction_sensitivity)
                    _voiceAlertsOn.value = settings.voice_alerts
                    _hapticOn.value = settings.haptic_feedback
                    _critical_only_on.value = settings.critical_only
                    _preferredVoice.value = settings.preferred_voice
                }
            } catch (e: Exception) {
                // Log error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private var _critical_only_on = _criticalOnlyOn as MutableStateFlow<Boolean>

    private fun saveSettings() {
        viewModelScope.launch {
            try {
                val userId = tokenManager.userIdFlow.first()
                if (userId != -1) {
                    api.updateSettings(
                        userId = userId,
                        settings = UserSettingsUpdate(
                            fatigue_sensitivity = _fatigueLevel.value.name,
                            phone_sensitivity = _phoneLevel.value.name,
                            distraction_sensitivity = _distractionLevel.value.name,
                            voice_alerts = _voiceAlertsOn.value,
                            haptic_feedback = _hapticOn.value,
                            critical_only = _criticalOnlyOn.value,
                            preferred_voice = _preferredVoice.value
                        )
                    )
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun setFatigue(l: SensitivityLevel) {
        _fatigueLevel.value = l
        saveSettings()
    }

    fun setPhone(l: SensitivityLevel) {
        _phoneLevel.value = l
        saveSettings()
    }

    fun setDistraction(l: SensitivityLevel) {
        _distractionLevel.value = l
        saveSettings()
    }

    fun toggleVoice() {
        _voiceAlertsOn.value = !_voiceAlertsOn.value
        saveSettings()
    }

    fun toggleHaptic() {
        _hapticOn.value = !_hapticOn.value
        saveSettings()
    }

    fun toggleCritical() {
        _critical_only_on.value = !_criticalOnlyOn.value
        saveSettings()
    }

    fun setVoice(voice: String) {
        _preferredVoice.value = voice
        saveSettings()
    }

    fun previewWarning() {
        soundManager.playWarning()
    }

    fun previewCritical() {
        soundManager.playCritical()
    }
}
