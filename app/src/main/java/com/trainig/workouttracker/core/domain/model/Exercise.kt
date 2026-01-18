package com.trainig.workouttracker.core.domain.model

data class Exercise(
    val id: Long = 0L,
    val name: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

