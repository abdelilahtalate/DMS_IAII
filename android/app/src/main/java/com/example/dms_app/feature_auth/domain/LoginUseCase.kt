package com.example.dms_app.feature_auth.domain

import  com.example.dms_app.core.network.AuthResponse
import com.example.dms_app.feature_auth.data.AuthRepository
import com.example.dms_app.core.network.LoginRequest
class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
       request: LoginRequest
    ): AuthResponse {
        return repository.login(request)
    }
}