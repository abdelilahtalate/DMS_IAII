package com.example.dms_app.feature_trips.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_trips.data.TripsRepositoryImpl
import com.example.dms_app.feature_trips.domain.TripSummary
import com.example.dms_app.feature_trips.domain.usecase.GetTripsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TripsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TripsRepositoryImpl(ApiClient.apiService)
    private val getTripsUseCase = GetTripsUseCase(repository)
    private val tokenManager = TokenManager(app)

    private val _state = MutableStateFlow(TripsUiState())
    val state: StateFlow<TripsUiState> = _state.asStateFlow()

    init {
        loadTrips()
    }

    fun loadTrips() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = tokenManager.userIdFlow.first()
            if (userId == -1) return@launch
            
            val result = getTripsUseCase(userId = userId)
            result.onSuccess { list ->
                _state.update { 
                    it.copy(
                        trips = list,
                        filteredTrips = applyFilter(list, it.activeFilter),
                        isLoading = false
                    ) 
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false, error = it.error) }
            }
        }
    }

    fun setFilter(filter: String) {
        _state.update { 
            it.copy(
                activeFilter = filter,
                filteredTrips = applyFilter(it.trips, filter)
            )
        }
    }

    private fun applyFilter(list: List<TripSummary>, filter: String): List<TripSummary> {
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        
        return when (filter) {
            "Today" -> list.filter { now - it.rawTimestamp <= dayMillis }
            "This Week" -> list.filter { now - it.rawTimestamp <= 7 * dayMillis }
            "This Month" -> list.filter { now - it.rawTimestamp <= 30 * dayMillis }
            else -> list
        }.sortedByDescending { it.rawTimestamp }
    }
}
