package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.DailyProgress
import com.trainig.workouttracker.core.domain.repository.DailyProgressRepository
import com.trainig.workouttracker.core.domain.util.DateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class IncrementProgressUseCaseTest {

    private lateinit var dailyProgressRepository: DailyProgressRepository
    private lateinit var dateProvider: DateProvider
    private lateinit var useCase: IncrementProgressUseCase

    private val testDate = LocalDate.of(2025, 10, 31)
    private val testExerciseId = 1L

    @Before
    fun setup() {
        dailyProgressRepository = mockk()
        dateProvider = mockk()
        every { dateProvider.today() } returns testDate
        useCase = IncrementProgressUseCase(dailyProgressRepository, dateProvider)
    }

    @Test
    fun `increment from zero creates new progress`() = runTest {
        // Given
        coEvery { dailyProgressRepository.getProgressForExercise(testDate, testExerciseId) } returns null
        coEvery { dailyProgressRepository.updateProgress(any()) } returns Unit

        // When
        val result = useCase(testExerciseId, 5)

        // Then
        assertEquals(0, result.oldValue)
        assertEquals(5, result.newValue)
        assertEquals(true, result.wasIncrement)
        coVerify {
            dailyProgressRepository.updateProgress(
                match { it.completedReps == 5 && it.exerciseId == testExerciseId }
            )
        }
    }

    @Test
    fun `increment existing progress updates correctly`() = runTest {
        // Given
        val existingProgress = DailyProgress(
            id = 1L,
            date = testDate,
            exerciseId = testExerciseId,
            completedReps = 10
        )
        coEvery { dailyProgressRepository.getProgressForExercise(testDate, testExerciseId) } returns existingProgress
        coEvery { dailyProgressRepository.updateProgress(any()) } returns Unit

        // When
        val result = useCase(testExerciseId, 5)

        // Then
        assertEquals(10, result.oldValue)
        assertEquals(15, result.newValue)
        coVerify {
            dailyProgressRepository.updateProgress(
                match { it.completedReps == 15 }
            )
        }
    }

    @Test
    fun `decrement does not go below zero`() = runTest {
        // Given
        val existingProgress = DailyProgress(
            id = 1L,
            date = testDate,
            exerciseId = testExerciseId,
            completedReps = 3
        )
        coEvery { dailyProgressRepository.getProgressForExercise(testDate, testExerciseId) } returns existingProgress
        coEvery { dailyProgressRepository.updateProgress(any()) } returns Unit

        // When
        val result = useCase(testExerciseId, -5)

        // Then
        assertEquals(3, result.oldValue)
        assertEquals(0, result.newValue)
        coVerify {
            dailyProgressRepository.updateProgress(
                match { it.completedReps == 0 }
            )
        }
    }
}

