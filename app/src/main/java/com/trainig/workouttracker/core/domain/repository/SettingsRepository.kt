package com.trainig.workouttracker.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isSoundEnabled: Flow<Boolean>
    val isHapticsEnabled: Flow<Boolean>
    val onboardingCompleted: Flow<Boolean>
    val anthropicApiKey: Flow<String>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setAnthropicApiKey(key: String)
}

