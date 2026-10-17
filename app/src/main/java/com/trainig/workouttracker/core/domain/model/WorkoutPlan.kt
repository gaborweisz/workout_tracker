package com.trainig.workouttracker.core.domain.model

import kotlin.math.roundToInt

data class WorkoutPlan(
    val id: Long = 0L,
    val name: String,
    val exercises: List<PlanExercise>,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Rough session length: ~40 s of work per set plus the prescribed rest. */
    val estimatedMinutes: Int
        get() = (exercises.sumOf { it.sets * (SECONDS_PER_SET + it.restSeconds) } / 60f).roundToInt()

    companion object {
        const val SECONDS_PER_SET = 40
    }
}

data class PlanExercise(
    val id: Long = 0L,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val notes: String = ""
)
