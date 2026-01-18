package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import javax.inject.Inject

class UpdateExerciseUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(exercise: Exercise): Result {
        val trimmedName = exercise.name.trim()

        if (trimmedName.isEmpty()) {
            return Result.EmptyName
        }

        if (exerciseRepository.isExerciseNameTaken(trimmedName, exercise.id)) {
            return Result.NameAlreadyExists
        }

        exerciseRepository.updateExercise(exercise.copy(name = trimmedName))
        return Result.Success
    }

    sealed interface Result {
        data object Success : Result
        data object EmptyName : Result
        data object NameAlreadyExists : Result
    }
}

