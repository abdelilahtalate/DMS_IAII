package com.example.dms_app.feature_insights.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_insights.data.InsightsRepository
import com.example.dms_app.feature_insights.domain.GetInsightsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsightsViewModel : ViewModel() {

    private val repository = InsightsRepository(ApiClient.apiService)
    private val getInsightsUseCase = GetInsightsUseCase(repository)

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = getInsightsUseCase(userId = 1) // Using default userId 1 for now
            
            result.onSuccess { insights ->
                _state.update { 
                    it.copy(
                        weeklyScores = insights.dayScores,
                        heatmapData = insights.heatmapData,
                        stats = insights.stats,
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { e ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load insights"
                    )
                }
            }
        }
    }
}
