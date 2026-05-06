package com.example.dms_app.feature_livedrive.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.DriveMindApp
import com.example.dms_app.core.location.DefaultLocationClient
import com.example.dms_app.core.location.LocationRepository
import com.example.dms_app.feature_livedrive.data.LiveDriveRepository
import com.example.dms_app.feature_livedrive.domain.ObserveAiResultsUseCase
import com.example.dms_app.feature_livedrive.domain.StartAnalysisUseCase
import com.example.dms_app.feature_livedrive.domain.StopAnalysisUseCase
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    val faceDetected: Boolean = false,
    val lastResult: AiResult? = null,
    val error: String? = null
)

class LiveDriveViewModel : ViewModel() {

    private val repository = LiveDriveRepository()
    private val startUseCase = StartAnalysisUseCase(repository)
    private val stopUseCase = StopAnalysisUseCase(repository)
    
    // GPS Location
    private val locationRepository = LocationRepository(DefaultLocationClient(DriveMindApp.instance))

    private val _state = MutableStateFlow(LiveDriveUiState())
    val state: StateFlow<LiveDriveUiState> = _state

    private val _alertEvent = MutableSharedFlow<Unit>()
    val alertEvent: SharedFlow<Unit> = _alertEvent

    private var monitoringJob: Job? = null
    private var timerJob: Job? = null
    private var speedJob: Job? = null
    private var locationSyncJob: Job? = null
    
    private var currentSessionId: Int? = null

    fun toggleMonitoring() {
        viewModelScope.launch {
            try {
                if (_state.value.isMonitoring) {
                    endDrive()
                } else {
                    // Start Session on Backend
                    val sessionResponse = repository.startSession(userId = 1, deviceId = "ESP_001")
                    currentSessionId = sessionResponse.session_id
                    
                    // Start AI Analysis
                    startUseCase()
                    
                    _state.value = _state.value.copy(
                        isMonitoring = true,
                        elapsedSeconds = 0,
                        focusScore = 100,
                        driveStatus = DriveStatus.FOCUSED
                    )
                    startListening()
                    startTimer()
                    startSpeedTracking()
                    startLocationSync()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private val observeAiResultsUseCase = ObserveAiResultsUseCase(repository)

    private fun startListening() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            observeAiResultsUseCase()
                .catch { e ->
                    _state.value = _state.value.copy(error = "Connection lost: ${e.message}")
                }
                .collect { wrapper ->
                    val newFocus = wrapper.focus.toInt()
                    val newStatus = when {
                        newFocus < 40 -> DriveStatus.ALERT
                        newFocus < 75 -> DriveStatus.WARNING
                        else -> DriveStatus.FOCUSED
                    }
                    
                    _state.value = _state.value.copy(
                        lastResult = wrapper.scores,
                        focusScore = newFocus,
                        driveStatus = newStatus,
                        faceDetected = wrapper.face_detected
                    )

                    if (newStatus == DriveStatus.ALERT) {
                        _alertEvent.emit(Unit)
                        logAlertOnBackend("DISTRACTION")
                    }
                }
        }
    }

    private fun logAlertOnBackend(type: String) {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            try {
                repository.logAlert(sessionId, type, 0.0, 0.0) // Mock lat/lon for now
            } catch (e: Exception) { }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.value = _state.value.copy(elapsedSeconds = _state.value.elapsedSeconds + 1)
            }
        }
    }

    private fun startSpeedTracking() {
        speedJob?.cancel()
        speedJob = viewModelScope.launch {
            locationRepository.getSpeedUpdates()
                .catch { }
                .collect { speed ->
                    _state.value = _state.value.copy(speed = speed)
                }
        }
    }

    private fun startLocationSync() {
        locationSyncJob?.cancel()
        locationSyncJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Sync location every 5 seconds
                val sessionId = currentSessionId ?: continue
                try {
                    repository.logLocation(sessionId, 0.0, 0.0, _state.value.speed.toDouble())
                } catch (e: Exception) { }
            }
        }
    }

    fun endDrive() {
        viewModelScope.launch {
            try {
                currentSessionId?.let { repository.stopSession(it) }
                stopUseCase()
            } catch (e: Exception) { }
            
            monitoringJob?.cancel()
            timerJob?.cancel()
            speedJob?.cancel()
            locationSyncJob?.cancel()
            currentSessionId = null
            
            _state.value = _state.value.copy(
                isMonitoring = false,
                lastResult = null,
                elapsedSeconds = 0,
                speed = 0
            )
        }
    }
}
