package com.example.dms_app.feature_auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _loadingComplete = MutableStateFlow(false)
    val loadingComplete: StateFlow<Boolean> = _loadingComplete

    init {
        viewModelScope.launch {
            delay(2500)
            _loadingComplete.value = true
        }
    }
}
