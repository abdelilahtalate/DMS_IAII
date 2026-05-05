package com.example.dms_app.feature_auth.domain

import com.example.dms_app.core.network.AuthResponse
import com.example.dms_app.core.network.SignupRequest
import com.example.dms_app.feature_auth.data.AuthRepository

class SignupUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        request: SignupRequest
    ): AuthResponse {
        return repository.signup(request)
    }
}