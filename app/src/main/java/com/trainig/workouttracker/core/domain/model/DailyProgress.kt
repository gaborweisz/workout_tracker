package com.trainig.workouttracker.core.domain.model

import java.time.LocalDate

data class DailyProgress(
    val id: Long = 0L,
    val date: LocalDate,
    val exerciseId: Long,
    val completedReps: Int
)

