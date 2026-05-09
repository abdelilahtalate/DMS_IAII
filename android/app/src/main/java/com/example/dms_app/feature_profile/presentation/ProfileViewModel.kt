package com.example.dms_app.feature_profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.feature_profile.data.repository.ProfileRepositoryImpl
import com.example.dms_app.feature_profile.domain.usecase.GetDevicesUseCase
import com.example.dms_app.feature_profile.domain.usecase.GetProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProfileRepositoryImpl(
        tokenManager = TokenManager(application)
    )
    private val getProfileUseCase = GetProfileUseCase(repository)
    private val getDevicesUseCase = GetDevicesUseCase(repository)

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            val tokenManager = TokenManager(getApplication())
            val userId = tokenManager.userIdFlow.first()
            if (userId != -1) {
                loadProfile(userId)
            }
        }
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

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            repository.clearToken()
            onSignedOut()
        }
    }
}
