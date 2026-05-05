package com.example.dms_app.feature_auth.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    private val _currentSlide = MutableStateFlow(0)
    val currentSlide: StateFlow<Int> = _currentSlide

    fun updateSlide(index: Int) {
        _currentSlide.value = index
    }
}
