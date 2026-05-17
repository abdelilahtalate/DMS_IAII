package com.example.dms_app.feature_livedrive.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.DriveMindApp
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.location.DefaultLocationClient
import com.example.dms_app.core.location.LocationRepository
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_livedrive.data.LiveDriveRepository
import com.example.dms_app.feature_livedrive.domain.ObserveAiResultsUseCase
import com.example.dms_app.feature_livedrive.domain.StartAnalysisUseCase
import com.example.dms_app.feature_livedrive.domain.StopAnalysisUseCase
import com.example.dms_app.feature_settings.data.AlertSettingsRepository
import com.example.dms_app.feature_settings.domain.AlertSettings
import com.example.dms_app.feature_settings.domain.GetAlertSettingsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class DriveStatus {
    FOCUSED, WARNING, ALERT
}

data class FatigueData(val is_fatigued: Boolean, val score: Float)
data class DistractionData(val is_distracted: Boolean, val score: Float)
data class AiResult(val fatigue: FatigueData, val distraction: DistractionData)

data class AiResultWrapper(
    val frame_id: String,
    val timestamp: Double,
    val face_detected: Boolean,
    val status: String,
    val focus: Float,
    val scores: AiResult?
)

data class LiveDriveUiState(
    val isMonitoring: Boolean = false,
    val focusScore: Int = 100,
    val driveStatus: DriveStatus = DriveStatus.FOCUSED,
    val elapsedSeconds: Int = 0,
    val speed: Int = 0,
    val distanceMeters: Int = 0,
    val faceDetected: Boolean = false,
    val lastResult: AiResult? = null,
    val error: String? = null
)

class LiveDriveViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = LiveDriveRepository()
    private val startUseCase = StartAnalysisUseCase(repository)
    private val stopUseCase = StopAnalysisUseCase(repository)
    private val observeAiResultsUseCase = ObserveAiResultsUseCase(repository)
    private val tokenManager = TokenManager(app)
    private val getAlertSettingsUseCase = GetAlertSettingsUseCase(
        AlertSettingsRepository(
            apiService = ApiClient.apiService,
            tokenManager = tokenManager
        )
    )
    
    // GPS Location
    private val locationRepository = LocationRepository(DefaultLocationClient(DriveMindApp.instance))
    private val soundManager = com.example.dms_app.core.utils.SoundManager(app)

    private val _state = MutableStateFlow(LiveDriveUiState())
    val state: StateFlow<LiveDriveUiState> = _state

    private val _alertEvent = MutableSharedFlow<Unit>()
    val alertEvent: SharedFlow<Unit> = _alertEvent

    private var aiListeningJob: Job? = null
    private var locationJob: Job? = null
    private var timerJob: Job? = null
    private var speedJob: Job? = null
    private var locationSyncJob: Job? = null
    private var settingsRefreshJob: Job? = null
    
    private var currentSessionId: Int? = null
    private var totalDistanceMeters: Double = 0.0
    private var focusSum: Long = 0
    private var focusCount: Int = 0
    private var alertSettings = AlertSettings.defaults()
    
    private var lastAlertLogTime: Long = 0
    private var lastSoundTriggerTime: Long = 0
    
    private val ALERT_LOG_COOLDOWN_MS = 10000L // Log to backend every 10s
    private val SOUND_TRIGGER_COOLDOWN_MS = 2500L // Play sound every 2.5s if alert continues
    
    // Alert Smoothing
    private var consecutiveAlerts = 0
    private val ALERT_THRESHOLD_FRAMES = 2 // Must be alert for 2 frames to trigger sound
    
    private val capturedLocations = mutableListOf<com.example.dms_app.core.network.LocationCreate>()
    private var lastLocation: android.location.Location? = null

    fun toggleMonitoring() {
        viewModelScope.launch {
            try {
                if (_state.value.isMonitoring) {
                    println("DEBUG: Toggling OFF monitoring")
                    endDriveInternal()
                } else {
                    println("DEBUG: Toggling ON monitoring")
                    // Start Session on Backend
                    try {
                        val userId = tokenManager.userIdFlow.first()
                        if (userId == -1) {
                             _state.value = _state.value.copy(error = "User session expired. Please log in again.")
                             return@launch
                        }
                        val sessionResponse = repository.startSession(userId = userId, deviceId = "ESP_001")
                        currentSessionId = sessionResponse.session_id
                        println("DEBUG: Started session with ID: $currentSessionId for userId: $userId")
                        
                        // Initialize Session Stats
                        totalDistanceMeters = 0.0
                        focusSum = 100
                        focusCount = 1
                        capturedLocations.clear()
                        lastLocation = null
                        consecutiveAlerts = 0
                        refreshAlertSettings()
                        
                        // Start AI Analysis
                        try {
                            startUseCase()
                        } catch (e: Exception) {
                            println("DEBUG: Failed to start AI: ${e.message}")
                        }
                        
                        _state.value = _state.value.copy(
                            isMonitoring = true,
                            elapsedSeconds = 0,
                            focusScore = 100,
                            distanceMeters = 0,
                            driveStatus = DriveStatus.FOCUSED,
                            error = null
                        )
                        startListening()
                        startTimer()
                        startSpeedTracking()
                        startLocationSync()
                        startAlertSettingsRefresh()
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(error = "Failed to start session: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private fun startListening() {
        aiListeningJob?.cancel()
        aiListeningJob = viewModelScope.launch {
            observeAiResultsUseCase()
                .catch { e ->
                    _state.value = _state.value.copy(error = "Connection lost: ${e.message}")
                }
                .collect { wrapper ->
                    val newFocus = wrapper.focus.toInt()
                    val settings = alertSettings
                    val newStatus = resolveDriveStatus(wrapper, settings)
                    
                    val now = System.currentTimeMillis()
                    
                    // Sound & Haptic Logic with Cooldown and Cancellation
                    if (newStatus == DriveStatus.FOCUSED) {
                        consecutiveAlerts = 0
                        soundManager.stopAll() // Immediately stop alarm when focused
                        soundManager.stopVibration()
                    } else if (newStatus == DriveStatus.WARNING) {
                        consecutiveAlerts = 0
                        if (!settings.criticalOnly && now - lastSoundTriggerTime > SOUND_TRIGGER_COOLDOWN_MS) {
                            playWarningFeedback(settings)
                            lastSoundTriggerTime = now
                        }
                    } else if (newStatus == DriveStatus.ALERT) {
                        consecutiveAlerts++
                        
                        // Only trigger sound if alert persists for multiple frames
                        if (consecutiveAlerts >= ALERT_THRESHOLD_FRAMES) {
                            if (now - lastSoundTriggerTime > SOUND_TRIGGER_COOLDOWN_MS) {
                                _alertEvent.emit(Unit)
                                playCriticalFeedback(settings)
                                lastSoundTriggerTime = now
                            }
                        }
                        
                        // Backend Logging Cooldown
                        if (now - lastAlertLogTime > ALERT_LOG_COOLDOWN_MS) {
                            lastAlertLogTime = now
                            logAlertOnBackend(resolveAlertType(wrapper, settings))
                        }
                    }
                    
                    _state.value = _state.value.copy(
                        lastResult = wrapper.scores,
                        focusScore = newFocus,
                        driveStatus = newStatus,
                        faceDetected = wrapper.face_detected
                    )

                    focusSum += newFocus
                    focusCount++
                }
        }
    }

    private suspend fun refreshAlertSettings() {
        getAlertSettingsUseCase()
            .onSuccess { settings ->
                alertSettings = settings ?: AlertSettings.defaults()
            }
            .onFailure { error ->
                alertSettings = AlertSettings.defaults()
                _state.value = _state.value.copy(
                    error = error.localizedMessage ?: "Failed to load alert settings"
                )
            }
    }

    private fun resolveDriveStatus(
        wrapper: AiResultWrapper,
        settings: AlertSettings
    ): DriveStatus {
        val fatigueAlert = wrapper.scores?.fatigue?.let {
            it.score >= settings.fatigueSensitivity.scoreThreshold()
        } ?: false

        val distractionAlert = wrapper.scores?.distraction?.let {
            it.score >= settings.distractionSensitivity.scoreThreshold()
        } ?: false

        val focus = wrapper.focus.toInt()
        val alertThreshold = settings.averageSensitivity().focusAlertThreshold()
        val warningThreshold = settings.averageSensitivity().focusWarningThreshold()

        return when {
            fatigueAlert || distractionAlert || focus < alertThreshold -> DriveStatus.ALERT
            focus < warningThreshold -> DriveStatus.WARNING
            else -> DriveStatus.FOCUSED
        }
    }

    private fun resolveAlertType(
        wrapper: AiResultWrapper,
        settings: AlertSettings
    ): String {
        val fatigueScore = wrapper.scores?.fatigue?.score ?: 0f
        val distractionScore = wrapper.scores?.distraction?.score ?: 0f

        return when {
            fatigueScore >= settings.fatigueSensitivity.scoreThreshold() -> "FATIGUE"
            distractionScore >= settings.distractionSensitivity.scoreThreshold() -> "DISTRACTION"
            else -> "ALERT"
        }
    }

    private fun playWarningFeedback(settings: AlertSettings) {
        if (settings.voiceAlerts) {
            soundManager.playWarning()
        }
        if (settings.hapticFeedback) {
            soundManager.vibrate(300)
        }
    }

    private fun playCriticalFeedback(settings: AlertSettings) {
        if (settings.voiceAlerts) {
            soundManager.playCritical()
        }
        if (settings.hapticFeedback) {
            soundManager.vibrate(800)
        }
    }

    private fun logAlertOnBackend(type: String) {
        val sessionId = currentSessionId ?: return
        val loc = lastLocation ?: return
        
        viewModelScope.launch {
            try {
                repository.logAlert(sessionId, type, loc.latitude, loc.longitude)
            } catch (e: Exception) {
                println("DEBUG: Failed to log alert: ${e.message}")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                
                // Calculate distance based on current speed
                // speed is in km/h. Distance in meters = (speed * 1000) / 3600 per second
                val metersPerSecond = (_state.value.speed.toDouble() * 1000.0) / 3600.0
                totalDistanceMeters += metersPerSecond
                
                _state.value = _state.value.copy(
                    elapsedSeconds = _state.value.elapsedSeconds + 1,
                    distanceMeters = totalDistanceMeters.toInt()
                )
            }
        }
    }

    private fun startSpeedTracking() {
        speedJob?.cancel()
        speedJob = viewModelScope.launch {
            locationRepository.getLocationUpdates(2000L)
                .catch { }
                .collect { location ->
                    lastLocation = location
                    _state.value = _state.value.copy(speed = (location.speed * 3.6).roundToInt())
                }
        }
    }

    private fun startLocationSync() {
        locationSyncJob?.cancel()
        locationSyncJob = viewModelScope.launch {
            while (true) {
                delay(10000) // Collect point every 10 seconds for the path
                val sessionId = currentSessionId ?: continue
                val loc = lastLocation ?: continue
                
                val point = com.example.dms_app.core.network.LocationCreate(
                    session_id = sessionId,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    speed = (loc.speed * 3.6)
                )
                
                capturedLocations.add(point)
                
                // Sync with backend every 30 seconds or if we have 5 points
                if (capturedLocations.size >= 3) {
                    try {
                        repository.logLocations(sessionId, capturedLocations.toList())
                        capturedLocations.clear()
                    } catch (e: Exception) {
                        println("DEBUG: Failed to sync locations: ${e.message}")
                    }
                }
            }
        }
    }

    private fun startAlertSettingsRefresh() {
        settingsRefreshJob?.cancel()
        settingsRefreshJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                refreshAlertSettings()
            }
        }
    }

    private fun endDriveInternal() {
        viewModelScope.launch {
            val sessionId = currentSessionId
            if (sessionId != null) {
                try {
                    // Final stats sync
                    val avgFocus = if (focusCount > 0) (focusSum / focusCount).toInt() else 100
                    repository.updateScore(sessionId, avgFocus)
                    repository.updateDistance(sessionId, totalDistanceMeters.toInt())
                    
                    if (capturedLocations.isNotEmpty()) {
                        repository.logLocations(sessionId, capturedLocations.toList())
                    }
                    
                    repository.stopSession(sessionId)
                    stopUseCase()
                } catch (e: Exception) {
                    println("DEBUG: Error stopping session: ${e.message}")
                }
            }
            
            aiListeningJob?.cancel()
            locationJob?.cancel()
            timerJob?.cancel()
            speedJob?.cancel()
            locationSyncJob?.cancel()
            settingsRefreshJob?.cancel()
            currentSessionId = null
            
            _state.value = _state.value.copy(
                isMonitoring = false,
                lastResult = null,
                elapsedSeconds = 0,
                speed = 0,
                distanceMeters = 0
            )
            println("DEBUG: Drive ended and jobs cancelled")
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiListeningJob?.cancel()
        locationJob?.cancel()
        timerJob?.cancel()
        speedJob?.cancel()
        locationSyncJob?.cancel()
        settingsRefreshJob?.cancel()
        soundManager.release()
    }
}

private fun String.scoreThreshold(): Float {
    return when (uppercase()) {
        "HIGH" -> 0.45f
        "MEDIUM" -> 0.65f
        "LOW" -> 0.80f
        else -> 0.65f
    }
}

private fun String.focusWarningThreshold(): Int {
    return when (uppercase()) {
        "HIGH" -> 80
        "MEDIUM" -> 75
        "LOW" -> 65
        else -> 75
    }
}

private fun String.focusAlertThreshold(): Int {
    return when (uppercase()) {
        "HIGH" -> 50
        "MEDIUM" -> 40
        "LOW" -> 30
        else -> 40
    }
}

private fun AlertSettings.averageSensitivity(): String {
    val values = listOf(
        fatigueSensitivity.sensitivityValue(),
        phoneSensitivity.sensitivityValue(),
        distractionSensitivity.sensitivityValue()
    )
    val average = values.average()
    return when {
        average >= 2.5 -> "HIGH"
        average <= 1.5 -> "LOW"
        else -> "MEDIUM"
    }
}

private fun String.sensitivityValue(): Int {
    return when (uppercase()) {
        "HIGH" -> 3
        "MEDIUM" -> 2
        "LOW" -> 1
        else -> 2
    }
}
