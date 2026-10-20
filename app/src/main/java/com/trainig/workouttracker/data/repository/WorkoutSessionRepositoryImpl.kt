package com.trainig.workouttracker.data.repository

import com.trainig.workouttracker.core.domain.model.SessionSet
import com.trainig.workouttracker.core.domain.model.WorkoutPlan
import com.trainig.workouttracker.core.domain.model.WorkoutSession
import com.trainig.workouttracker.core.domain.repository.WorkoutSessionRepository
import com.trainig.workouttracker.data.local.dao.WorkoutSessionDao
import com.trainig.workouttracker.data.local.entity.SessionSetEntity
import com.trainig.workouttracker.data.local.entity.WorkoutSessionEntity
import com.trainig.workouttracker.data.local.entity.toDomain
import com.trainig.workouttracker.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutSessionRepositoryImpl @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao
) : WorkoutSessionRepository {

    override fun getActiveSession(): Flow<WorkoutSession?> =
        workoutSessionDao.getActiveSession().map { it?.toDomain() }

    override fun getRecentSessions(limit: Int): Flow<List<WorkoutSession>> =
        workoutSessionDao.getRecentSessions(limit).map { sessions -> sessions.map { it.toDomain() } }

    override fun getFinishedSessions(): Flow<List<WorkoutSession>> =
        workoutSessionDao.getFinishedSessions().map { sessions -> sessions.map { it.toDomain() } }

    override fun observeSession(id: Long): Flow<WorkoutSession?> =
        workoutSessionDao.observeSession(id).map { it?.toDomain() }

    override suspend fun startSession(plan: WorkoutPlan): Long {
        val session = WorkoutSessionEntity(
            planId = plan.id.takeIf { it != 0L },
            planName = plan.name,
            startedAt = System.currentTimeMillis(),
            finishedAt = null
        )
        return workoutSessionDao.startSession(session) { sessionId ->
            plan.exercises.flatMapIndexed { index, exercise ->
                (1..exercise.sets).map { setNumber ->
                    SessionSetEntity(
                        sessionId = sessionId,
                        exerciseIndex = index,
                        exerciseName = exercise.name,
                        notes = exercise.notes,
                        setNumber = setNumber,
                        targetReps = exercise.reps,
                        reps = exercise.reps,
                        weightKg = exercise.weightKg,
                        restSeconds = exercise.restSeconds,
                        completed = false
                    )
                }
            }
        }
    }

    override suspend fun updateSet(set: SessionSet) =
        workoutSessionDao.updateSet(set.toEntity())

    override suspend fun finishSession(id: Long) =
        workoutSessionDao.finishSession(id, System.currentTimeMillis())

    override suspend fun discardSession(id: Long) =
        workoutSessionDao.deleteSession(id)
}
