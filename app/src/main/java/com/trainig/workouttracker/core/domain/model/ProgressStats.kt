package com.trainig.workouttracker.core.domain.model

import java.time.LocalDate

/** Reps and goals for one calendar day, keyed by exercise id. */
data class DayActivity(
    val date: LocalDate,
    val repsByExercise: Map<Long, Int>,
    val targetsByExercise: Map<Long, Int>
) {
    /** Reps for one exercise, or for all of them when [exerciseId] is null. */
    fun reps(exerciseId: Long?): Int =
        if (exerciseId == null) repsByExercise.values.sum() else repsByExercise[exerciseId] ?: 0

    /** True when there is at least one goal in scope and every one of them was reached. */
    fun goalMet(exerciseId: Long?): Boolean {
        val targets = targetsByExercise.filter { (id, target) -> target > 0 && (exerciseId == null || id == exerciseId) }
        return targets.isNotEmpty() && targets.all { (id, target) -> (repsByExercise[id] ?: 0) >= target }
    }
}

data class ProgressStats(
    /** One entry per day, oldest first, including days with nothing logged. */
    val days: List<DayActivity>,
    /** Exercises with reps logged in the range. */
    val exercises: List<Exercise>,
    /** Consecutive days with reps logged, ending today (or yesterday if today is still empty). */
    val streakDays: Int
)
