package com.example.dms_app.feature_auth.presentation.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.database.TokenManager
import com.example.dms_app.feature_auth.data.AuthRepository
import com.example.dms_app.feature_auth.domain.LoginUseCase
import com.example.dms_app.core.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    // 🔹 Dependencies (manual for now, DI later)
    private val repository = AuthRepository()
    private val loginUseCase = LoginUseCase(repository)
    private val tokenManager = TokenManager(app)

    // 🔹 State
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    // 🔹 UI Updates
    fun updateEmail(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    // 🔹 Login Action
    fun login() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null
                )

                val result = loginUseCase(LoginRequest(
                    _state.value.email,
                    _state.value.password)
                )

                if (result.error != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error
                    )
                    return@launch
                }

                // Save token
                tokenManager.saveToken(result.access_token ?: "")

                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true
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