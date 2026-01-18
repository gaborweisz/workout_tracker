package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.DailySummary
import com.trainig.workouttracker.core.domain.model.ExerciseDetail
import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class GetDailySummaryUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val dailyGoalRepository: DailyGoalRepository,
    private val dailyProgressRepository: DailyProgressRepository
) {
    suspend operator fun invoke(date: LocalDate): DailySummary {
        val exercises = exerciseRepository.getAllExercises().first()
        val goals = dailyGoalRepository.getGoalsForDate(date).first()
        val progress = dailyProgressRepository.getProgressForDate(date).first()

        val exerciseMap = exercises.associateBy { it.id }
        val goalMap = goals.associateBy { it.exerciseId }
        val progressMap = progress.associateBy { it.exerciseId }

        val details = progress.mapNotNull { prog ->
            val exercise = exerciseMap[prog.exerciseId] ?: return@mapNotNull null
            val goal = goalMap[prog.exerciseId]
            val targetReps = goal?.targetReps ?: 0

            ExerciseDetail(
                exerciseId = prog.exerciseId,
                exerciseName = exercise.name,
                completedReps = prog.completedReps,
                targetReps = targetReps,
                isGoalAchieved = targetReps > 0 && prog.completedReps >= targetReps
            )
        }

        val totalReps = progress.sumOf { it.completedReps }
        val goalsAchieved = details.count { it.isGoalAchieved }
        val totalGoals = goals.count { it.targetReps > 0 }

        return DailySummary(
            date = date,
            totalReps = totalReps,
            goalsAchieved = goalsAchieved,
            totalGoals = totalGoals,
            exerciseDetails = details
        )
    }
}

