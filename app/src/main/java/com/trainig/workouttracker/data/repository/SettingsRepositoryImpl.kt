package com.trainig.workouttracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.trainig.workouttracker.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Kept in its own file so backup rules can exclude it (see res/xml/backup_rules.xml)
private val Context.secretsDataStore: DataStore<Preferences> by preferencesDataStore(name = "secrets")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val dataStore = context.dataStore
    private val secretsDataStore = context.secretsDataStore

    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
    }

    override val anthropicApiKey: Flow<String> =
        secretsDataStore.data.map { preferences ->
            preferences[PreferencesKeys.ANTHROPIC_API_KEY] ?: ""
        }

    override suspend fun setAnthropicApiKey(key: String) {
        secretsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ANTHROPIC_API_KEY] = key.trim()
        }
    }

    override val isSoundEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        }

    override val isHapticsEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
        }

    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}

