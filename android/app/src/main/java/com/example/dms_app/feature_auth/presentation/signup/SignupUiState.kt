package com.example.dms_app.feature_auth.presentation.signup

data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val gender: String = "",
    val age: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)