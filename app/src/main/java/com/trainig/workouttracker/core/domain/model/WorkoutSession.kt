package com.trainig.workouttracker.core.domain.model

data class WorkoutSession(
    val id: Long = 0L,
    val planId: Long?,
    val planName: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val sets: List<SessionSet>
) {
    val completedSets: Int get() = sets.count { it.completed }
    val totalSets: Int get() = sets.size
    val volumeKg: Double get() = sets.filter { it.completed }.sumOf { it.reps * it.weightKg }
    val isFinished: Boolean get() = finishedAt != null

    /** Sets grouped by exercise, in plan order. */
    val exercises: List<List<SessionSet>>
        get() = sets.groupBy { it.exerciseIndex }.toSortedMap().values.map { group -> group.sortedBy { it.setNumber } }
}

data class SessionSet(
    val id: Long = 0L,
    val sessionId: Long = 0L,
    val exerciseIndex: Int,
    val exerciseName: String,
    val notes: String = "",
    val setNumber: Int,
    val targetReps: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val completed: Boolean = false
)
