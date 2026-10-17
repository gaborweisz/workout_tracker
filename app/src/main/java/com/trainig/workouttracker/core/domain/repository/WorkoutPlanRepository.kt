package com.trainig.workouttracker.core.domain.repository

import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface WorkoutPlanRepository {
    fun getPlans(): Flow<List<WorkoutPlan>>
    suspend fun getPlan(id: Long): WorkoutPlan?

    /** Inserts when [WorkoutPlan.id] is 0, otherwise replaces the plan and its exercises. Returns the plan id. */
    suspend fun savePlan(plan: WorkoutPlan): Long
    suspend fun deletePlan(id: Long)
}
