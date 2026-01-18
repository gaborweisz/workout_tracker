package com.trainig.workouttracker.presentation.home

import app.cash.turbine.test
import com.trainig.workouttracker.core.domain.model.Exercise
import com.trainig.workouttracker.core.domain.model.TodayExerciseItem
import com.trainig.workouttracker.core.domain.usecase.GetTodayRecordsUseCase
import com.trainig.workouttracker.core.domain.usecase.IncrementProgressUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var getTodayRecordsUseCase: GetTodayRecordsUseCase
    private lateinit var incrementProgressUseCase: IncrementProgressUseCase
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTodayRecordsUseCase = mockk()
        incrementProgressUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting exercise updates selected id`() = runTest {
        // Given
        every { getTodayRecordsUseCase() } returns flowOf(emptyList())
        viewModel = HomeViewModel(getTodayRecordsUseCase, incrementProgressUseCase)

        // When
        viewModel.selectExercise(1L)

        // Then
        assertEquals(1L, viewModel.selectedExerciseId.value)
    }

    @Test
    fun `incrementing without selection does nothing`() = runTest {
        // Given
        every { getTodayRecordsUseCase() } returns flowOf(emptyList())
        viewModel = HomeViewModel(getTodayRecordsUseCase, incrementProgressUseCase)

        // When
        viewModel.incrementProgress(5)

        // Then - no crash, no calls
        assertNull(viewModel.selectedExerciseId.value)
    }

    @Test
    fun `achieving goal emits celebration event`() = runTest {
        // Given
        val exercise = Exercise(id = 1L, name = "Push-up")
        val exerciseItem = TodayExerciseItem(
            exercise = exercise,
            completedReps = 25,
            targetReps = 30,
            isGoalAchieved = false
        )

        every { getTodayRecordsUseCase() } returns flowOf(listOf(exerciseItem))
        coEvery { incrementProgressUseCase(1L, 5) } returns IncrementProgressUseCase.Result(
            oldValue = 25,
            newValue = 30,
            wasIncrement = true
        )

        viewModel = HomeViewModel(getTodayRecordsUseCase, incrementProgressUseCase)
        viewModel.selectExercise(1L)

        // When & Then
        viewModel.celebrationEvent.test {
            viewModel.incrementProgress(5)
            val event = awaitItem()
            assertEquals(
                HomeViewModel.CelebrationEvent.GoalAchieved("Push-up"),
                event
            )
        }
    }
}

