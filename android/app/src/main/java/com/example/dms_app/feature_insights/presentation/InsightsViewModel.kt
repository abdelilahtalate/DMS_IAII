package com.example.dms_app.feature_insights.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_insights.data.InsightsRepository
import com.example.dms_app.feature_insights.domain.GetInsightsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InsightsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = InsightsRepository(ApiClient.apiService)
    private val getInsightsUseCase = GetInsightsUseCase(repository)
    private val tokenManager = TokenManager(app)

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val userId = tokenManager.userIdFlow.first()
            if (userId == -1) return@launch

            val result = getInsightsUseCase(userId = userId)
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
