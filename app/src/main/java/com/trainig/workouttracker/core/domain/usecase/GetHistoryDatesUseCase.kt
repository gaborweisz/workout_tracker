package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import java.time.LocalDate
import javax.inject.Inject

class GetHistoryDatesUseCase @Inject constructor(
    private val dailyProgressRepository: DailyProgressRepository
) {
    suspend operator fun invoke(): List<LocalDate> {
        return dailyProgressRepository.getAllDatesWithProgress()
            .sortedDescending()
    }
}

