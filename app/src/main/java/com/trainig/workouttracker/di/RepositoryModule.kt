package com.trainig.workouttracker.di

import com.trainig.workouttracker.core.domain.repository.DailyGoalRepository
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import com.trainig.workouttracker.core.domain.repository.AiPlanGenerator
import com.trainig.workouttracker.core.domain.repository.SettingsRepository
import com.trainig.workouttracker.core.domain.repository.WorkoutPlanRepository
import com.trainig.workouttracker.core.domain.repository.WorkoutSessionRepository
import com.trainig.workouttracker.data.remote.ClaudePlanGenerator
import com.trainig.workouttracker.data.repository.DailyGoalRepositoryImpl
import com.trainig.workouttracker.data.repository.DailyProgressRepositoryImpl
import com.trainig.workouttracker.data.repository.ExerciseRepositoryImpl
import com.trainig.workouttracker.data.repository.SettingsRepositoryImpl
import com.trainig.workouttracker.data.repository.WorkoutPlanRepositoryImpl
import com.trainig.workouttracker.data.repository.WorkoutSessionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindDailyGoalRepository(
        impl: DailyGoalRepositoryImpl
    ): DailyGoalRepository

    @Binds
    @Singleton
    abstract fun bindDailyProgressRepository(
        impl: DailyProgressRepositoryImpl
    ): DailyProgressRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutPlanRepository(
        impl: WorkoutPlanRepositoryImpl
    ): WorkoutPlanRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutSessionRepository(
        impl: WorkoutSessionRepositoryImpl
    ): WorkoutSessionRepository

    @Binds
    @Singleton
    abstract fun bindAiPlanGenerator(
        impl: ClaudePlanGenerator
    ): AiPlanGenerator
}

