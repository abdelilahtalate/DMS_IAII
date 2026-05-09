package com.example.dms_app.feature_device.presentation.pairing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.core.network.PairRequest
import com.example.dms_app.core.network.PairResponse
import com.example.dms_app.feature_device.data.DeviceRepository
import com.example.dms_app.feature_device.domain.PairDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PairingViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = DeviceRepository()
    private val useCase = PairDeviceUseCase(repository)
    private val tokenManager = TokenManager(app)

    private val _state = MutableStateFlow(PairingUiState())
    val state: StateFlow<PairingUiState> = _state

    fun pairDevice(deviceId: String, secret: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val userEmail = tokenManager.emailFlow.first()
                if (userEmail.isEmpty()) {
                    _state.value = _state.value.copy(error = "Session expired. Please log in again.", isLoading = false)
                    return@launch
                }

                val request = PairRequest(
                    device_id = deviceId,
                    secret = secret,
                    user_email = userEmail
                )

                val response = useCase(request)

                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true,
                    previewUrl = response.device.preview_stream
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}