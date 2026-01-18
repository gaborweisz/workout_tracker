package com.trainig.workouttracker.presentation.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import com.trainig.workouttracker.core.domain.usecase.AddExerciseUseCase
import com.trainig.workouttracker.core.domain.usecase.UpdateExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val addExerciseUseCase: AddExerciseUseCase,
    private val updateExerciseUseCase: UpdateExerciseUseCase
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val exercises: StateFlow<List<Exercise>> = exerciseRepository.getActiveExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExercise(name: String) {
        viewModelScope.launch {
            when (val result = addExerciseUseCase(name)) {
                is AddExerciseUseCase.Result.Success -> {
                    _uiEvent.emit(UiEvent.ShowMessage("Exercise added"))
                }
                AddExerciseUseCase.Result.EmptyName -> {
                    _uiEvent.emit(UiEvent.ShowError("Exercise name cannot be empty"))
                }
                AddExerciseUseCase.Result.NameAlreadyExists -> {
                    _uiEvent.emit(UiEvent.ShowError("Exercise with this name already exists"))
                }
            }
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            when (updateExerciseUseCase(exercise)) {
                UpdateExerciseUseCase.Result.Success -> {
                    _uiEvent.emit(UiEvent.ShowMessage("Exercise updated"))
                }
                UpdateExerciseUseCase.Result.EmptyName -> {
                    _uiEvent.emit(UiEvent.ShowError("Exercise name cannot be empty"))
                }
                UpdateExerciseUseCase.Result.NameAlreadyExists -> {
                    _uiEvent.emit(UiEvent.ShowError("Exercise with this name already exists"))
                }
            }
        }
    }

    fun archiveExercise(exerciseId: Long) {
        viewModelScope.launch {
            exerciseRepository.archiveExercise(exerciseId)
            _uiEvent.emit(UiEvent.ShowMessage("Exercise archived"))
        }
    }

    sealed interface UiEvent {
        data class ShowMessage(val message: String) : UiEvent
        data class ShowError(val error: String) : UiEvent
    }
}

