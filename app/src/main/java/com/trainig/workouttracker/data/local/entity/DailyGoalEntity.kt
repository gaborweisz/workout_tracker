package com.trainig.workouttracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.trainig.workouttracker.core.domain.model.DailyGoal
import java.time.LocalDate

@Entity(
    tableName = "daily_goals",
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
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String, // Stored as ISO-8601 string (yyyy-MM-dd)
    val exerciseId: Long,
    val targetReps: Int
)

fun DailyGoalEntity.toDomain(): DailyGoal = DailyGoal(
    id = id,
    date = LocalDate.parse(date),
    exerciseId = exerciseId,
    targetReps = targetReps
)

fun DailyGoal.toEntity(): DailyGoalEntity = DailyGoalEntity(
    id = id,
    date = date.toString(),
    exerciseId = exerciseId,
    targetReps = targetReps
)

