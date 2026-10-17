package com.trainig.workouttracker.core.domain.repository

import com.trainig.workouttracker.core.domain.model.PlanPreferences
import com.trainig.workouttracker.core.domain.model.WorkoutPlan

interface AiPlanGenerator {
    /** Asks an AI model for a plan. Failures carry a user-presentable message. */
    suspend fun generate(preferences: PlanPreferences, apiKey: String): Result<WorkoutPlan>
}
