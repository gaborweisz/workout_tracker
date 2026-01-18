package com.trainig.workouttracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.trainig.workouttracker.core.domain.model.DailyProgress
import java.time.LocalDate

@Entity(
    tableName = "daily_progress",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date", "exerciseId"], unique = true),
        Index(value = ["exerciseId"])
    ]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // Stored as ISO-8601 string (yyyy-MM-dd)
    val exerciseId: Long,
    val completedReps: Int
)

fun DailyProgressEntity.toDomain(): DailyProgress = DailyProgress(
    id = id,
    date = LocalDate.parse(date),
    exerciseId = exerciseId,
    completedReps = completedReps
)

fun DailyProgress.toEntity(): DailyProgressEntity = DailyProgressEntity(
    id = id,
    date = date.toString(),
    exerciseId = exerciseId,
    completedReps = completedReps
)

