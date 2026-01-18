package com.trainig.workouttracker.core.domain.model

import java.time.LocalDate

data class DailyGoal(
    val id: Long = 0L,
    val date: LocalDate,
    val exerciseId: Long,
    val targetReps: Int
)

