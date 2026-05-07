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
    private var totalDistanceMeters: Double = 0.0
    private var focusSum: Long = 0
    private var focusCount: Int = 0
    
    private var lastAlertTime: Long = 0
    private val ALERT_COOLDOWN_MS = 5000L // Only log one alert every 5 seconds
    
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
                        val sessionResponse = repository.startSession(userId = 1, deviceId = "ESP_001")
                        currentSessionId = sessionResponse.session_id
                        println("DEBUG: Started session with ID: $currentSessionId")
                    } catch (e: Exception) {
                        println("DEBUG: Failed to start session: ${e.message}")
                        _state.value = _state.value.copy(error = "Failed to start session: ${e.message}")
                        return@launch
                    }
                    
                    totalDistanceMeters = 0.0
                    focusSum = 100
                    focusCount = 1
                    capturedLocations.clear()
                    lastLocation = null
                    
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
                }
            } catch (e: Exception) {
                println("DEBUG: Toggle error: ${e.message}")
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

                    focusSum += newFocus
                    focusCount++

                    if (newStatus == DriveStatus.ALERT) {
                        _alertEvent.emit(Unit)
                        
                        val now = System.currentTimeMillis()
                        if (now - lastAlertTime > ALERT_COOLDOWN_MS) {
                            lastAlertTime = now
                            
                            // Determine alert type
                            val type = when {
                                wrapper.scores?.fatigue?.is_fatigued == true -> "FATIGUE"
                                wrapper.scores?.distraction?.is_distracted == true -> "DISTRACTION"
                                else -> "ALERT"
                            }
                            logAlertOnBackend(type)
                        }
                    }
                }
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
                    speed = loc.speed.toDouble()
                )
                capturedLocations.add(point)
                
                try {
                    repository.logLocation(sessionId, point.latitude, point.longitude, point.speed)
                    repository.updateDistance(sessionId, _state.value.distanceMeters)
                } catch (e: Exception) { }
            }
        }
    }

    private fun simplifyPath(points: List<com.example.dms_app.core.network.LocationCreate>, epsilon: Double): List<com.example.dms_app.core.network.LocationCreate> {
        if (points.size < 3) return points

        var maxDist = 0.0
        var index = 0
        for (i in 1 until points.size - 1) {
            val dist = perpendicularDistance(points[i], points[0], points.last())
            if (dist > maxDist) {
                maxDist = dist
                index = i
            }
        }

        return if (maxDist > epsilon) {
            val left = simplifyPath(points.subList(0, index + 1), epsilon)
            val right = simplifyPath(points.subList(index, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(points.first(), points.last())
        }
    }

    private fun perpendicularDistance(p: com.example.dms_app.core.network.LocationCreate, start: com.example.dms_app.core.network.LocationCreate, end: com.example.dms_app.core.network.LocationCreate): Double {
        val dy = end.latitude - start.latitude
        val dx = end.longitude - start.longitude
        if (dx == 0.0 && dy == 0.0) {
            return Math.sqrt(Math.pow(p.latitude - start.latitude, 2.0) + Math.pow(p.longitude - start.longitude, 2.0))
        }
        val t = ((p.longitude - start.longitude) * dx + (p.latitude - start.latitude) * dy) / (dx * dx + dy * dy)
        val nearestX: Double
        val nearestY: Double
        if (t < 0) {
            nearestX = start.longitude
            nearestY = start.latitude
        } else if (t > 1) {
            nearestX = end.longitude
            nearestY = end.latitude
        } else {
            nearestX = start.longitude + t * dx
            nearestY = start.latitude + t * dy
        }
        return Math.sqrt(Math.pow(p.longitude - nearestX, 2.0) + Math.pow(p.latitude - nearestY, 2.0))
    }

    fun endDrive() {
        viewModelScope.launch {
            endDriveInternal()
        }
    }

    private suspend fun endDriveInternal() {
        println("DEBUG: Ending drive for session: $currentSessionId")
        try {
            currentSessionId?.let { id ->
                println("DEBUG: Syncing final distance: ${_state.value.distanceMeters}")
                repository.updateDistance(id, _state.value.distanceMeters)
                
                // Post average focus
                if (focusCount > 0) {
                    val avgFocus = (focusSum / focusCount).toInt()
                    println("DEBUG: Syncing average focus: $avgFocus")
                    repository.updateScore(id, avgFocus)
                }
                
                println("DEBUG: Calling stopSession endpoint")
                
                // Final Path Simplification and Sync
                if (capturedLocations.size > 2) {
                    println("DEBUG: Simplifying path with ${capturedLocations.size} points")
                    // epsilon of 0.0001 is roughly 11 meters
                    val simplified = simplifyPath(capturedLocations, 0.0001)
                    println("DEBUG: Path simplified to ${simplified.size} points. Syncing...")
                    try {
                        repository.logLocations(id, simplified)
                    } catch (e: Exception) {
                        println("DEBUG: Batch location sync failed: ${e.message}")
                    }
                }

                repository.stopSession(id) 
            }
            
            println("DEBUG: Stopping AI Analysis")
            stopUseCase()
        } catch (e: Exception) {
            println("DEBUG: Error during endDrive: ${e.message}")
        } finally {
            monitoringJob?.cancel()
            timerJob?.cancel()
            speedJob?.cancel()
            locationSyncJob?.cancel()
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
}
