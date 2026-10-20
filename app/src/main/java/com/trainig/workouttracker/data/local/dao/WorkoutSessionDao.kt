package com.trainig.workouttracker.data.local.dao

import androidx.room.*
import com.trainig.workouttracker.data.local.entity.SessionSetEntity
import com.trainig.workouttracker.data.local.entity.SessionWithSets
import com.trainig.workouttracker.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutSessionDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    abstract fun getActiveSession(): Flow<SessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY startedAt DESC LIMIT :limit")
    abstract fun getRecentSessions(limit: Int): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE finishedAt IS NOT NULL ORDER BY startedAt ASC")
    abstract fun getFinishedSessions(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    abstract fun observeSession(id: Long): Flow<SessionWithSets?>

    @Insert
    abstract suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert
    abstract suspend fun insertSets(sets: List<SessionSetEntity>)

    @Update
    abstract suspend fun updateSet(set: SessionSetEntity)

    @Query("UPDATE workout_sessions SET finishedAt = :finishedAt WHERE id = :id")
    abstract suspend fun finishSession(id: Long, finishedAt: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    abstract suspend fun deleteSession(id: Long)

    @Transaction
    open suspend fun startSession(session: WorkoutSessionEntity, sets: (sessionId: Long) -> List<SessionSetEntity>): Long {
        val sessionId = insertSession(session)
        insertSets(sets(sessionId))
        return sessionId
    }
}
