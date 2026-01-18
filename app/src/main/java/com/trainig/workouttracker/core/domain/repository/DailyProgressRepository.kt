package com.trainig.workouttracker.core.domain.repository

import com.trainig.workouttracker.core.domain.model.DailyProgress
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyProgressRepository {
    fun getProgressForDate(date: LocalDate): Flow<List<DailyProgress>>
    suspend fun getProgressForExercise(date: LocalDate, exerciseId: Long): DailyProgress?
    suspend fun updateProgress(progress: DailyProgress)
    suspend fun incrementProgress(date: LocalDate, exerciseId: Long, delta: Int)
    suspend fun getAllDatesWithProgress(): List<LocalDate>
}

