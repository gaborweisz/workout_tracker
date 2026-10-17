package com.trainig.workouttracker.presentation.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.PlanPreferences
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.repository.AiPlanGenerator
import com.trainig.workouttracker.core.domain.repository.SettingsRepository
import com.trainig.workouttracker.core.domain.repository.WorkoutPlanRepository
import com.trainig.workouttracker.core.domain.usecase.SuggestPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanGeneratorViewModel @Inject constructor(
    private val suggestPlanUseCase: SuggestPlanUseCase,
    private val aiPlanGenerator: AiPlanGenerator,
    private val workoutPlanRepository: WorkoutPlanRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    enum class Source { BUILT_IN, AI }

    data class UiState(
        val preferences: PlanPreferences = PlanPreferences(),
        val suggestion: WorkoutPlan? = null,
        val source: Source? = null,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val apiKey: StateFlow<String> = settingsRepository.anthropicApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )

    fun updatePreferences(transform: (PlanPreferences) -> PlanPreferences) {
        _uiState.update { it.copy(preferences = transform(it.preferences)) }
    }

    fun suggest() {
        _uiState.update {
            it.copy(suggestion = suggestPlanUseCase(it.preferences), source = Source.BUILT_IN)
        }
    }

    fun askAi() {
        val key = apiKey.value
        if (key.isBlank() || _uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            aiPlanGenerator.generate(_uiState.value.preferences, key)
                .onSuccess { plan ->
                    _uiState.update { it.copy(suggestion = plan, source = Source.AI, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEvent.emit(UiEvent.ShowError(error.message ?: "Something went wrong"))
                }
        }
    }

    fun regenerate() {
        when (_uiState.value.source) {
            Source.AI -> askAi()
            else -> suggest()
        }
    }

    fun acceptSuggestion() {
        val plan = _uiState.value.suggestion ?: return
        viewModelScope.launch {
            val planId = workoutPlanRepository.savePlan(plan.copy(createdAt = System.currentTimeMillis()))
            _uiEvent.emit(UiEvent.PlanSaved(planId))
        }
    }

    sealed interface UiEvent {
        data class PlanSaved(val planId: Long) : UiEvent
        data class ShowError(val error: String) : UiEvent
    }
}
