package com.trainig.workouttracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trainig.workouttracker.data.local.dao.DailyGoalDao
import com.trainig.workouttracker.data.local.dao.DailyProgressDao
import com.trainig.workouttracker.data.local.dao.ExerciseDao
import com.trainig.workouttracker.data.local.entity.DailyGoalEntity
import com.trainig.workouttracker.data.local.entity.DailyProgressEntity
import com.trainig.workouttracker.data.local.entity.ExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        DailyGoalEntity::class,
        DailyProgressEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun dailyProgressDao(): DailyProgressDao
}

