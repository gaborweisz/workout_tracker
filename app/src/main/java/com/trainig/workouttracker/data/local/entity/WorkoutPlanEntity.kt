package com.trainig.workouttracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.trainig.workouttracker.core.domain.model.PlanExercise
import com.trainig.workouttracker.core.domain.model.WorkoutPlan

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long
)

@Entity(
    tableName = "plan_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class PlanExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val planId: Long,
    val position: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val notes: String
)

data class PlanWithExercises(
    @Embedded val plan: WorkoutPlanEntity,
    @Relation(parentColumn = "id", entityColumn = "planId")
    val exercises: List<PlanExerciseEntity>
)

fun PlanWithExercises.toDomain(): WorkoutPlan = WorkoutPlan(
    id = plan.id,
    name = plan.name,
    createdAt = plan.createdAt,
    exercises = exercises.sortedBy { it.position }.map {
        PlanExercise(
            id = it.id,
            name = it.name,
            sets = it.sets,
            reps = it.reps,
            weightKg = it.weightKg,
            restSeconds = it.restSeconds,
            notes = it.notes
        )
    }
)

fun WorkoutPlan.toEntity(): WorkoutPlanEntity = WorkoutPlanEntity(
    id = id,
    name = name,
    createdAt = createdAt
)

fun PlanExercise.toEntity(planId: Long, position: Int): PlanExerciseEntity = PlanExerciseEntity(
    planId = planId,
    position = position,
    name = name,
    sets = sets,
    reps = reps,
    weightKg = weightKg,
    restSeconds = restSeconds,
    notes = notes
)
