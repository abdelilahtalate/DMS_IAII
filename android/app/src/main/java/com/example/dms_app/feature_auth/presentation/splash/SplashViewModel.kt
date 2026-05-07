package com.example.dms_app.feature_auth.presentation.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)

    private val _navigationTarget = MutableStateFlow<String?>(null)
    val navigationTarget: StateFlow<String?> = _navigationTarget

    init {
        viewModelScope.launch {
            delay(2500)
            val token = tokenManager.tokenFlow.firstOrNull()
            val userId = tokenManager.userIdFlow.firstOrNull() ?: -1
            
            if (token != null && userId != -1) {
                _navigationTarget.value = "home/placeholder"
            } else {
                _navigationTarget.value = "onboarding"
            }
        }
    }
}
