package com.example.dms_app.feature_profile.domain.usecase

import com.example.dms_app.feature_profile.domain.UserProfile
import com.example.dms_app.feature_profile.domain.repository.ProfileRepository

class GetProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: Int): Result<UserProfile> {
        return repository.getProfile(userId)
    }
}
