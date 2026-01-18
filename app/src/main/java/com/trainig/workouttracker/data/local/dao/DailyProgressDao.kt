package com.trainig.workouttracker.data.local.dao

import androidx.room.*
import com.trainig.workouttracker.data.local.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProgressDao {
    @Query("SELECT * FROM daily_progress WHERE date = :date")
    fun getProgressForDate(date: String): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE date = :date AND exerciseId = :exerciseId")
    suspend fun getProgressForExercise(date: String, exerciseId: Long): DailyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: DailyProgressEntity): Long

    @Update
    suspend fun updateProgress(progress: DailyProgressEntity)

    @Transaction
    suspend fun incrementProgress(date: String, exerciseId: Long, delta: Int) {
        val current = getProgressForExercise(date, exerciseId)
        if (current != null) {
            val newReps = (current.completedReps + delta).coerceAtLeast(0)
            updateProgress(current.copy(completedReps = newReps))
        } else {
            val newReps = delta.coerceAtLeast(0)
            insertProgress(
                DailyProgressEntity(
                    date = date,
                    exerciseId = exerciseId,
                    completedReps = newReps
                )
            )
        }
    }

    @Query("SELECT DISTINCT date FROM daily_progress ORDER BY date DESC")
    suspend fun getAllDatesWithProgress(): List<String>
}

