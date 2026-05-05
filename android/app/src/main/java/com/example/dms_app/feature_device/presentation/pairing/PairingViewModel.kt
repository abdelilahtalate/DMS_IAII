package com.example.dms_app.feature_device.presentation.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.network.PairRequest
import com.example.dms_app.feature_device.data.DeviceRepository
import com.example.dms_app.feature_device.domain.PairDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PairingViewModel : ViewModel() {

    private val repository = DeviceRepository()
    private val useCase = PairDeviceUseCase(repository)

    private val _state = MutableStateFlow(PairingUiState())
    val state: StateFlow<PairingUiState> = _state

    fun pairDevice(deviceId: String, secret: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val request = PairRequest(
                    device_id = deviceId,
                    secret = secret,
                    user_email = "test@test.com" // TEMP (later from token)
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