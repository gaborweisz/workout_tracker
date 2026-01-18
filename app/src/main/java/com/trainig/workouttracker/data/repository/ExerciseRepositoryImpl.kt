package com.trainig.workouttracker.data.repository

import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import com.trainig.workouttracker.data.local.dao.ExerciseDao
import com.trainig.workouttracker.data.local.entity.toEntity
import com.trainig.workouttracker.data.local.entity.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getActiveExercises(): Flow<List<Exercise>> =
        exerciseDao.getActiveExercises().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getExerciseById(id: Long): Exercise? =
        exerciseDao.getExerciseById(id)?.toDomain()

    override suspend fun insertExercise(exercise: Exercise): Long =
        exerciseDao.insertExercise(exercise.toEntity())

    override suspend fun updateExercise(exercise: Exercise) =
        exerciseDao.updateExercise(exercise.toEntity())

    override suspend fun deleteExercise(id: Long) =
        exerciseDao.deleteExercise(id)

    override suspend fun archiveExercise(id: Long) =
        exerciseDao.archiveExercise(id)

    override suspend fun isExerciseNameTaken(name: String, excludeId: Long?): Boolean {
        return if (excludeId != null) {
            exerciseDao.countExercisesWithName(name, excludeId) > 0
        } else {
            exerciseDao.countExercisesWithName(name) > 0
        }
    }
}

