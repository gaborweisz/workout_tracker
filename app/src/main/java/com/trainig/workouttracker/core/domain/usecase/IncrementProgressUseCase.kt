package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.DailyProgress
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.core.domain.util.DateProvider
import javax.inject.Inject

class IncrementProgressUseCase @Inject constructor(
    private val dailyProgressRepository: DailyProgressRepository,
    private val dateProvider: DateProvider
) {
    suspend operator fun invoke(exerciseId: Long, delta: Int): Result {
        val today = dateProvider.today()

        // Get current progress
        val currentProgress = dailyProgressRepository.getProgressForExercise(today, exerciseId)
        val currentReps = currentProgress?.completedReps ?: 0
        val newReps = (currentReps + delta).coerceAtLeast(0)

        // Check if goal was just achieved
        val wasGoalJustAchieved = delta > 0 && newReps > currentReps

        // Update progress
        if (currentProgress != null) {
            dailyProgressRepository.updateProgress(
                currentProgress.copy(completedReps = newReps)
            )
        } else {
            dailyProgressRepository.updateProgress(
                DailyProgress(
                    date = today,
                    exerciseId = exerciseId,
                    completedReps = newReps
                )
            )
        }

        return Result(
            oldValue = currentReps,
            newValue = newReps,
            wasIncrement = delta > 0
        )
    }

    data class Result(
        val oldValue: Int,
        val newValue: Int,
        val wasIncrement: Boolean
    )
}

