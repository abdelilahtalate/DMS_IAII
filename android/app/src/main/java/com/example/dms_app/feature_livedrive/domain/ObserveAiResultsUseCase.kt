package com.example.dms_app.feature_livedrive.domain

import com.example.dms_app.feature_livedrive.data.LiveDriveRepository
import com.example.dms_app.feature_livedrive.presentation.AiResultWrapper
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveAiResultsUseCase(private val repository: LiveDriveRepository) {
    private val gson = Gson()

    operator fun invoke(): Flow<AiResultWrapper> {
        return repository.observeAiResults().map { json ->
            gson.fromJson(json, AiResultWrapper::class.java)
        }
    }
}
