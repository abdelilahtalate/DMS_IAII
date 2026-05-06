package com.example.dms_app.feature_trips.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.feature_trips.domain.EventType
import com.example.dms_app.feature_trips.domain.TripDetail
import com.example.dms_app.feature_trips.domain.TripEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripDetailsViewModel : ViewModel() {

    private val _state = MutableStateFlow(TripDetailsUiState())
    val state: StateFlow<TripDetailsUiState> = _state.asStateFlow()

    // Explicit flows as requested in prompt if preferred
    // val trip = _state.map { it.trip }.stateIn(...) // etc
    // For now, keeping the state flow as it contains both.

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Mock data following the layout requirements
            val mockTrip = TripDetail(
                id = tripId,
                title = "Morning Commute",
                date = "April 28, 2026",
                distance = "18 km",
                duration = "27 min",
                scoreImprovement = 15
            )
            
            val mockEvents = listOf(
                TripEvent("08:10", EventType.START, "Started from Home"),
                TripEvent("08:15", EventType.PHONE, "Phone Distraction", durationSec = 12),
                TripEvent("08:22", EventType.WARNING, "Harsh Braking"),
                TripEvent("08:37", EventType.END, "Arrived at Work")
            )

            _state.update { 
                it.copy(
                    trip = mockTrip,
                    events = mockEvents,
                    isLoading = false
                )
            }
        }
    }
}
