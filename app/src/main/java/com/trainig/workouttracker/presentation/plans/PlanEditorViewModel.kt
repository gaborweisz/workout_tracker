package com.trainig.workouttracker.presentation.plans

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.PlanExercise
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.repository.WorkoutPlanRepository
import com.trainig.workouttracker.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    private val planId: Long = savedStateHandle.get<Long>(Screen.PlanEditor.ARG_PLAN_ID) ?: 0L
    val isNewPlan: Boolean = planId == 0L

    private val _plan = MutableStateFlow(WorkoutPlan(id = planId, name = "", exercises = emptyList()))
    val plan: StateFlow<WorkoutPlan> = _plan.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        if (!isNewPlan) {
            viewModelScope.launch {
                workoutPlanRepository.getPlan(planId)?.let { _plan.value = it }
            }
        }
    }

    fun setName(name: String) {
        _plan.update { it.copy(name = name) }
    }

    fun updateExercise(index: Int, transform: (PlanExercise) -> PlanExercise) {
        _plan.update { plan ->
            plan.copy(exercises = plan.exercises.mapIndexed { i, exercise -> if (i == index) transform(exercise) else exercise })
        }
    }

    fun addExercise(name: String, notes: String = "") {
        val exercise = PlanExercise(name = name.trim(), sets = 3, reps = 10, weightKg = 0.0, restSeconds = 90, notes = notes)
        _plan.update { it.copy(exercises = it.exercises + exercise) }
    }

    fun removeExercise(index: Int) {
        _plan.update { plan -> plan.copy(exercises = plan.exercises.filterIndexed { i, _ -> i != index }) }
    }

    fun moveExercise(from: Int, to: Int) {
        _plan.update { plan ->
            if (to !in plan.exercises.indices) return@update plan
            val exercises = plan.exercises.toMutableList()
            exercises.add(to, exercises.removeAt(from))
            plan.copy(exercises = exercises)
        }
    }

    fun save() {
        val plan = _plan.value
        viewModelScope.launch {
            when {
                plan.name.isBlank() -> _uiEvent.emit(UiEvent.ShowError("Give your plan a name"))
                plan.exercises.isEmpty() -> _uiEvent.emit(UiEvent.ShowError("Add at least one exercise"))
                plan.exercises.any { it.name.isBlank() } -> _uiEvent.emit(UiEvent.ShowError("Every exercise needs a name"))
                else -> {
                    workoutPlanRepository.savePlan(plan.copy(name = plan.name.trim()))
                    _uiEvent.emit(UiEvent.Saved)
                }
            }
        }
    }

    sealed interface UiEvent {
        data object Saved : UiEvent
        data class ShowError(val error: String) : UiEvent
    }
}
