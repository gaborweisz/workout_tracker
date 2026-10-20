package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.DayActivity
import com.trainig.workouttracker.core.domain.model.ProgressStats
import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import com.trainig.workouttracker.core.domain.util.DateProvider
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class GetProgressStatsUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val dailyGoalRepository: DailyGoalRepository,
    private val dailyProgressRepository: DailyProgressRepository,
    private val dateProvider: DateProvider
) {
    /** Stats for the last [days] days, today included. */
    suspend operator fun invoke(days: Int): ProgressStats {
        val today = dateProvider.today()
        val start = today.minusDays(days - 1L)

        val progressByDate = dailyProgressRepository.getProgressBetween(start, today).groupBy { it.date }
        val goalsByDate = dailyGoalRepository.getGoalsBetween(start, today).groupBy { it.date }

        val activity = (0 until days).map { offset ->
            val date = start.plusDays(offset.toLong())
            DayActivity(
                date = date,
                repsByExercise = progressByDate[date].orEmpty().associate { it.exerciseId to it.completedReps },
                targetsByExercise = goalsByDate[date].orEmpty().associate { it.exerciseId to it.targetReps }
            )
        }

        val loggedIds = activity.flatMap { day -> day.repsByExercise.filterValues { it > 0 }.keys }.toSet()
        val exercises = exerciseRepository.getAllExercises().first()
            .filter { it.id in loggedIds }
            .sortedBy { it.name.lowercase() }

        return ProgressStats(
            days = activity,
            exercises = exercises,
            streakDays = streak(dailyProgressRepository.getActiveDates().toSet(), today)
        )
    }

    private fun streak(activeDates: Set<LocalDate>, today: LocalDate): Int {
        // Today not being logged yet shouldn't break a streak that ran through yesterday
        var day = if (today in activeDates) today else today.minusDays(1)
        var count = 0
        while (day in activeDates) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}
