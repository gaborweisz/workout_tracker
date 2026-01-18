package com.trainig.workouttracker.data.repository

import com.trainig.workouttracker.core.domain.model.DailyProgress
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.data.local.dao.DailyProgressDao
import com.trainig.workouttracker.data.local.entity.toEntity
import com.trainig.workouttracker.data.local.entity.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DailyProgressRepositoryImpl @Inject constructor(
    private val dailyProgressDao: DailyProgressDao
) : DailyProgressRepository {

    override fun getProgressForDate(date: LocalDate): Flow<List<DailyProgress>> =
        dailyProgressDao.getProgressForDate(date.toString()).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getProgressForExercise(date: LocalDate, exerciseId: Long): DailyProgress? =
        dailyProgressDao.getProgressForExercise(date.toString(), exerciseId)?.toDomain()

    override suspend fun updateProgress(progress: DailyProgress) {
        dailyProgressDao.insertProgress(progress.toEntity())
    }

    override suspend fun incrementProgress(date: LocalDate, exerciseId: Long, delta: Int) {
        dailyProgressDao.incrementProgress(date.toString(), exerciseId, delta)
    }

    override suspend fun getAllDatesWithProgress(): List<LocalDate> =
        dailyProgressDao.getAllDatesWithProgress().map { LocalDate.parse(it) }
}

