package com.trainig.workouttracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.trainig.workouttracker.core.domain.model.SessionSet
import com.trainig.workouttracker.core.domain.model.WorkoutSession

/** planId is deliberately not a foreign key: deleting a plan must keep its workout history. */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val planId: Long?,
    val planName: String,
    val startedAt: Long,
    val finishedAt: Long?
)

@Entity(
    tableName = "session_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SessionSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sessionId: Long,
    val exerciseIndex: Int,
    val exerciseName: String,
    val notes: String,
    val setNumber: Int,
    val targetReps: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val completed: Boolean
)

data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<SessionSetEntity>
)

fun SessionWithSets.toDomain(): WorkoutSession = WorkoutSession(
    id = session.id,
    planId = session.planId,
    planName = session.planName,
    startedAt = session.startedAt,
    finishedAt = session.finishedAt,
    sets = sets.sortedWith(compareBy({ it.exerciseIndex }, { it.setNumber })).map { it.toDomain() }
)

fun SessionSetEntity.toDomain(): SessionSet = SessionSet(
    id = id,
    sessionId = sessionId,
    exerciseIndex = exerciseIndex,
    exerciseName = exerciseName,
    notes = notes,
    setNumber = setNumber,
    targetReps = targetReps,
    reps = reps,
    weightKg = weightKg,
    restSeconds = restSeconds,
    completed = completed
)

fun SessionSet.toEntity(): SessionSetEntity = SessionSetEntity(
    id = id,
    sessionId = sessionId,
    exerciseIndex = exerciseIndex,
    exerciseName = exerciseName,
    notes = notes,
    setNumber = setNumber,
    targetReps = targetReps,
    reps = reps,
    weightKg = weightKg,
    restSeconds = restSeconds,
    completed = completed
)
