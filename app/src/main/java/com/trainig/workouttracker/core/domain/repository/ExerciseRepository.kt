package com.trainig.workouttracker.core.domain.repository

import com.trainig.workouttracker.core.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getActiveExercises(): Flow<List<Exercise>>
    suspend fun getExerciseById(id: Long): Exercise?
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(id: Long)
    suspend fun archiveExercise(id: Long)
    suspend fun isExerciseNameTaken(name: String, excludeId: Long? = null): Boolean
}

