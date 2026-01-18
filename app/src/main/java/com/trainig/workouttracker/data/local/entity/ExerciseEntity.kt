package com.trainig.workouttracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.trainig.workouttracker.core.domain.model.Exercise

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["name"], unique = true)]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    isArchived = isArchived,
    createdAt = createdAt
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    isArchived = isArchived,
    createdAt = createdAt
)

