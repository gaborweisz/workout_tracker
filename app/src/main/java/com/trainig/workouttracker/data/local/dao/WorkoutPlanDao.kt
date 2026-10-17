package com.trainig.workouttracker.data.local.dao

import androidx.room.*
import com.trainig.workouttracker.data.local.entity.PlanExerciseEntity
import com.trainig.workouttracker.data.local.entity.PlanWithExercises
import com.trainig.workouttracker.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutPlanDao {
    @Transaction
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    abstract fun getPlans(): Flow<List<PlanWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE id = :id")
    abstract suspend fun getPlan(id: Long): PlanWithExercises?

    @Insert
    abstract suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Update
    abstract suspend fun updatePlan(plan: WorkoutPlanEntity)

    @Query("DELETE FROM workout_plans WHERE id = :id")
    abstract suspend fun deletePlan(id: Long)

    @Insert
    abstract suspend fun insertExercises(exercises: List<PlanExerciseEntity>)

    @Query("DELETE FROM plan_exercises WHERE planId = :planId")
    abstract suspend fun deleteExercisesForPlan(planId: Long)

    /** Writes the plan and replaces its exercise list atomically. */
    @Transaction
    open suspend fun savePlan(plan: WorkoutPlanEntity, exercises: (planId: Long) -> List<PlanExerciseEntity>): Long {
        val planId = if (plan.id == 0L) {
            insertPlan(plan)
        } else {
            updatePlan(plan)
            deleteExercisesForPlan(plan.id)
            plan.id
        }
        insertExercises(exercises(planId))
        return planId
    }
}
