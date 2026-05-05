package com.example.dms_app.feature_auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dms_app.core.network.SignupRequest
import com.example.dms_app.feature_auth.data.AuthRepository
import com.example.dms_app.feature_auth.domain.SignupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val signupUseCase = SignupUseCase(repository)

    private val _state = MutableStateFlow(SignupUiState())
    val state: StateFlow<SignupUiState> = _state

    fun updateName(value: String) {
        _state.value = _state.value.copy(fullName = value)
    }

    fun updateEmail(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun updateGender(value: String) {
        _state.value = _state.value.copy(gender = value)
    }

    fun updateAge(value: String) {
        _state.value = _state.value.copy(age = value)
    }

    fun signup() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val current = _state.value

                val response = signupUseCase(
                    SignupRequest(
                        full_name = current.fullName,
                        email = current.email,
                        password = current.password,
                        gender = current.gender,
                        age = current.age.toIntOrNull()
                    )
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = response.message
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}