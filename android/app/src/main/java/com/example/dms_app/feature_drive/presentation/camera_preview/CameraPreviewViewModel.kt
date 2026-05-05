package com.example.dms_app.feature_drive.presentation.camera_preview

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.feature_drive.data.DriveRepository
import com.example.dms_app.feature_drive.domain.GetStreamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class CameraPreviewUiState(
    val bitmap: Bitmap? = null,
    val frameCount: Long = 0, // Forces StateFlow to emit even if Bitmap reference is the same
    val isLoading: Boolean = true,
    val error: String? = null
)

class CameraPreviewViewModel : ViewModel() {

    private val repository = DriveRepository(ApiClient.apiService)
    private val getStreamUseCase = GetStreamUseCase(repository)

    private val _state = MutableStateFlow(CameraPreviewUiState())
    val state: StateFlow<CameraPreviewUiState> = _state

    private var currentFrameCount = 0L

    private var streamJob: kotlinx.coroutines.Job? = null

    fun startStream(url: String) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            getStreamUseCase(url)
                .catch { e ->
                    _state.value = _state.value.copy(
                        error = e.localizedMessage,
                        isLoading = false
                    )
                }
                .collect { newBitmap ->
                    currentFrameCount++
                    _state.value = _state.value.copy(
                        bitmap = newBitmap,
                        frameCount = currentFrameCount,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun stopStream() {
        streamJob?.cancel()
        streamJob = null
    }
}