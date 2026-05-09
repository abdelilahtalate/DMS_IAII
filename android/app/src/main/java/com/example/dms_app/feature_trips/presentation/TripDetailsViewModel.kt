package com.example.dms_app.feature_trips.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_trips.data.TripsRepositoryImpl
import com.example.dms_app.feature_trips.domain.EventType
import com.example.dms_app.feature_trips.domain.TripDetail
import com.example.dms_app.feature_trips.domain.TripEvent
import com.example.dms_app.feature_trips.domain.usecase.GetTripDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripDetailsViewModel : ViewModel() {

    private val repository = TripsRepositoryImpl(ApiClient.apiService)
    private val getTripDetailUseCase = GetTripDetailUseCase(repository)

    private val _state = MutableStateFlow(TripDetailsUiState())
    val state: StateFlow<TripDetailsUiState> = _state.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = getTripDetailUseCase(tripId.toInt())
            
            result.onSuccess { (detail, events, mapData) ->
                val (locations, alertMarkers) = mapData
                _state.update { 
                    it.copy(
                        trip = detail,
                        events = events,
                        locations = locations,
                        alertMarkers = alertMarkers,
                        isLoading = false
                    )
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
