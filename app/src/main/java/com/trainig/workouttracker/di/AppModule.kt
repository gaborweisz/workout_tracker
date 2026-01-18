package com.trainig.workouttracker.di

import com.trainig.workouttracker.core.domain.util.DateProvider
import com.trainig.workouttracker.core.domain.util.SystemDateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDateProvider(
        impl: SystemDateProvider
    ): DateProvider
}

