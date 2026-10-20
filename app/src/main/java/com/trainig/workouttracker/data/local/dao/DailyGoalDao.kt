package com.trainig.workouttracker.data.local.dao

import androidx.room.*
import com.trainig.workouttracker.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {
    @Query("SELECT * FROM daily_goals WHERE date = :date")
    fun getGoalsForDate(date: String): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals WHERE date = :date AND exerciseId = :exerciseId")
    suspend fun getGoalForExercise(date: String, exerciseId: Long): DailyGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<DailyGoalEntity>)

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Query("SELECT * FROM daily_goals WHERE date BETWEEN :start AND :end")
    suspend fun getGoalsBetween(start: String, end: String): List<DailyGoalEntity>

    @Query("DELETE FROM daily_goals WHERE date = :date")
    suspend fun deleteGoalsForDate(date: String)
}

