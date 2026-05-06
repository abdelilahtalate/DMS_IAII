package com.example.dms_app.feature_profile.domain.usecase

import com.example.dms_app.feature_profile.domain.Device
import com.example.dms_app.feature_profile.domain.repository.ProfileRepository

class GetDevicesUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: Int): Result<List<Device>> {
        return repository.getDevices(userId)
    }
}
