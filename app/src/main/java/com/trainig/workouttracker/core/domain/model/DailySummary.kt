package com.trainig.workouttracker.core.domain.model

import java.time.LocalDate

data class DailySummary(
    val date: LocalDate,
    val totalReps: Int,
    val goalsAchieved: Int,
    val totalGoals: Int,
    val exerciseDetails: List<ExerciseDetail>
)

data class ExerciseDetail(
    val exerciseId: Long,
    val exerciseName: String,
    val completedReps: Int,
    val targetReps: Int,
    val isGoalAchieved: Boolean
)

