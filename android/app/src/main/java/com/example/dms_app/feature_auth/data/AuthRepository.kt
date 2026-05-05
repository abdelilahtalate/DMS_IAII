package com.example.dms_app.feature_auth.data
import com.example.dms_app.core.network.LoginRequest
import com.example.dms_app.core.network.ApiClient
import com.example.dms_app.core.network.AuthResponse
import com.example.dms_app.core.network.SignupRequest

class AuthRepository {

    suspend fun signup(request: SignupRequest): AuthResponse {
        return ApiClient.apiService.signup(request)
    }
    suspend fun login(request: LoginRequest): AuthResponse {
        return ApiClient.apiService.login(request)
    }
}

