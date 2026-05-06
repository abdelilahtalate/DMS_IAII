package com.example.dms_app.feature_livedrive.domain

import com.example.dms_app.core.network.StatusResponse
import com.example.dms_app.feature_livedrive.data.LiveDriveRepository

class StartAnalysisUseCase(private val repository: LiveDriveRepository) {
    suspend operator fun invoke(): StatusResponse {
        return repository.startAnalysis()
    }
}
