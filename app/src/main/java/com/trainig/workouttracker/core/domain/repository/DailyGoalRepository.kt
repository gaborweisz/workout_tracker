package com.trainig.workouttracker.core.domain.repository

import com.trainig.workouttracker.core.domain.model.DailyGoal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyGoalRepository {
    fun getGoalsForDate(date: LocalDate): Flow<List<DailyGoal>>
    suspend fun getGoalForExercise(date: LocalDate, exerciseId: Long): DailyGoal?
    suspend fun setGoal(goal: DailyGoal)
    suspend fun setGoals(goals: List<DailyGoal>)
    suspend fun getGoalsBetween(start: LocalDate, end: LocalDate): List<DailyGoal>
    suspend fun deleteGoalsForDate(date: LocalDate)
}

