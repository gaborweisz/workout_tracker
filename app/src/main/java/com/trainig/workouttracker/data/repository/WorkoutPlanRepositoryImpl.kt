package com.trainig.workouttracker.data.repository

import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.repository.WorkoutPlanRepository
import com.trainig.workouttracker.data.local.dao.WorkoutPlanDao
import com.trainig.workouttracker.data.local.entity.toDomain
import com.trainig.workouttracker.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutPlanRepositoryImpl @Inject constructor(
    private val workoutPlanDao: WorkoutPlanDao
) : WorkoutPlanRepository {

    override fun getPlans(): Flow<List<WorkoutPlan>> =
        workoutPlanDao.getPlans().map { plans -> plans.map { it.toDomain() } }

    override suspend fun getPlan(id: Long): WorkoutPlan? =
        workoutPlanDao.getPlan(id)?.toDomain()

    override suspend fun savePlan(plan: WorkoutPlan): Long =
        workoutPlanDao.savePlan(plan.toEntity()) { planId ->
            plan.exercises.mapIndexed { index, exercise -> exercise.toEntity(planId, index) }
        }

    override suspend fun deletePlan(id: Long) =
        workoutPlanDao.deletePlan(id)
}
