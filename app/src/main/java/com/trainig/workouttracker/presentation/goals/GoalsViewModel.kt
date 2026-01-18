package com.trainig.workouttracker.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import com.trainig.workouttracker.core.domain.usecase.SetDailyGoalsUseCase
import com.trainig.workouttracker.core.domain.util.DateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val dailyGoalRepository: DailyGoalRepository,
    private val setDailyGoalsUseCase: SetDailyGoalsUseCase,
    private val dateProvider: DateProvider
) : ViewModel() {

    private val _goalInputs = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val goalInputs: StateFlow<Map<Long, Int>> = _goalInputs.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess.asSharedFlow()

    val exercises: StateFlow<List<Exercise>> = exerciseRepository.getActiveExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadCurrentGoals()
    }

    private fun loadCurrentGoals() {
        viewModelScope.launch {
            dailyGoalRepository.getGoalsForDate(dateProvider.today())
                .first()
                .let { goals ->
                    _goalInputs.value = goals.associate { it.exerciseId to it.targetReps }
                }
        }
    }

    fun updateGoal(exerciseId: Long, target: Int) {
        _goalInputs.value = _goalInputs.value.toMutableMap().apply {
            this[exerciseId] = target.coerceAtLeast(0)
        }
    }

    fun saveGoals() {
        viewModelScope.launch {
            setDailyGoalsUseCase(_goalInputs.value)
            _saveSuccess.emit(true)
        }
    }
}

