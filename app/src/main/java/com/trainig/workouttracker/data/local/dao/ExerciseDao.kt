package com.trainig.workouttracker.data.local.dao

import androidx.room.*
import com.trainig.workouttracker.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY createdAt ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun getActiveExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: Long)

    @Query("UPDATE exercises SET isArchived = 1 WHERE id = :id")
    suspend fun archiveExercise(id: Long)

    @Query("SELECT COUNT(*) FROM exercises WHERE LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun countExercisesWithName(name: String, excludeId: Long): Int

    @Query("SELECT COUNT(*) FROM exercises WHERE LOWER(name) = LOWER(:name)")
    suspend fun countExercisesWithName(name: String): Int
}

