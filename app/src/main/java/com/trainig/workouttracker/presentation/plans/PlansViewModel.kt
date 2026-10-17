package com.trainig.workouttracker.presentation.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.model.WorkoutSession
import com.trainig.workouttracker.core.domain.repository.WorkoutPlanRepository
import com.trainig.workouttracker.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutSessionRepository: WorkoutSessionRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val plans: StateFlow<List<WorkoutPlan>> = workoutPlanRepository.getPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeSession: StateFlow<WorkoutSession?> = workoutSessionRepository.getActiveSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentSessions: StateFlow<List<WorkoutSession>> = workoutSessionRepository.getRecentSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Starts [plan]; any workout still in progress is discarded first. */
    fun startWorkout(plan: WorkoutPlan) {
        viewModelScope.launch {
            activeSession.value?.let { workoutSessionRepository.discardSession(it.id) }
            val sessionId = workoutSessionRepository.startSession(plan)
            _uiEvent.emit(UiEvent.WorkoutStarted(sessionId))
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            workoutPlanRepository.deletePlan(planId)
            _uiEvent.emit(UiEvent.ShowMessage("Plan deleted"))
        }
    }

    sealed interface UiEvent {
        data class WorkoutStarted(val sessionId: Long) : UiEvent
        data class ShowMessage(val message: String) : UiEvent
    }
}
