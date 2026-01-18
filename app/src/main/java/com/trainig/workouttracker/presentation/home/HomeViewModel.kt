package com.trainig.workouttracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.TodayExerciseItem
import com.trainig.workouttracker.core.domain.usecase.GetTodayRecordsUseCase
import com.trainig.workouttracker.core.domain.usecase.IncrementProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayRecordsUseCase: GetTodayRecordsUseCase,
    private val incrementProgressUseCase: IncrementProgressUseCase
) : ViewModel() {

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId.asStateFlow()

    private val _celebrationEvent = MutableSharedFlow<CelebrationEvent>()
    val celebrationEvent: SharedFlow<CelebrationEvent> = _celebrationEvent.asSharedFlow()

    val todayExercises: StateFlow<List<TodayExerciseItem>> = getTodayRecordsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGoalsCompleted: StateFlow<Boolean> = todayExercises
        .map { exercises ->
            val withGoals = exercises.filter { it.hasGoal }
            withGoals.isNotEmpty() && withGoals.all { it.isGoalAchieved }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun selectExercise(exerciseId: Long?) {
        _selectedExerciseId.value = exerciseId
    }

    fun incrementProgress(delta: Int) {
        val exerciseId = _selectedExerciseId.value ?: return

        viewModelScope.launch {
            val result = incrementProgressUseCase(exerciseId, delta)

            // Check if goal was just achieved
            if (delta > 0 && result.newValue > result.oldValue) {
                val exercise = todayExercises.value.find { it.exercise.id == exerciseId }
                if (exercise != null && exercise.hasGoal && result.newValue >= exercise.targetReps && result.oldValue < exercise.targetReps) {
                    _celebrationEvent.emit(CelebrationEvent.GoalAchieved(exercise.exercise.name))
                }
            }
        }
    }

    sealed interface CelebrationEvent {
        data class GoalAchieved(val exerciseName: String) : CelebrationEvent
        data object AllGoalsCompleted : CelebrationEvent
    }
}

