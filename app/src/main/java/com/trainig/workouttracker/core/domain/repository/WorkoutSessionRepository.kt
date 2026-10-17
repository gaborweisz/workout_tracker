package com.trainig.workouttracker.core.domain.repository

import com.trainig.workouttracker.core.domain.model.SessionSet
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutSessionRepository {
    /** The unfinished session, if a workout is in progress. */
    fun getActiveSession(): Flow<WorkoutSession?>
    fun getRecentSessions(limit: Int = 20): Flow<List<WorkoutSession>>
    fun observeSession(id: Long): Flow<WorkoutSession?>

    /** Creates a session with one row per planned set. Returns the session id. */
    suspend fun startSession(plan: WorkoutPlan): Long
    suspend fun updateSet(set: SessionSet)
    suspend fun finishSession(id: Long)
    suspend fun discardSession(id: Long)
}
