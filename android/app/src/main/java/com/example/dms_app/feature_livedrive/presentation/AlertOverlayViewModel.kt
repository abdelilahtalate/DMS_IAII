package com.example.dms_app.feature_livedrive.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.feature_livedrive.data.LiveDriveRepository
import com.example.dms_app.feature_livedrive.domain.ObserveAiResultsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AlertType {
    FATIGUE, DISTRACTION, PHONE
}

class AlertOverlayViewModel(
    private val observeAiResultsUseCase: ObserveAiResultsUseCase = ObserveAiResultsUseCase(LiveDriveRepository())
) : ViewModel() {

    private val _alertType = MutableStateFlow(AlertType.FATIGUE)
    val alertType: StateFlow<AlertType> = _alertType.asStateFlow()

    private val _showDismiss = MutableStateFlow(false)
    val showDismiss: StateFlow<Boolean> = _showDismiss.asStateFlow()

    init {
        // Observe real data from the repository via Use Case
        viewModelScope.launch {
            observeAiResultsUseCase()
                .catch { }
                .collect { result ->
                    val type = when {
                        result.scores?.fatigue?.score ?: 0f > 0.7f -> AlertType.FATIGUE
                        result.scores?.distraction?.score ?: 0f > 0.7f -> AlertType.DISTRACTION
                        else -> _alertType.value
                    }
                    _alertType.value = type
                }
        }

        // Show dismiss button after 700ms delay
        viewModelScope.launch {
            delay(700)
            _showDismiss.value = true
        }
    }

    fun dismiss() {
        // Dismiss logic handled by parent
    }

    fun callHelp() {
        // In a real app, this would use Intent(Intent.ACTION_CALL)
        println("[Alert] Calling Emergency Services...")
    }

    fun triggerVoiceAlert() {
        // In a real app, this would use TextToSpeech or MediaPlayer
        println("[Alert] Triggering Voice Alert: Please wake up and focus on the road!")
    }
}
