package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.TodayExerciseItem
import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import com.trainig.workouttracker.core.domain.util.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetTodayRecordsUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val dailyGoalRepository: DailyGoalRepository,
    private val dailyProgressRepository: DailyProgressRepository,
    private val dateProvider: DateProvider
) {
    operator fun invoke(): Flow<List<TodayExerciseItem>> {
        val today = dateProvider.today()

        return combine(
            exerciseRepository.getActiveExercises(),
            dailyGoalRepository.getGoalsForDate(today),
            dailyProgressRepository.getProgressForDate(today)
        ) { exercises, goals, progress ->
            exercises.map { exercise ->
                val goal = goals.find { it.exerciseId == exercise.id }?.targetReps ?: 0
                val completed = progress.find { it.exerciseId == exercise.id }?.completedReps ?: 0

                TodayExerciseItem(
                    exercise = exercise,
                    completedReps = completed,
                    targetReps = goal,
                    isGoalAchieved = goal > 0 && completed >= goal
                )
            }
        }
    }
}

