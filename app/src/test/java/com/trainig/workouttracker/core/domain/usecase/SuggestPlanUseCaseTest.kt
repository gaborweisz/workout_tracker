package com.trainig.workouttracker.core.domain.usecase

import com.trainig.workouttracker.core.domain.model.Equipment
import com.trainig.workouttracker.core.domain.model.ExperienceLevel
import com.trainig.workouttracker.core.domain.model.PlanPreferences
import com.trainig.workouttracker.core.domain.model.TrainingGoal
import com.trainig.workouttracker.core.domain.model.WorkoutFocus
import com.trainig.workouttracker.core.domain.plan.ExerciseLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SuggestPlanUseCaseTest {

    private val suggestPlan = SuggestPlanUseCase()

    private fun allPreferences(): List<PlanPreferences> =
        TrainingGoal.entries.flatMap { goal ->
            WorkoutFocus.entries.flatMap { focus ->
                Equipment.entries.flatMap { equipment ->
                    ExperienceLevel.entries.flatMap { experience ->
                        listOf(30, 45, 60, 75, 90).map { minutes ->
                            PlanPreferences(goal, focus, equipment, experience, minutes)
                        }
                    }
                }
            }
        }

    @Test
    fun `every combination yields a usable plan`() {
        allPreferences().forEach { preferences ->
            val plan = suggestPlan(preferences, Random(42))

            assertTrue("Too few exercises for $preferences", plan.exercises.size >= 3)
            assertTrue("Too many exercises for $preferences", plan.exercises.size <= 8)
            assertEquals("Duplicate exercise for $preferences", plan.exercises.size, plan.exercises.map { it.name }.toSet().size)
            plan.exercises.forEach {
                assertTrue(it.sets in 2..6)
                assertTrue(it.reps > 0)
                assertTrue(it.restSeconds > 0)
            }
        }
    }

    @Test
    fun `only suggests exercises the equipment allows`() {
        Equipment.entries.forEach { equipment ->
            WorkoutFocus.entries.forEach { focus ->
                val plan = suggestPlan(PlanPreferences(focus = focus, equipment = equipment), Random(7))
                plan.exercises.forEach { exercise ->
                    val libraryEntry = ExerciseLibrary.all.first { it.name == exercise.name }
                    assertTrue("${exercise.name} needs ${libraryEntry.requires}", libraryEntry.isAvailableWith(equipment))
                }
            }
        }
    }

    @Test
    fun `compound lifts come before isolation work`() {
        WorkoutFocus.entries.forEach { focus ->
            val plan = suggestPlan(PlanPreferences(focus = focus), Random(3))
            val compoundFlags = plan.exercises.map { exercise -> ExerciseLibrary.all.first { it.name == exercise.name }.compound }
            assertEquals(compoundFlags.sortedDescending(), compoundFlags)
        }
    }

    @Test
    fun `strength plans use fewer reps and longer rest than endurance plans`() {
        val strength = suggestPlan(PlanPreferences(goal = TrainingGoal.STRENGTH), Random(1))
        val endurance = suggestPlan(PlanPreferences(goal = TrainingGoal.ENDURANCE), Random(1))

        assertTrue(strength.exercises.maxOf { it.reps } < endurance.exercises.minOf { it.reps })
        assertTrue(strength.exercises.minOf { it.restSeconds } > endurance.exercises.maxOf { it.restSeconds })
    }

    @Test
    fun `beginners get no more than five exercises`() {
        val plan = suggestPlan(PlanPreferences(experience = ExperienceLevel.BEGINNER, durationMinutes = 90), Random(5))
        assertTrue(plan.exercises.size <= 5)
    }
}
