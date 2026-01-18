package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.DailyGoal
import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.core.domain.util.DateProvider
import javax.inject.Inject

class SetDailyGoalsUseCase @Inject constructor(
    private val dailyGoalRepository: DailyGoalRepository,
    private val dateProvider: DateProvider
) {
    suspend operator fun invoke(goals: Map<Long, Int>) {
        val today = dateProvider.today()
        val dailyGoals = goals.map { (exerciseId, targetReps) ->
            DailyGoal(
                date = today,
                exerciseId = exerciseId,
                targetReps = targetReps.coerceAtLeast(0)
            )
        }
        dailyGoalRepository.setGoals(dailyGoals)
    }
}

