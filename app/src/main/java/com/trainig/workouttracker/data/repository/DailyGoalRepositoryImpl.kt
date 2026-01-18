package com.trainig.workouttracker.data.repository

import com.trainig.workouttracker.core.domain.model.DailyGoal
import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.data.local.dao.DailyGoalDao
import com.trainig.workouttracker.data.local.entity.toEntity
import com.trainig.workouttracker.data.local.entity.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DailyGoalRepositoryImpl @Inject constructor(
    private val dailyGoalDao: DailyGoalDao
) : DailyGoalRepository {

    override fun getGoalsForDate(date: LocalDate): Flow<List<DailyGoal>> =
        dailyGoalDao.getGoalsForDate(date.toString()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getGoalForExercise(date: LocalDate, exerciseId: Long): DailyGoal? =
        dailyGoalDao.getGoalForExercise(date.toString(), exerciseId)?.toDomain()

    override suspend fun setGoal(goal: DailyGoal) {
        dailyGoalDao.insertGoal(goal.toEntity())
    }

    override suspend fun setGoals(goals: List<DailyGoal>) {
        dailyGoalDao.insertGoals(goals.map { it.toEntity() })
    }

    override suspend fun deleteGoalsForDate(date: LocalDate) {
        dailyGoalDao.deleteGoalsForDate(date.toString())
    }
}

