package com.example.dms_app.feature_profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.feature_profile.data.repository.ProfileRepositoryImpl
import com.example.dms_app.feature_profile.domain.usecase.GetDevicesUseCase
import com.example.dms_app.feature_profile.domain.usecase.GetProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // Dependencies (Manual for now)
    private val repository = ProfileRepositoryImpl()
    private val getProfileUseCase = GetProfileUseCase(repository)
    private val getDevicesUseCase = GetDevicesUseCase(repository)

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        // Using a default userId of 1 for now. 
        // In a real app, this would come from an AuthManager or TokenManager.
        loadProfile(userId = 1)
    }

    private fun loadProfile(userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val profileResult = getProfileUseCase(userId)
            val devicesResult = getDevicesUseCase(userId)

            _state.update { 
                it.copy(
                    userProfile = profileResult.getOrNull(),
                    connectedDevices = devicesResult.getOrDefault(emptyList()),
                    isLoading = false,
                    error = profileResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun signOut() {
        // Handle sign out logic
    }
}
