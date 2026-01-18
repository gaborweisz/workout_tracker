package com.trainig.workouttracker.core.domain.model

data class TodayExerciseItem(
    val exercise: Exercise,
    val completedReps: Int,
    val targetReps: Int,
    val isGoalAchieved: Boolean
) {
    val hasGoal: Boolean get() = targetReps > 0
    val progressPercentage: Float get() = if (targetReps > 0) {
        (completedReps.toFloat() / targetReps).coerceIn(0f, 1f)
    } else {
        0f
    }
}

