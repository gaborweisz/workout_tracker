package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddExerciseUseCaseTest {

    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var useCase: AddExerciseUseCase

    @Before
    fun setup() {
        exerciseRepository = mockk()
        useCase = AddExerciseUseCase(exerciseRepository)
    }

    @Test
    fun `adding exercise with valid name succeeds`() = runTest {
        // Given
        val exerciseName = "Burpee"
        coEvery { exerciseRepository.isExerciseNameTaken(exerciseName) } returns false
        coEvery { exerciseRepository.insertExercise(any()) } returns 1L

        // When
        val result = useCase(exerciseName)

        // Then
        assertEquals(AddExerciseUseCase.Result.Success(1L), result)
        coVerify {
            exerciseRepository.insertExercise(match { it.name == exerciseName })
        }
    }

    @Test
    fun `adding exercise with empty name fails`() = runTest {
        // When
        val result = useCase("   ")

        // Then
        assertEquals(AddExerciseUseCase.Result.EmptyName, result)
    }

    @Test
    fun `adding exercise with duplicate name fails`() = runTest {
        // Given
        val exerciseName = "Push-up"
        coEvery { exerciseRepository.isExerciseNameTaken(exerciseName) } returns true

        // When
        val result = useCase(exerciseName)

        // Then
        assertEquals(AddExerciseUseCase.Result.NameAlreadyExists, result)
    }

    @Test
    fun `exercise name is trimmed before validation`() = runTest {
        // Given
        val exerciseName = "  Plank  "
        coEvery { exerciseRepository.isExerciseNameTaken("Plank") } returns false
        coEvery { exerciseRepository.insertExercise(any()) } returns 1L

        // When
        val result = useCase(exerciseName)

        // Then
        assertEquals(AddExerciseUseCase.Result.Success(1L), result)
        coVerify {
            exerciseRepository.insertExercise(match { it.name == "Plank" })
        }
    }
}

