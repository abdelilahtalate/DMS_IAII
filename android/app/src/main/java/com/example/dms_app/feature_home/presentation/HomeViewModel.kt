package com.example.dms_app.feature_home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.feature_home.data.HomeRepository
import com.example.dms_app.feature_home.domain.StartAnalysisUseCase
import com.example.dms_app.feature_home.domain.StopAnalysisUseCase
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class FatigueData(
    val is_fatigued: Boolean,
    val score: Float
)

data class DistractionData(
    val is_distracted: Boolean,
    val score: Float
)

data class AiResult(
    val fatigue: FatigueData,
    val distraction: DistractionData
)

data class AiResultWrapper(
    val scores: AiResult
)

data class HomeUiState(
    val isMonitoring: Boolean = false,
    val lastResult: AiResult? = null,
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()
    private val startUseCase = StartAnalysisUseCase(repository)
    private val stopUseCase = StopAnalysisUseCase(repository)

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private var monitoringJob: Job? = null

    fun toggleMonitoring() {
        viewModelScope.launch {
            try {
                if (_state.value.isMonitoring) {
                    stopUseCase()
                    monitoringJob?.cancel()
                    _state.value = _state.value.copy(isMonitoring = false, lastResult = null)
                } else {
                    startUseCase()
                    _state.value = _state.value.copy(isMonitoring = true)
                    startListening()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    private fun startListening() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            repository.observeAiResults()
                .catch { e ->
                    _state.value = _state.value.copy(error = "Connection lost: ${e.message}")
                }
                .collect { json ->
                    try {
                        val wrapper = Gson().fromJson(json, AiResultWrapper::class.java)
                        _state.value = _state.value.copy(lastResult = wrapper.scores)
                    } catch (e: Exception) {
                        // Handle parse error if needed
                    }
                }
        }
    }
}
