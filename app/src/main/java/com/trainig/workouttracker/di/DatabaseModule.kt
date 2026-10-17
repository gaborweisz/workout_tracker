package com.trainig.workouttracker.di

import android.content.Context
import com.trainig.workouttracker.data.local.DatabaseBuilder
import com.trainig.workouttracker.data.local.WorkoutDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWorkoutDatabase(
        @ApplicationContext context: Context
    ): WorkoutDatabase {
        return DatabaseBuilder.getDatabase(context)
    }

    @Provides
    fun provideExerciseDao(database: WorkoutDatabase) = database.exerciseDao()

    @Provides
    fun provideDailyGoalDao(database: WorkoutDatabase) = database.dailyGoalDao()

    @Provides
    fun provideDailyProgressDao(database: WorkoutDatabase) = database.dailyProgressDao()

    @Provides
    fun provideWorkoutPlanDao(database: WorkoutDatabase) = database.workoutPlanDao()

    @Provides
    fun provideWorkoutSessionDao(database: WorkoutDatabase) = database.workoutSessionDao()
}

