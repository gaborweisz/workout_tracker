package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import javax.inject.Inject

class AddExerciseUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(name: String): Result {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            return Result.EmptyName
        }

        if (exerciseRepository.isExerciseNameTaken(trimmedName)) {
            return Result.NameAlreadyExists
        }

        val id = exerciseRepository.insertExercise(
            Exercise(name = trimmedName)
        )

        return Result.Success(id)
    }

    sealed interface Result {
        data class Success(val id: Long) : Result
        data object EmptyName : Result
        data object NameAlreadyExists : Result
    }
}

