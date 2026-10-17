package com.trainig.workouttracker.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.trainig.workouttracker.data.local.dao.DailyGoalDao
import com.trainig.workouttracker.data.local.dao.DailyProgressDao
import com.trainig.workouttracker.data.local.dao.ExerciseDao
import com.trainig.workouttracker.data.local.dao.WorkoutPlanDao
import com.trainig.workouttracker.data.local.dao.WorkoutSessionDao
import com.trainig.workouttracker.data.local.entity.DailyGoalEntity
import com.trainig.workouttracker.data.local.entity.DailyProgressEntity
import com.trainig.workouttracker.data.local.entity.ExerciseEntity
import com.trainig.workouttracker.data.local.entity.PlanExerciseEntity
import com.trainig.workouttracker.data.local.entity.SessionSetEntity
import com.trainig.workouttracker.data.local.entity.WorkoutPlanEntity
import com.trainig.workouttracker.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        ExerciseEntity::class,
        DailyGoalEntity::class,
        DailyProgressEntity::class,
        WorkoutPlanEntity::class,
        PlanExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionSetEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2 only adds the gym plan and session tables
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
}
